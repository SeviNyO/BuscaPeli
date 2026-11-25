CREATE TABLE movies (
  movie_id INTEGER PRIMARY KEY,
  title TEXT NOT NULL,
  tmdb_id INTEGER,
  imdb_id TEXT
);
CREATE INDEX idx_movies_title ON movies (lower(title));
CREATE TABLE dataset_users (user_id INTEGER PRIMARY KEY);
CREATE TABLE dataset_ratings (
  user_id INTEGER NOT NULL REFERENCES dataset_users(user_id),
  movie_id INTEGER NOT NULL REFERENCES movies(movie_id),
  rating NUMERIC(2,1) NOT NULL CHECK (rating BETWEEN 0.5 AND 5.0),
  rated_at BIGINT NOT NULL,
  PRIMARY KEY (user_id, movie_id)
);
CREATE INDEX idx_dataset_ratings_movie ON dataset_ratings(movie_id);
CREATE TABLE movie_genres (
  movie_id INTEGER NOT NULL REFERENCES movies(movie_id) ON DELETE CASCADE,
  genre TEXT NOT NULL,
  PRIMARY KEY (movie_id, genre)
);
CREATE INDEX idx_movie_genres_genre ON movie_genres(genre);
CREATE TABLE dataset_tags (
  tag_id BIGSERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES dataset_users(user_id),
  movie_id INTEGER NOT NULL REFERENCES movies(movie_id),
  tag TEXT NOT NULL,
  tagged_at BIGINT NOT NULL
);
CREATE TABLE app_users (
  user_id UUID PRIMARY KEY,
  email TEXT NOT NULL UNIQUE,
  display_name TEXT NOT NULL,
  password_hash TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE user_ratings (
  user_id UUID NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
  movie_id INTEGER NOT NULL REFERENCES movies(movie_id),
  rating NUMERIC(2,1) NOT NULL CHECK (rating BETWEEN 0.5 AND 5.0),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, movie_id)
);
CREATE TABLE favorites (
  user_id UUID NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
  movie_id INTEGER NOT NULL REFERENCES movies(movie_id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, movie_id)
);
CREATE TABLE watchlist (
  user_id UUID NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
  movie_id INTEGER NOT NULL REFERENCES movies(movie_id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, movie_id)
);
CREATE TABLE user_tags (
  user_id UUID NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
  movie_id INTEGER NOT NULL REFERENCES movies(movie_id),
  tag TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, movie_id, tag)
);
