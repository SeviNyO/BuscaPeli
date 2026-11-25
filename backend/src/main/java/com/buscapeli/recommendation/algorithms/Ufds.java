package com.buscapeli.recommendation.algorithms;

import java.util.*;

public class Ufds<T> {
    private final Map<T,T> parent = new HashMap<>();
    private final Map<T,Integer> rank = new HashMap<>();
    private int components;
    public Ufds(Collection<T> elements) {
        for (T element : elements) { parent.put(element, element); rank.put(element, 0); }
        components = parent.size();
    }
    public T find(T item) {
        T p = parent.get(item);
        if (p == null) throw new IllegalArgumentException("Unknown UFDS element: " + item);
        if (!p.equals(item)) parent.put(item, find(p));
        return parent.get(item);
    }
    public boolean union(T first, T second) {
        T a = find(first), b = find(second);
        if (a.equals(b)) return false;
        if (rank.get(a) < rank.get(b)) { T tmp = a; a = b; b = tmp; }
        parent.put(b, a);
        if (rank.get(a).equals(rank.get(b))) rank.put(a, rank.get(a) + 1);
        components--;
        return true;
    }
    public int components() { return components; }
    public List<Set<T>> groups() {
        Map<T,Set<T>> groups = new HashMap<>();
        for (T item : parent.keySet()) groups.computeIfAbsent(find(item), ignored -> new HashSet<>()).add(item);
        return new ArrayList<>(groups.values());
    }
}
