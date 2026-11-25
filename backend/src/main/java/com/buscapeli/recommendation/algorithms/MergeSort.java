package com.buscapeli.recommendation.algorithms;

import java.util.*;

public final class MergeSort {
    private MergeSort() {}
    public static <T> List<T> sort(List<T> input, Comparator<T> comparator) {
        if (input.size() < 2) return new ArrayList<>(input);
        int middle = input.size() / 2;
        List<T> left = sort(input.subList(0, middle), comparator);
        List<T> right = sort(input.subList(middle, input.size()), comparator);
        List<T> result = new ArrayList<>(input.size());
        int i = 0, j = 0;
        while (i < left.size() && j < right.size())
            result.add(comparator.compare(left.get(i), right.get(j)) <= 0 ? left.get(i++) : right.get(j++));
        result.addAll(left.subList(i, left.size())); result.addAll(right.subList(j, right.size()));
        return result;
    }
}
