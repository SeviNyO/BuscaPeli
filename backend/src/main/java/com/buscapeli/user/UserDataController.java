package com.buscapeli.user;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/me")
public class UserDataController {
    private final JdbcTemplate jdbc;
    public UserDataController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public record RatingInput(@NotNull Integer movie_id,@DecimalMin("0.5") @DecimalMax("5.0") double rating){}
    private String user(HttpSession session){Object id=session.getAttribute("userId");if(id==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Inicia sesión");return id.toString();}

    @GetMapping("/ratings") public List<Map<String,Object>> ratings(HttpSession session){
        String sql="""
            SELECT m.movie_id,m.title,m.tmdb_id,m.imdb_id,ur.rating AS user_rating,
                   COALESCE(AVG(dr.rating),0) AS average_rating,COUNT(DISTINCT dr.user_id) AS rating_count,
                   COALESCE(string_agg(DISTINCT g.genre,'|'),'') AS genres
            FROM user_ratings ur JOIN movies m USING(movie_id)
            LEFT JOIN dataset_ratings dr ON dr.movie_id=m.movie_id LEFT JOIN movie_genres g ON g.movie_id=m.movie_id
            WHERE ur.user_id=? GROUP BY m.movie_id,m.title,m.tmdb_id,m.imdb_id,ur.rating ORDER BY max(ur.updated_at) DESC
            """;
        return movieRows(sql,UUID.fromString(user(session)));
    }
    @PutMapping("/ratings") public Map<String,Object> rate(@Valid @RequestBody RatingInput input,HttpSession session){
        String uid=user(session); if(jdbc.queryForObject("SELECT count(*) FROM movies WHERE movie_id=?",Integer.class,input.movie_id())==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Película inexistente");
        jdbc.update("INSERT INTO user_ratings(user_id,movie_id,rating) VALUES (?,?,?) ON CONFLICT(user_id,movie_id) DO UPDATE SET rating=EXCLUDED.rating,updated_at=now()",UUID.fromString(uid),input.movie_id(),input.rating());
        return Map.of("movie_id",input.movie_id(),"rating",input.rating());
    }
    @DeleteMapping("/ratings/{movieId}") public Map<String,Object> removeRating(@PathVariable int movieId,HttpSession session){jdbc.update("DELETE FROM user_ratings WHERE user_id=? AND movie_id=?",UUID.fromString(user(session)),movieId);return Map.of("movie_id",movieId,"deleted",true);}
    @GetMapping("/favorites") public List<Map<String,Object>> favorites(HttpSession session){
        String sql="""
            SELECT m.movie_id,m.title,m.tmdb_id,m.imdb_id,COALESCE(AVG(dr.rating),0) AS average_rating,
                   COUNT(DISTINCT dr.user_id) AS rating_count,COALESCE(string_agg(DISTINCT g.genre,'|'),'') AS genres
            FROM favorites f JOIN movies m USING(movie_id) LEFT JOIN dataset_ratings dr ON dr.movie_id=m.movie_id
            LEFT JOIN movie_genres g ON g.movie_id=m.movie_id WHERE f.user_id=?
            GROUP BY m.movie_id,m.title,m.tmdb_id,m.imdb_id ORDER BY max(f.created_at) DESC
            """;
        return movieRows(sql,UUID.fromString(user(session)));
    }
    @PostMapping("/favorites/{movieId}") public Map<String,Object> addFavorite(@PathVariable int movieId,HttpSession session){
        String uid=user(session); jdbc.update("INSERT INTO favorites(user_id,movie_id) SELECT ?,movie_id FROM movies WHERE movie_id=? ON CONFLICT DO NOTHING",UUID.fromString(uid),movieId);return Map.of("movie_id",movieId,"saved",true);
    }
    @DeleteMapping("/favorites/{movieId}") public Map<String,Object> removeFavorite(@PathVariable int movieId,HttpSession session){jdbc.update("DELETE FROM favorites WHERE user_id=? AND movie_id=?",UUID.fromString(user(session)),movieId);return Map.of("movie_id",movieId,"saved",false);}
    @GetMapping("/watchlist") public List<Map<String,Object>> watchlist(HttpSession session){
        String sql="SELECT m.movie_id,m.title,m.tmdb_id,m.imdb_id,COALESCE(AVG(dr.rating),0) AS average_rating,COUNT(DISTINCT dr.user_id) AS rating_count,COALESCE(string_agg(DISTINCT g.genre,'|'),'') AS genres FROM watchlist f JOIN movies m USING(movie_id) LEFT JOIN dataset_ratings dr ON dr.movie_id=m.movie_id LEFT JOIN movie_genres g ON g.movie_id=m.movie_id WHERE f.user_id=? GROUP BY m.movie_id,m.title,m.tmdb_id,m.imdb_id ORDER BY max(f.created_at) DESC";
        return movieRows(sql,UUID.fromString(user(session)));
    }
    @PostMapping("/watchlist/{movieId}") public Map<String,Object> addToWatchlist(@PathVariable int movieId,HttpSession session){jdbc.update("INSERT INTO watchlist(user_id,movie_id) SELECT ?,movie_id FROM movies WHERE movie_id=? ON CONFLICT DO NOTHING",UUID.fromString(user(session)),movieId);return Map.of("movie_id",movieId,"saved",true);}
    @DeleteMapping("/watchlist/{movieId}") public Map<String,Object> removeFromWatchlist(@PathVariable int movieId,HttpSession session){jdbc.update("DELETE FROM watchlist WHERE user_id=? AND movie_id=?",UUID.fromString(user(session)),movieId);return Map.of("movie_id",movieId,"saved",false);}
    @PostMapping("/tags/{movieId}") public Map<String,Object> addTag(@PathVariable int movieId,@RequestBody Map<String,String> body,HttpSession session){
        String tag=body.getOrDefault("tag","").trim();if(tag.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tag vacío");
        jdbc.update("INSERT INTO user_tags(user_id,movie_id,tag) SELECT ?,movie_id,? FROM movies WHERE movie_id=? ON CONFLICT DO NOTHING",UUID.fromString(user(session)),tag,movieId);
        return Map.of("movie_id",movieId,"tag",tag);
    }
    @GetMapping("/tags") public List<Map<String,Object>> tags(HttpSession session){
        return jdbc.query("SELECT movie_id,tag FROM user_tags WHERE user_id=? ORDER BY created_at DESC",
                (rs,row)->Map.of("movie_id",rs.getInt("movie_id"),"tag",rs.getString("tag")),UUID.fromString(user(session)));
    }
    private List<Map<String,Object>> movieRows(String sql,UUID userId){
        return jdbc.query(sql,(rs,row)->{
            Map<String,Object> result=new LinkedHashMap<>();
            result.put("movie_id",rs.getInt("movie_id"));result.put("title",rs.getString("title"));
            result.put("tmdb_id",rs.getObject("tmdb_id"));result.put("imdb_id",rs.getString("imdb_id"));
            result.put("average_rating",rs.getDouble("average_rating"));result.put("rating_count",rs.getLong("rating_count"));
            String genres=rs.getString("genres");result.put("genres",genres.isBlank()?List.of():Arrays.asList(genres.split("\\|")));
            try { result.put("user_rating",rs.getDouble("user_rating")); } catch(java.sql.SQLException ignored) {}
            return result;
        },userId);
    }
}
