package com.buscapeli.movie;

import com.buscapeli.tmdb.TmdbService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService movies;
    private final TmdbService tmdb;
    public MovieController(MovieService movies, TmdbService tmdb) { this.movies=movies; this.tmdb=tmdb; }

    @GetMapping
    public Map<String,Object> search(@RequestParam(required=false) String search,
            @RequestParam(required=false) String genre, @RequestParam(defaultValue="title") String sort,
            @RequestParam(defaultValue="1") @Min(1) int page,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int pageSize,
            @RequestParam(defaultValue="1") @Min(1) int minRatings,
            @RequestParam(required=false) Integer year) {
        if (!java.util.Set.of("title","popular","top_rated").contains(sort)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"sort inválido");
        return movies.search(search,genre,sort,page,pageSize,minRatings,year);
    }
    @GetMapping("/popular") public Map<String,Object> popular(@RequestParam(defaultValue="20") @Min(1) @Max(100) int limit) { return movies.search(null,null,"popular",1,limit,1); }
    @GetMapping("/top-rated") public Map<String,Object> topRated(@RequestParam(defaultValue="20") @Min(1) @Max(100) int limit,@RequestParam(defaultValue="20") @Min(1) int minRatings) { return movies.search(null,null,"top_rated",1,limit,minRatings); }
    @GetMapping("/{id}") public MovieDto get(@PathVariable int id) { return movies.get(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Película no encontrada")); }
    @GetMapping("/{id}/details") public Map<String,Object> details(@PathVariable int id) { return tmdb.details(movies.get(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Película no encontrada"))); }
    @GetMapping("/{id}/poster") public Map<String,Object> poster(@PathVariable int id) { return tmdb.poster(movies.get(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Película no encontrada"))); }
}
