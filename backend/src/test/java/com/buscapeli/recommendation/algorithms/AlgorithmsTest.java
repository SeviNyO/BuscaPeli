package com.buscapeli.recommendation.algorithms;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AlgorithmsTest {
    @Test void bfsFindsTheTwoHopMovieCandidates() {
        BipartiteGraph graph=new BipartiteGraph();
        Node movie=Node.movie(1), user=Node.user(7), candidate=Node.movie(2);
        graph.addEdge(movie,user,1.0); graph.addEdge(user,candidate,1.5);
        assertEquals(2,Bfs.traverse(graph,movie,2,w->true).get(candidate));
    }

    @Test void ufdsAndKruskalJoinUsersWithoutCycles() {
        List<String> users=List.of("u1","u2","u3");
        var edges=new ArrayList<Kruskal.Edge<String>>();
        edges.add(new Kruskal.Edge<>("u1","u2",0.25));
        edges.add(new Kruskal.Edge<>("u2","u3",0.5));
        edges.add(new Kruskal.Edge<>("u1","u3",0.75));
        var result=Kruskal.minimumSpanningForest(users,edges,1);
        assertEquals(2,result.edges().size());
        assertEquals(1,result.groups().size());
    }

    @Test void bellmanFordPrefersLowerRatingCostPath() {
        BipartiteGraph graph=new BipartiteGraph();
        Node source=Node.movie(1), user=Node.user(1), candidate=Node.movie(2);
        graph.addEdge(source,user,0.5); graph.addEdge(user,candidate,1.5);
        Map<Node,Double> result=BellmanFord.shortestPaths(graph,source,graph.nodes(),w->true);
        assertEquals(2.0,result.get(candidate));
    }

    @Test void mergeSortAndGreedyChooseTheFirstNInRankOrder() {
        List<Integer> sorted=MergeSort.sort(List.of(4,1,3,2),Comparator.naturalOrder());
        assertEquals(List.of(1,2),GreedyTopN.select(sorted,2));
    }
}
