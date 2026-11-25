package com.buscapeli.recommendation.algorithms;

public record Node(String type, String id) {
    public static Node user(int id) { return new Node("user", Integer.toString(id)); }
    public static Node movie(int id) { return new Node("movie", Integer.toString(id)); }
    public static Node genre(String name) { return new Node("genre", name); }
}
