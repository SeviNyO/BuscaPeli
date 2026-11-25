package com.buscapeli.recommendation.algorithms;

import java.util.*;

public class BipartiteGraph {
    private final Map<Node, Map<Node, Double>> adjacency = new HashMap<>();
    public void addEdge(Node left, Node right, double weight) {
        adjacency.computeIfAbsent(left, ignored -> new HashMap<>()).put(right, weight);
        adjacency.computeIfAbsent(right, ignored -> new HashMap<>()).put(left, weight);
    }
    public Map<Node, Map<Node, Double>> adjacency() { return adjacency; }
    public Set<Node> nodes() { return adjacency.keySet(); }
}
