from flask import Flask, jsonify
from sqlalchemy import create_engine, text
import pandas as pd
from surprise import SVD, Reader, Dataset
from surprise.model_selection import train_test_split as surprise_split

app = Flask(__name__)

# --- same connection string you use in the notebook ---
DB_URL = "postgresql://postgres:Dormezzled79!@35.246.42.56:5432/postgres"
engine = create_engine(DB_URL)

# --- load and train SVD once on startup ---
print("Loading Goodreads data and training SVD...")

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

reader = Reader(rating_scale=(1, 5))
data = Dataset.load_from_df(
    filtered_df[["user_id", "book_id", "rating"]], reader
)
trainset, _ = surprise_split(data, test_size=0.2)
svd = SVD(n_factors=50, n_epochs=30, lr_all=0.005, reg_all=0.02)
svd.fit(trainset)
print("SVD model ready.")

# --- popularity fallback (same as notebook) ---
num_rating_df = filtered_df.groupby("book_id")["rating"].count().reset_index()
num_rating_df.rename(columns={"rating": "num_ratings"}, inplace=True)
avg_rating_df = filtered_df.groupby("book_id")["rating"].mean().reset_index()
avg_rating_df.rename(columns={"rating": "avg_rating"}, inplace=True)
book_info = filtered_df[[
    "book_id", "title", "author", "genre", "description", "image_url"
]].drop_duplicates("book_id")

popular_df = num_rating_df.merge(avg_rating_df, on="book_id").merge(book_info, on="book_id")
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


def get_app_user_ratings(internal_user_id):
    """Read this user's ratings from book_rating table."""
    df = pd.read_sql("""
        SELECT br.user_id, b.external_id AS book_id, br.rating
        FROM book_rating br
        JOIN book b ON b.id = br.book_id
        WHERE br.user_id = :uid
    """, engine, params={"uid": internal_user_id})
    return df


def get_svd_recommendations(proxy_user_id, rated_book_ids, n=10):
    all_books = filtered_df["book_id"].unique()
    unrated = [b for b in all_books if str(b) not in rated_book_ids]
    preds = [svd.predict(proxy_user_id, b) for b in unrated]
    preds.sort(key=lambda x: x.est, reverse=True)
    top = preds[:n]
    book_ids = [p.iid for p in top]
    scores = [round(p.est, 3) for p in top]
    info = filtered_df[[
        "book_id", "title", "author", "genre", "description", "image_url"
    ]].drop_duplicates("book_id")
    results = pd.DataFrame({"book_id": book_ids, "predicted_rating": scores})
    results = results.merge(info, on="book_id", how="left")
    results = results.drop_duplicates(subset="title", keep="first")
    return results


def push_recommendations(internal_user_id, recs_df):
    """Upsert recommended books and write to recommendations table."""
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
                    title = EXCLUDED.title, author = EXCLUDED.author,
                    description = EXCLUDED.description, genre = EXCLUDED.genre,
                    cover_url = EXCLUDED.cover_url, source = EXCLUDED.source
            """), row.to_dict())

        external_ids = recs_df["book_id"].astype(str).tolist()
        result = conn.execute(text("""
            SELECT id, external_id FROM book WHERE external_id = ANY(:ids)
        """), {"ids": external_ids})
        id_map = {str(r.external_id): r.id for r in result}

        conn.execute(text(
            "DELETE FROM recommendations WHERE user_id = :uid"
        ), {"uid": internal_user_id})

        recs_to_save = pd.DataFrame({
            "user_id": internal_user_id,
            "book_id": [id_map[str(bid)] for bid in recs_df["book_id"]],
            "score": recs_df["predicted_rating"].values
        })
        recs_to_save.to_sql("recommendations", conn, if_exists="append", index=False)

    print(f"Pushed {len(recs_to_save)} recommendations for user {internal_user_id}")


@app.route("/recommend/<int:internal_user_id>", methods=["POST"])
def recommend(internal_user_id):
    user_ratings = get_app_user_ratings(internal_user_id)
    rated_ids = set(user_ratings["book_id"].astype(str).values)

    if len(user_ratings) < 5:
        # cold start — popularity
        recs = (
            popular_df[~popular_df["book_id"].astype(str).isin(rated_ids)]
            .head(10)
            [["book_id", "title", "author", "genre", "description", "image_url", "score"]]
            .rename(columns={"score": "predicted_rating"})
            .copy()
        )
        method = "popularity"
    else:
        # warm start — find nearest Goodreads proxy user by genre overlap
        user_genres = set(
            filtered_df[filtered_df["book_id"].astype(str).isin(rated_ids)]["genre"].values
        )
        genre_match = (
            filtered_df[filtered_df["genre"].isin(user_genres)]
            .groupby("user_id").size()
            .sort_values(ascending=False)
        )
        proxy_user = genre_match.index[0] if len(genre_match) > 0 \
            else filtered_df["user_id"].iloc[0]
        recs = get_svd_recommendations(proxy_user, rated_ids, n=10)
        method = "svd_proxy"

    push_recommendations(internal_user_id, recs)
    return jsonify({"status": "ok", "method": method, "count": len(recs)})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)