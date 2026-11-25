package com.buscapeli.movie;

import java.util.List;

public record MovieDto(int movie_id, String title, Integer year, List<String> genres,
                       Integer tmdb_id, String imdb_id, double average_rating,
                       long rating_count, String poster_url) {}
