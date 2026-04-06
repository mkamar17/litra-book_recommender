import os
import pickle
from flask import Flask, jsonify
from sqlalchemy import create_engine, text
import pandas as pd
from surprise import SVD, Reader, Dataset
from surprise.model_selection import train_test_split as surprise_split

app = Flask(__name__)

DB_URL = "postgresql://postgres:FYP_connect10@35.246.42.56:5432/postgres"
engine = create_engine(DB_URL)

# loading and prepping goodreads data - cached after first run 

CACHE_PATH = "datasets/filtered_df_cache.pkl"

if os.path.exists(CACHE_PATH):
    print("Loading preprocessed data from cache...")
    filtered_df = pd.read_pickle(CACHE_PATH)
    print(f"Data ready: {filtered_df['user_id'].nunique()} users, {filtered_df['book_id'].nunique()} books")
else:
    print("Processing Goodreads data for first time (this takes ~60s)...")

    interactions = pd.read_json(
        "datasets/goodreads_interactions_young_adult.json.gz",
        lines=True, nrows=2_000_000
    )
    books_ya = pd.read_json(
        "datasets/goodreads_books_young_adult.json.gz",
        lines=True
    )

    raw_df = interactions.merge(books_ya, on="book_id")
    clean_df = raw_df[raw_df["rating"] > 0].copy()
    filtered_df = clean_df.groupby("book_id").filter(lambda x: len(x) >= 5)
    filtered_df = filtered_df.groupby("user_id").filter(lambda x: len(x) >= 5)
    filtered_df = filtered_df[
        filtered_df["image_url"].notna() &
        (filtered_df["image_url"] != "") &
        (~filtered_df["image_url"].str.contains("nophoto", na=False))
    ]

    # getting authors
    authors_df = pd.read_json("datasets/goodreads_book_authors.json.gz", lines=True)
    authors_df["author_id"] = authors_df["author_id"].astype(str)
    filtered_df["author_id"] = filtered_df["authors"].apply(
        lambda x: x[0]["author_id"] if isinstance(x, list) and len(x) > 0 else None
    )
    filtered_df = filtered_df.merge(
        authors_df[["author_id", "name"]], on="author_id", how="left"
    )
    filtered_df = filtered_df.rename(columns={"name": "author"})
    filtered_df = filtered_df.drop(columns=["authors", "author_id"])

    # extraing genre
    GENRE_SHELVES = {
        "fantasy", "romance", "science-fiction", "sci-fi", "mystery", "thriller",
        "horror", "dystopia", "paranormal", "urban-fantasy", "historical-fiction",
        "contemporary", "adventure", "fiction", "non-fiction", "humor", "comedy",
        "paranormal-romance", "sci-fi-fantasy", "teen-fiction", "young-adult-fiction"
    }

    def extract_genre(shelves):
        if not isinstance(shelves, list):
            return "unknown"
        for shelf in shelves:
            if shelf["name"] in GENRE_SHELVES:
                return shelf["name"]
        return "unknown"

    filtered_df["genre"] = filtered_df["popular_shelves"].apply(extract_genre)
    filtered_df = filtered_df.drop(columns=["popular_shelves"])

    filtered_df.to_pickle(CACHE_PATH)
    print(f"Data ready and cached: {filtered_df['user_id'].nunique()} users, {filtered_df['book_id'].nunique()} books")


# training SVD on goodreads dataset 

print("Training SVD model...")
reader = Reader(rating_scale=(1, 5))
base_data = Dataset.load_from_df(
    filtered_df[["user_id", "book_id", "rating"]], reader
)
trainset, _ = surprise_split(base_data, test_size=0.2)
svd = SVD(n_factors=50, n_epochs=30, lr_all=0.005, reg_all=0.02)
svd.fit(trainset)
print("SVD model ready.")

# cold start - popular books 

num_rating_df = filtered_df.groupby("book_id")["rating"].count().reset_index()
num_rating_df.rename(columns={"rating": "num_ratings"}, inplace=True)
avg_rating_df = filtered_df.groupby("book_id")["rating"].mean().reset_index()
avg_rating_df.rename(columns={"rating": "avg_rating"}, inplace=True)
book_info_df = filtered_df[[
    "book_id", "title", "author", "genre", "description", "image_url"
]].drop_duplicates("book_id")

popular_df = num_rating_df.merge(avg_rating_df, on="book_id").merge(book_info_df, on="book_id")
C = popular_df["num_ratings"].mean()
m = popular_df["avg_rating"].mean()
popular_df["score"] = (
    (popular_df["num_ratings"] * popular_df["avg_rating"]) + (C * m)
) / (popular_df["num_ratings"] + C)
popular_df = popular_df[popular_df["num_ratings"] >= 50]
popular_df = (
    popular_df.sort_values("score", ascending=False)
    .drop_duplicates(subset="title", keep="first")
    .reset_index(drop=True)
)

# helper methods

def get_app_user_ratings(internal_user_id):
    """
    Read this app user's real ratings from book_rating table.
    Only returns Goodreads books (numeric external_ids) since those
    are the only ones present in the SVD training data.
    Google Books IDs (alphanumeric like 'NbcYEQAAQBAJ') are excluded.
    """
    with engine.connect() as conn:
        result = conn.execute(text("""
            SELECT br.user_id, b.external_id AS book_id, br.rating
            FROM book_rating br
            JOIN book b ON b.id = br.book_id
            WHERE br.user_id = :uid
        """), {"uid": internal_user_id})
        rows = result.fetchall()

    if not rows:
        return pd.DataFrame(columns=["user_id", "book_id", "rating"])

    df = pd.DataFrame(rows, columns=["user_id", "book_id", "rating"])

    # filtering the google books from training 

    df = df[df["book_id"].str.isnumeric()]

    return df


def find_best_proxy(user_rated_book_ids, user_genres):
    """
    Find the Goodreads user whose ratings overlap most with
    the app user's rated books and preferred genres.
    Book overlap takes priority over genre overlap.
    """

    book_overlap = filtered_df[
        filtered_df["book_id"].astype(str).isin(user_rated_book_ids)
    ].groupby("user_id").size().sort_values(ascending=False)

    if len(book_overlap) > 0:
        return book_overlap.index[0]

    genre_overlap = filtered_df[
        filtered_df["genre"].isin(user_genres)
    ].groupby("user_id").size().sort_values(ascending=False)

    if len(genre_overlap) > 0:
        return genre_overlap.index[0]

    # otherwise, most active user
    return filtered_df.groupby("user_id").size().sort_values(ascending=False).index[0]


def get_personalised_recommendations(internal_user_id, user_ratings_df, n=20):
    """
    Blend the app user's real ratings with a Goodreads proxy user's ratings,
    retrain SVD on the blended dataset, and return top-n recommendations.
    """
    rated_book_ids = set(user_ratings_df["book_id"].astype(str).values)
    user_genres = set(
        filtered_df[filtered_df["book_id"].astype(str).isin(rated_book_ids)]["genre"].values
    )

    proxy_user_id = find_best_proxy(rated_book_ids, user_genres)
    print(f"Proxy user for app user {internal_user_id}: {proxy_user_id}")

    # getting the proxy users' Goodreads ratings
    proxy_ratings = filtered_df[filtered_df["user_id"] == proxy_user_id][
        ["user_id", "book_id", "rating"]
    ].copy()
    proxy_ratings["book_id"] = proxy_ratings["book_id"].astype(str)

    APP_USER_ID = f"app_user_{internal_user_id}"

    # building the app user's ratings in the same format
    app_ratings = user_ratings_df[["book_id", "rating"]].copy()
    app_ratings["book_id"] = app_ratings["book_id"].astype(str)
    app_ratings["user_id"] = APP_USER_ID

    # blend: proxy Goodreads ratings + app user's real ratings
    blended = pd.concat([
        proxy_ratings[["user_id", "book_id", "rating"]],
        app_ratings[["user_id", "book_id", "rating"]]
    ], ignore_index=True)

    # retrain SVD on blended data
    blend_reader = Reader(rating_scale=(1, 5))
    blend_data = Dataset.load_from_df(blended[["user_id", "book_id", "rating"]], blend_reader)
    blend_trainset = blend_data.build_full_trainset()
    blend_svd = SVD(n_factors=50, n_epochs=30, lr_all=0.005, reg_all=0.02)
    blend_svd.fit(blend_trainset)

    # predict ratings for all unrated Goodreads books
    all_books = filtered_df["book_id"].unique()
    unrated = [b for b in all_books if str(b) not in rated_book_ids]
    preds = [blend_svd.predict(APP_USER_ID, str(b)) for b in unrated]
    preds.sort(key=lambda x: x.est, reverse=True)
    top = preds[:n]

    book_ids = [p.iid for p in top]
    scores = [round(p.est, 3) for p in top]

    info = filtered_df[[
        "book_id", "title", "author", "genre", "description", "image_url"
    ]].drop_duplicates("book_id").copy()
    info["book_id"] = info["book_id"].astype(str)

    results = pd.DataFrame({"book_id": book_ids, "predicted_rating": scores})
    results = results.merge(info, on="book_id", how="left")
    results = results.drop_duplicates(subset="title", keep="first")
    return results


def push_recommendations(internal_user_id, recs_df):
    """Upsert recommended books into book table and write to recommendations table."""
    books_to_save = recs_df[[
        "book_id", "title", "author", "description", "genre", "image_url"
    ]].copy().rename(columns={"book_id": "external_id", "image_url": "cover_url"})
    books_to_save["source"] = "goodreads"
    books_to_save["external_id"] = books_to_save["external_id"].astype(str)

    with engine.begin() as conn:
        for _, row in books_to_save.iterrows():
            conn.execute(text("""
                INSERT INTO book (external_id, title, author, description, genre, cover_url, source)
                VALUES (:external_id, :title, :author, :description, :genre, :cover_url, :source)
                ON CONFLICT (external_id) DO UPDATE SET
                    title       = EXCLUDED.title,
                    author      = EXCLUDED.author,
                    description = EXCLUDED.description,
                    genre       = EXCLUDED.genre,
                    cover_url   = EXCLUDED.cover_url,
                    source      = EXCLUDED.source
            """), row.to_dict())

        external_ids = books_to_save["external_id"].tolist()
        result = conn.execute(text("""
            SELECT id, external_id FROM book WHERE external_id = ANY(:ids)
        """), {"ids": external_ids})
        id_map = {str(r.external_id): r.id for r in result}

        conn.execute(text(
            "DELETE FROM recommendations WHERE user_id = :uid"
        ), {"uid": internal_user_id})

        valid_recs = [(bid, recs_df.loc[recs_df["book_id"] == bid, "predicted_rating"].values[0])
                      for bid in recs_df["book_id"] if str(bid) in id_map]

        recs_to_save = pd.DataFrame({
            "user_id": internal_user_id,
            "book_id": [id_map[str(bid)] for bid, _ in valid_recs],
            "score":   [score for _, score in valid_recs]
        })
        recs_to_save.to_sql("recommendations", conn, if_exists="append", index=False)

    print(f"Pushed {len(recs_to_save)} recommendations for user {internal_user_id}")


@app.route("/recommend/<int:internal_user_id>", methods=["POST"])
def recommend(internal_user_id):
    user_ratings = get_app_user_ratings(internal_user_id)
    rated_ids = set(user_ratings["book_id"].astype(str).values)

    print(f"User {internal_user_id} has {len(user_ratings)} usable Goodreads ratings out of total rated books")

    if len(user_ratings) < 5:
        recs = (
            popular_df[~popular_df["book_id"].astype(str).isin(rated_ids)]
            .head(20)[["book_id", "title", "author", "genre", "description", "image_url", "score"]]
            .rename(columns={"score": "predicted_rating"})
            .copy()
        )
        recs["book_id"] = recs["book_id"].astype(str)
        method = "popularity"
    else:
        # warm start — blend real ratings with Goodreads proxy, retrain SVD after each new rating
        recs = get_personalised_recommendations(internal_user_id, user_ratings, n=20)
        method = "svd_blended"

    push_recommendations(internal_user_id, recs)
    return jsonify({"status": "ok", "method": method, "count": len(recs)})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)