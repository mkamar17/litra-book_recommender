# 📚 BookApp — User Manual & Setup Guide

This document walks you through everything needed to download the data, configure the environment, and run all components of the application from scratch.

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Download the Datasets](#2-download-the-datasets)
3. [Set Up the Database Access](#3-set-up-the-database-access)
4. [Set Up & Run the Recommender API (ML Service)](#4-set-up--run-the-recommender-api-ml-service)
5. [Set Up & Run the Backend (Spring Boot)](#5-set-up--run-the-backend-spring-boot)
6. [Set Up & Run the Frontend (React)](#6-set-up--run-the-frontend-react)

---

## 1. Prerequisites

Before getting started, ensure the following tools are installed on your machine:

| Tool | Version | Purpose |
|---|---|---|
| Java (JDK) | 17+ | Running the Spring Boot backend |
| Apache Maven | 3.8+ | Building and running the backend |
| Python | 3.8+ | Running the recommender API |
| Node.js & npm | 18+ | Running the React frontend |

> **Tip:** You can verify each installation by running `java -version`, `mvn -version`, `python --version`, and `node --version` in your terminal.

---

## 2. Download the Datasets

The application uses the **UCSD Book Graph — Young Adult** dataset. You must download all three files before running the recommender service.

**Source:** [https://sites.google.com/eng.ucsd.edu/ucsdbookgraph/home](https://sites.google.com/eng.ucsd.edu/ucsdbookgraph/home)

Download the following three files from the **Young Adult** section of that page:

| File | Description | Size |
|---|---|---|
| `goodreads_books_young_adult.json.gz` | Book metadata | 93,398 books |
| `goodreads_interactions_young_adult.json.gz` | User-book interactions | 34,919,254 interactions |
| `goodreads_reviews_young_adult.json.gz` | Detailed user reviews | 2,389,900 reviews |

Once downloaded, create a `datasets/` directory inside the `product/ML/` directory and place all three files there:

```bash
mkdir product/ML/datasets
```

Move the three `.json.gz` files into `product/ML/datasets/`. **Do not extract them** — the recommender API reads them directly and will handle all data cleaning automatically on first run.

Your directory structure should look like this:

```
project-root/
├── product/
│   ├── ML/
│   │   ├── datasets/                          ← place the 3 downloaded .json.gz files here
│   │   │   ├── goodreads_books_young_adult.json.gz
│   │   │   ├── goodreads_interactions_young_adult.json.gz
│   │   │   └── goodreads_reviews_young_adult.json.gz
│   │   ├── notebooks/
│   │   │   └── book_recommender.ipynb
│   │   ├── recommender_api.py
│   │   └── requirements.txt
│   ├── Backend/
│   │   ├── src/
│   │   └── pom.xml
│   └── Frontend/
│       ├── src/
│       ├── index.html
│       └── package.json
└── README.md
```

> **Note:** After the first run, the API caches the processed datasets locally, so subsequent startups will be significantly faster.

---

## 3. Set Up the Database Access

The backend connects to a **Google Cloud SQL** database. Your machine's IP address must be added to the Authorised Networks list before the backend can connect.

To do this:
1. Find your current public **IPv4** address by visiting [https://whatismyip.com](https://whatismyip.com)
2. Send an email to **mkamar2740@gmail.com** with the subject line **"Request to whitelist IP address"** and include your IP address in the body
3. Once you've been notified that your IP has been added, proceed to Step 4

---

## 4. Set Up & Run the Recommender API (ML Service)

The recommender API is a Python service that handles all machine learning logic, including data cleaning and generating book recommendations.

**Step 4a — Navigate to the ML directory**

```bash
cd product/ML
```

**Step 4b — Create and activate a virtual environment**

```bash
# Create the virtual environment
python -m venv venv

# Activate it (macOS/Linux)
source venv/bin/activate

# Activate it (Windows)
venv\Scripts\activate
```

**Step 4c — Install dependencies**

```bash
pip install -r requirements.txt
```

**Step 4d — Run the recommender API**

```bash
python recommender_api.py
```

The service will start and process the datasets on first launch. Once you see the server is running, leave this terminal open and proceed to the next step.

> **Note:** The first run may take several minutes as it cleans and caches the datasets. All subsequent runs will start much faster.

---

## 5. Set Up & Run the Backend (Spring Boot)

**Step 5a — Navigate to the backend directory**

```bash
cd product/Backend
```

**Step 5b — Run the backend using Maven**

```bash
mvn spring-boot:run
```

Maven will automatically download all required dependencies and start the Spring Boot server. Once you see the server started message in the terminal, the backend is ready.

> **Ensure your IP address has been whitelisted (Step 3b) before running this**, otherwise the backend will fail to connect to the database.

---

## 6. Set Up & Run the Frontend (React)

**Step 6a — Navigate to the frontend directory**

```bash
cd product/Frontend
```

**Step 6b — Install dependencies**

```bash
npm install
```

**Step 6c — Start the development server**

```bash
npm run dev
```

The frontend will start and display a local URL in the terminal (typically `http://localhost:5173`). Open this in your browser to access the application.

---

## Running Order Summary

For quick reference, always start the services in this order:

```
1. Recommender API   →   python recommender_api.py        (inside product/ML/)
2. Backend           →   mvn spring-boot:run               (inside product/Backend/)
3. Frontend          →   npm run dev                       (inside product/Frontend/)
```

All three must be running simultaneously for the application to work correctly.


