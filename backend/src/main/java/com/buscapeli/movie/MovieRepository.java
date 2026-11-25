package com.buscapeli.movie;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class MovieRepository {
    private static final Pattern YEAR = Pattern.compile("\\((\\d{4})\\)$");
    private final JdbcTemplate jdbc;
    public MovieRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<MovieDto> findAll() {
        String sql = """
            SELECT m.movie_id,m.title,m.tmdb_id,m.imdb_id,
                   COALESCE(AVG(r.rating),0) AS average_rating,COUNT(DISTINCT r.user_id) AS rating_count,
                   COALESCE(string_agg(DISTINCT g.genre,'|'),'') AS genres
            FROM movies m LEFT JOIN dataset_ratings r ON r.movie_id=m.movie_id
            LEFT JOIN movie_genres g ON g.movie_id=m.movie_id
            GROUP BY m.movie_id,m.title,m.tmdb_id,m.imdb_id
            """;
        return jdbc.query(sql, (rs, row) -> {
            String title = rs.getString("title");
            Matcher matcher = YEAR.matcher(title);
            Integer year = matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
            String genres = rs.getString("genres");
            return new MovieDto(rs.getInt("movie_id"), title, year,
                    genres.isBlank() ? List.of() : Arrays.asList(genres.split("\\|")),
                    (Integer) rs.getObject("tmdb_id"), rs.getString("imdb_id"),
                    rs.getDouble("average_rating"), rs.getLong("rating_count"), null);
        });
    }

    public Optional<MovieDto> findById(int id) {
        return findAll().stream().filter(movie -> movie.movie_id() == id).findFirst();
    }

    public Map<Integer, List<Map<String, Object>>> ratingsByUser() {
        Map<Integer, List<Map<String, Object>>> result = new HashMap<>();
        jdbc.query("SELECT user_id,movie_id,rating FROM dataset_ratings", rs -> {
            result.computeIfAbsent(rs.getInt("user_id"), ignored -> new ArrayList<>())
                    .add(Map.of("movie_id", rs.getInt("movie_id"), "rating", rs.getDouble("rating")));
        });
        return result;
    }

    public List<Map<String,Object>> allRatings() {
        return jdbc.query("SELECT user_id,movie_id,rating FROM dataset_ratings",
                (rs, row) -> Map.of("user_id",rs.getInt("user_id"),"movie_id",rs.getInt("movie_id"),"rating",rs.getDouble("rating")));
    }

    public Map<Integer, Set<String>> genresByMovie() {
        return jdbc.query("SELECT movie_id,genre FROM movie_genres", rs -> {
            Map<Integer,Set<String>> result = new HashMap<>();
            do { result.computeIfAbsent(rs.getInt("movie_id"), ignored -> new HashSet<>()).add(rs.getString("genre")); }
            while (rs.next());
            return result;
        });
    }
}
