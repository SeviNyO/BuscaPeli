package com.buscapeli.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {
    private final JdbcTemplate jdbc;
    public HealthController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping("/api/v1/health") public Map<String,Object> health(){
        return Map.of("status","ok","movies",jdbc.queryForObject("SELECT count(*) FROM movies",Integer.class),
                "ratings",jdbc.queryForObject("SELECT count(*) FROM dataset_ratings",Integer.class));
    }
}
