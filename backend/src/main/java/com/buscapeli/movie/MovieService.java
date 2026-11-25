package com.buscapeli.movie;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class MovieService {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern YEAR = Pattern.compile("\\s*\\(\\d{4}\\)$");
    private final MovieRepository repository;
    public MovieService(MovieRepository repository) { this.repository = repository; }

    public Map<String,Object> search(String search, String genre, String sort, int page, int size, int minRatings) {
        return search(search,genre,sort,page,size,minRatings,null);
    }
    public Map<String,Object> search(String search, String genre, String sort, int page, int size, int minRatings,Integer year) {
        List<MovieDto> all = repository.findAll();
        String query = normalize(search == null ? "" : search);
        String selectedGenre = normalize(GenreNames.toEnglish(genre == null ? "" : genre));
        List<MovieDto> results = all.stream()
                .filter(movie -> selectedGenre.isBlank() || movie.genres().stream().anyMatch(g -> normalize(g).equals(selectedGenre)))
                .filter(movie -> year==null || year.equals(movie.year()))
                .toList();
        List<MovieDto> direct = query.isBlank() ? new ArrayList<>(results) : results.stream()
                .filter(movie -> normalize(movie.title()).contains(query)).toList();
        boolean suggested = false;
        if (!query.isBlank() && direct.isEmpty() && query.length() >= 4) {
            int threshold = Math.max(1, query.length() / 5);
            direct = results.stream().filter(movie -> levenshtein(query, normalize(YEAR.matcher(movie.title()).replaceFirst(""))) <= threshold)
                    .sorted(Comparator.comparingInt(movie -> levenshtein(query, normalize(YEAR.matcher(movie.title()).replaceFirst("")))))
                    .limit(10).toList();
            suggested = !direct.isEmpty();
        }
        List<MovieDto> ordered = new ArrayList<>(direct);
        if (sort.equals("popular")) ordered.sort(Comparator.comparingLong(MovieDto::rating_count).reversed());
        else if (sort.equals("top_rated")) ordered.removeIf(movie -> movie.rating_count() < minRatings);
        if (sort.equals("top_rated")) ordered.sort(Comparator.comparingDouble(MovieDto::average_rating).reversed().thenComparing(Comparator.comparingLong(MovieDto::rating_count).reversed()));
        else if (!sort.equals("popular")) ordered.sort(Comparator.comparing(MovieDto::title, String.CASE_INSENSITIVE_ORDER));
        int from = Math.min((page - 1) * size, ordered.size());
        int to = Math.min(from + size, ordered.size());
        return Map.of("items", ordered.subList(from,to), "page", page, "page_size", size, "total", ordered.size(), "suggested", suggested);
    }

    public Optional<MovieDto> get(int id) { return repository.findById(id); }
    public List<MovieDto> all() { return repository.findAll(); }
    public static String normalize(String value) {
        return DIACRITICS.matcher(Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD))
                .replaceAll("").trim().replaceAll("\\s+", " ");
    }
    private static int levenshtein(String a, String b) {
        int[] previous = new int[b.length() + 1], current = new int[b.length() + 1];
        for (int j=0;j<=b.length();j++) previous[j]=j;
        for (int i=1;i<=a.length();i++) {
            current[0]=i;
            for (int j=1;j<=b.length();j++) current[j]=Math.min(Math.min(current[j-1]+1,previous[j]+1),previous[j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1));
            int[] tmp=previous; previous=current; current=tmp;
        }
        return previous[b.length()];
    }
}
