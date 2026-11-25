package com.buscapeli.movie;

import java.util.Map;

public final class GenreNames {
    private GenreNames() {}
    private static final Map<String,String> ENGLISH = Map.ofEntries(
            Map.entry("acción","Action"), Map.entry("aventura","Adventure"), Map.entry("animación","Animation"),
            Map.entry("comedia","Comedy"), Map.entry("crimen","Crime"), Map.entry("documental","Documentary"),
            Map.entry("drama","Drama"), Map.entry("familia","Children"), Map.entry("fantasía","Fantasy"),
            Map.entry("cine negro","Film-Noir"), Map.entry("terror","Horror"), Map.entry("música","Musical"),
            Map.entry("misterio","Mystery"), Map.entry("romance","Romance"), Map.entry("ciencia ficción","Sci-Fi"),
            Map.entry("imax","IMAX"), Map.entry("suspenso","Thriller"), Map.entry("guerra","War"), Map.entry("western","Western"),
            Map.entry("todos",""));
    public static String toEnglish(String value) { return ENGLISH.getOrDefault(value.trim().toLowerCase(), value); }
    public static String toSpanish(String value) {
        return ENGLISH.entrySet().stream().filter(e->e.getValue().equalsIgnoreCase(value)).map(e->capitalize(e.getKey())).findFirst().orElse(value);
    }
    private static String capitalize(String value){return value.isEmpty()?value:value.substring(0,1).toUpperCase()+value.substring(1);}
}
