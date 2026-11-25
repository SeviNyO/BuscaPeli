package com.buscapeli.recommendation;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recommendations")
public class PersonalizedRecommendationController {
    private final RecommendationEngine engine;
    public PersonalizedRecommendationController(RecommendationEngine engine){this.engine=engine;}
    @GetMapping("/me") public Map<String,Object> personalized(@RequestParam(defaultValue="20") int limit,HttpSession session){
        Object userId=session.getAttribute("userId");
        if(userId==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Inicia sesión");
        return engine.personalized(UUID.fromString(userId.toString()),limit);
    }
}
