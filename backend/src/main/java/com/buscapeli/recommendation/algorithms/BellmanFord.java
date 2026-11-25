package com.buscapeli.recommendation.algorithms;

import java.util.*;
import java.util.function.Predicate;

public final class BellmanFord {
    private BellmanFord() {}
    public static Map<Node,Double> shortestPaths(BipartiteGraph graph, Node source, Set<Node> allowed,
                                                 Predicate<Double> edgeFilter) {
        Map<Node,Double> distance = new HashMap<>();
        for (Node node : allowed) distance.put(node, Double.POSITIVE_INFINITY);
        if (!allowed.contains(source)) return distance;
        distance.put(source, 0.0);
        for (int i = 1; i < allowed.size(); i++) {
            boolean changed = false;
            for (Node from : allowed) {
                double base = distance.get(from);
                if (!Double.isFinite(base)) continue;
                for (var edge : graph.adjacency().getOrDefault(from, Map.of()).entrySet()) {
                    if (allowed.contains(edge.getKey()) && edgeFilter.test(edge.getValue())
                            && base + edge.getValue() < distance.get(edge.getKey())) {
                        distance.put(edge.getKey(), base + edge.getValue()); changed = true;
                    }
                }
            }
            if (!changed) break;
        }
        return distance;
    }
}
