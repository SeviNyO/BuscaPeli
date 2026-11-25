package com.buscapeli.config;

import com.buscapeli.recommendation.RecommendationEngine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Order(1)
public class DatasetImporter implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final Path directory;
    private final int movieLimit;
    private final int minimumUserRatings;

    public DatasetImporter(JdbcTemplate jdbc,
            @Value("${app.dataset-path}") String directory,
            @Value("${app.top-movies-limit}") int movieLimit,
            @Value("${app.min-user-ratings}") int minimumUserRatings) {
        this.jdbc = jdbc;
        this.directory = Path.of(directory).toAbsolutePath().normalize();
        this.movieLimit = movieLimit;
        this.minimumUserRatings = minimumUserRatings;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (jdbc.queryForObject("SELECT count(*) FROM movies", Integer.class) > 0) return;
        for (String name : List.of("movies.csv", "ratings.csv", "tags.csv", "links.csv")) {
            if (!Files.isRegularFile(directory.resolve(name)))
                throw new IllegalStateException("Falta " + name + " en " + directory);
        }

        Map<String, Map<String, String>> movies = index(read("movies.csv"), "movieId");
        Map<String, Map<String, String>> links = index(read("links.csv"), "movieId");
        List<Map<String, String>> rawRatings = read("ratings.csv");
        Map<String, Long> counts = rawRatings.stream().collect(Collectors.groupingBy(r -> r.get("movieId"), Collectors.counting()));
        Set<String> popular = counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed()
                        .thenComparingInt(e -> Integer.parseInt(e.getKey())))
                .limit(movieLimit).map(Map.Entry::getKey).collect(Collectors.toSet());
        List<Map<String, String>> selectedRatings = rawRatings.stream().filter(r -> popular.contains(r.get("movieId"))).toList();
        Map<String, Long> byUser = selectedRatings.stream().collect(Collectors.groupingBy(r -> r.get("userId"), Collectors.counting()));
        Set<String> activeUsers = byUser.entrySet().stream().filter(e -> e.getValue() >= minimumUserRatings)
                .map(Map.Entry::getKey).collect(Collectors.toSet());

        for (String movieId : popular) {
            Map<String, String> row = movies.get(movieId);
            if (row == null) continue;
            Map<String, String> link = links.getOrDefault(movieId, Map.of());
            jdbc.update("INSERT INTO movies(movie_id,title,tmdb_id,imdb_id) VALUES (?,?,?,?)",
                    Integer.parseInt(movieId), row.get("title"), nullableInt(link.get("tmdbId")), emptyToNull(link.get("imdbId")));
            String genres = row.get("genres");
            if (genres != null && !genres.equals("(no genres listed)"))
                for (String genre : genres.split("\\|")) jdbc.update("INSERT INTO movie_genres(movie_id,genre) VALUES (?,?) ON CONFLICT DO NOTHING", Integer.parseInt(movieId), genre);
        }
        for (String userId : activeUsers) jdbc.update("INSERT INTO dataset_users(user_id) VALUES (?) ON CONFLICT DO NOTHING", Integer.parseInt(userId));
        List<Map<String,String>> finalRatings=selectedRatings.stream().filter(r->activeUsers.contains(r.get("userId"))).toList();
        jdbc.batchUpdate("INSERT INTO dataset_ratings(user_id,movie_id,rating,rated_at) VALUES (?,?,?,?)",finalRatings,1000,(ps,row)->{
            ps.setInt(1,Integer.parseInt(row.get("userId")));ps.setInt(2,Integer.parseInt(row.get("movieId")));
            ps.setDouble(3,Double.parseDouble(row.get("rating")));ps.setLong(4,Long.parseLong(row.get("timestamp")));
        });
        List<Map<String,String>> finalTags=read("tags.csv").stream().filter(r->activeUsers.contains(r.get("userId"))&&popular.contains(r.get("movieId"))).toList();
        jdbc.batchUpdate("INSERT INTO dataset_tags(user_id,movie_id,tag,tagged_at) VALUES (?,?,?,?)",finalTags,500,(ps,row)->{
            ps.setInt(1,Integer.parseInt(row.get("userId")));ps.setInt(2,Integer.parseInt(row.get("movieId")));
            ps.setString(3,row.get("tag"));ps.setLong(4,Long.parseLong(row.get("timestamp")));
        });
        System.out.printf("MovieLens importado: %d películas, %d usuarios activos, %d ratings.%n",
                jdbc.queryForObject("SELECT count(*) FROM movies", Integer.class), activeUsers.size(),
                jdbc.queryForObject("SELECT count(*) FROM dataset_ratings", Integer.class));
    }

    private List<Map<String, String>> read(String name) throws Exception {
        try (Reader reader = Files.newBufferedReader(directory.resolve(name), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get().parse(reader)) {
            List<Map<String, String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                Map<String, String> row = new HashMap<>();
                for (String header : parser.getHeaderMap().keySet()) row.put(header, record.get(header));
                rows.add(row);
            }
            return rows;
        }
    }

    private static Map<String, Map<String, String>> index(List<Map<String, String>> rows, String key) {
        return rows.stream().collect(Collectors.toMap(r -> r.get(key), r -> r, (a, b) -> a));
    }
    private static Integer nullableInt(String value) { return value == null || value.isBlank() ? null : Integer.valueOf(value); }
    private static String emptyToNull(String value) { return value == null || value.isBlank() ? null : value; }
}
