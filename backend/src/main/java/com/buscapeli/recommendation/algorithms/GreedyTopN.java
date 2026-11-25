package com.buscapeli.recommendation.algorithms;

import java.util.ArrayList;
import java.util.List;

public final class GreedyTopN {
    private GreedyTopN() {}
    public static <T> List<T> select(List<T> ranked, int count) {
        List<T> result=new ArrayList<>(Math.min(Math.max(count,0),ranked.size()));
        for(T item:ranked) { if(result.size()>=count) break; result.add(item); }
        return result;
    }
}
