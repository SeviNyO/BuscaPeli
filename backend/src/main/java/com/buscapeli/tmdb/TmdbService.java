package com.buscapeli.tmdb;

import com.buscapeli.movie.MovieDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TmdbService {
    private final String apiKey;
    private final ObjectMapper mapper;
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Map<Integer,Map<String,Object>> cache=new ConcurrentHashMap<>();
    public TmdbService(@Value("${app.tmdb-api-key}") String apiKey,ObjectMapper mapper) { this.apiKey=apiKey; this.mapper=mapper; }

    public Map<String,Object> poster(MovieDto movie) {
        if(movie.tmdb_id()==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"La película no tiene tmdbId asociado");
        Map<String,Object> metadata=metadata(movie);
        return Map.of("movie_id",movie.movie_id(),"tmdb_id",movie.tmdb_id(),"poster_url",metadata.getOrDefault("poster_url",""));
    }
    public Map<String,Object> details(MovieDto movie) {
        Map<String,Object> result=new LinkedHashMap<>(); result.put("movie",movie); result.putAll(metadata(movie)); return result;
    }
    private Map<String,Object> metadata(MovieDto movie) {
        if(movie.tmdb_id()==null) return Map.of("overview","","poster_url","","backdrop_url","","cast",List.of());
        return cache.computeIfAbsent(movie.tmdb_id(),ignored->fetch(movie.tmdb_id()));
    }
    private Map<String,Object> fetch(int tmdbId) {
        if(apiKey==null||apiKey.isBlank()) return Map.of("overview","","poster_url","","backdrop_url","","cast",List.of());
        try {
            String key=URLEncoder.encode(apiKey,StandardCharsets.UTF_8);
            String base="https://api.themoviedb.org/3/movie/"+tmdbId+"?api_key="+key+"&append_to_response=credits";
            HttpRequest request=HttpRequest.newBuilder(URI.create(base)).timeout(Duration.ofSeconds(8)).GET().build();
            HttpResponse<String> response=client.send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()!=200) return Map.of("overview","","poster_url","","backdrop_url","","cast",List.of());
            JsonNode root=mapper.readTree(response.body());
            List<Map<String,String>> cast=new ArrayList<>();
            JsonNode castNodes=root.path("credits").path("cast");
            for(int i=0;i<Math.min(8,castNodes.size());i++) cast.add(Map.of("name",castNodes.get(i).path("name").asText(),"character",castNodes.get(i).path("character").asText()));
            return Map.of("overview",root.path("overview").asText(""),"poster_url",image(root.path("poster_path").asText("")),
                    "backdrop_url",image(root.path("backdrop_path").asText("")),"release_date",root.path("release_date").asText(""),
                    "runtime",root.path("runtime").asInt(0),"cast",cast);
        } catch(Exception ignored) {
            return Map.of("overview","","poster_url","","backdrop_url","","cast",List.of());
        }
    }
    private static String image(String path) { return path==null||path.isBlank()?"":"https://image.tmdb.org/t/p/w500"+path; }
}
