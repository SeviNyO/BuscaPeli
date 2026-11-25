package com.buscapeli.recommendation.algorithms;

import java.util.*;

public final class Kruskal {
    private Kruskal() {}
    public record Edge<T>(T first, T second, double weight) {}
    public record Forest<T>(List<Edge<T>> edges, List<Set<T>> groups) {}
    public static <T> Forest<T> minimumSpanningForest(Collection<T> nodes, List<Edge<T>> edges, int targetComponents) {
        Ufds<T> sets = new Ufds<>(nodes);
        List<Edge<T>> accepted = new ArrayList<>();
        edges.sort(Comparator.comparingDouble(Edge<T>::weight)
                .thenComparing(edge -> String.valueOf(edge.first()))
                .thenComparing(edge -> String.valueOf(edge.second())));
        for (Edge<T> edge : edges) {
            if (sets.components() <= targetComponents) break;
            if (sets.union(edge.first(), edge.second())) accepted.add(edge);
        }
        return new Forest<>(accepted, sets.groups());
    }
}
