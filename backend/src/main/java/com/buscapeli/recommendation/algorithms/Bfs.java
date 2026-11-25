package com.buscapeli.recommendation.algorithms;

import java.util.*;
import java.util.function.Predicate;

public final class Bfs {
    private Bfs() {}
    public static Map<Node, Integer> traverse(BipartiteGraph graph, Node start, int maxDepth,
                                               Predicate<Double> edgeFilter) {
        Map<Node, Integer> distance = new HashMap<>();
        if (!graph.adjacency().containsKey(start)) return distance;
        Queue<Node> queue = new ArrayDeque<>();
        distance.put(start, 0); queue.add(start);
        while (!queue.isEmpty()) {
            Node current = queue.remove();
            if (distance.get(current) >= maxDepth) continue;
            for (var edge : graph.adjacency().getOrDefault(current, Map.of()).entrySet()) {
                if (!edgeFilter.test(edge.getValue()) || distance.containsKey(edge.getKey())) continue;
                distance.put(edge.getKey(), distance.get(current) + 1);
                queue.add(edge.getKey());
            }
        }
        return distance;
    }
}
