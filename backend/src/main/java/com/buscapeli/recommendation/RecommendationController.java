package com.buscapeli.recommendation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recommendations/movies")
public class RecommendationController {
    private final RecommendationEngine engine;
    public RecommendationController(RecommendationEngine engine) { this.engine=engine; }
    @GetMapping("/{id}") public Map<String,Object> collaborative(@PathVariable int id,
            @RequestParam(defaultValue="12") int limit,@RequestParam(defaultValue="4.0") double minRating) {
        Map<String,Object> result=engine.collaborative(id,limit,minRating);
        if(result==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Película no encontrada");
        return result;
    }
    @GetMapping("/{id}/by-genre") public Map<String,Object> byGenre(@PathVariable int id,@RequestParam(defaultValue="12") int limit) {
        Map<String,Object> result=engine.byGenre(id,limit);
        if(result==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Película no encontrada");
        return result;
    }
}
