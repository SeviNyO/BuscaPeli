package com.buscapeli.recommendation;

import com.buscapeli.movie.*;
import com.buscapeli.recommendation.algorithms.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationEngine {
    private final JdbcTemplate jdbc;
    private final MovieService movies;
    private final int minCommon;
    private final int targetCommunities;
    private final double minRating;
    private BipartiteGraph userMovie = new BipartiteGraph();
    private BipartiteGraph movieGenre = new BipartiteGraph();
    private Map<Integer,Integer> communityByUser = Map.of();

    public RecommendationEngine(JdbcTemplate jdbc, MovieService movies,
            @Value("${app.min-common-movies}") int minCommon,
            @Value("${app.target-communities}") int targetCommunities,
            @Value("${app.min-recommendation-rating}") double minRating) {
        this.jdbc=jdbc; this.movies=movies; this.minCommon=minCommon;
        this.targetCommunities=targetCommunities; this.minRating=minRating;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void precomputeGlobalGraphsAndCommunities() {
        BipartiteGraph usersMovies = new BipartiteGraph();
        for (Map<String,Object> row : jdbc.queryForList("SELECT user_id,movie_id,rating FROM dataset_ratings")) {
            int user=((Number)row.get("user_id")).intValue(), movie=((Number)row.get("movie_id")).intValue();
            double rating=((Number)row.get("rating")).doubleValue();
            usersMovies.addEdge(Node.user(user),Node.movie(movie),5.5-rating);
        }
        BipartiteGraph moviesGenres = new BipartiteGraph();
        for (Map<String,Object> row : jdbc.queryForList("SELECT movie_id,genre FROM movie_genres"))
            moviesGenres.addEdge(Node.movie(((Number)row.get("movie_id")).intValue()),Node.genre((String)row.get("genre")),1);
        this.userMovie=usersMovies; this.movieGenre=moviesGenres;
        this.communityByUser=buildGlobalCommunities();
        System.out.printf("Grafos listos: %d nodos usuario-película, %d comunidades globales.%n",userMovie.nodes().size(),new HashSet<>(communityByUser.values()).size());
    }

    private Map<Integer,Integer> buildGlobalCommunities() {
        Map<Integer,Set<Integer>> likedByUser=new HashMap<>();
        Map<Integer,List<Integer>> usersByLikedMovie=new HashMap<>();
        Set<Integer> users=jdbc.query("SELECT user_id FROM dataset_users",(rs,row)->rs.getInt(1)).stream().collect(Collectors.toSet());
        jdbc.query("SELECT user_id,movie_id FROM dataset_ratings WHERE rating>=?",rs->{
            int user=rs.getInt("user_id"), movie=rs.getInt("movie_id");
            likedByUser.computeIfAbsent(user,ignored->new HashSet<>()).add(movie);
            usersByLikedMovie.computeIfAbsent(movie,ignored->new ArrayList<>()).add(user);
        },minRating);
        Map<UserPair,Integer> common=new HashMap<>();
        for(List<Integer> viewers:usersByLikedMovie.values()) {
            List<Integer> unique=viewers.stream().distinct().sorted().toList();
            for(int i=0;i<unique.size();i++) for(int j=i+1;j<unique.size();j++) common.merge(new UserPair(unique.get(i),unique.get(j)),1,Integer::sum);
        }
        List<Node> nodes=users.stream().map(Node::user).toList();
        List<Kruskal.Edge<Node>> edges=new ArrayList<>();
        common.forEach((pair,count)->{if(count>=minCommon) edges.add(new Kruskal.Edge<>(Node.user(pair.a),Node.user(pair.b),1.0/count));});
        Kruskal.Forest<Node> forest=Kruskal.minimumSpanningForest(nodes,edges,targetCommunities);
        Map<Integer,Integer> membership=new HashMap<>(); int groupId=0;
        for(Set<Node> group:forest.groups()) { for(Node node:group) membership.put(Integer.parseInt(node.id()),groupId); groupId++; }
        return Map.copyOf(membership);
    }

    public Map<String,Object> collaborative(int movieId,int limit,double threshold) {
        MovieDto seed=movies.get(movieId).orElse(null); if(seed==null) return null;
        Node start=Node.movie(movieId);
        var allowedRating=(java.util.function.Predicate<Double>)(cost->cost<=5.5-threshold);
        Map<Node,Integer> hops=Bfs.traverse(userMovie,start,2,allowedRating);
        Set<Integer> seedUsers=userMovie.adjacency().getOrDefault(start,Map.of()).entrySet().stream()
                .filter(e->e.getKey().type().equals("user")&&allowedRating.test(e.getValue()))
                .map(e->Integer.parseInt(e.getKey().id())).collect(Collectors.toSet());
        Set<Integer> communities=seedUsers.stream().map(communityByUser::get).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> peers=communityByUser.entrySet().stream().filter(e->communities.contains(e.getValue())).map(Map.Entry::getKey).collect(Collectors.toSet());
        if(peers.isEmpty()) peers=seedUsers;
        Set<Integer> candidates=hops.entrySet().stream().filter(e->e.getKey().type().equals("movie")&&e.getValue()==2)
                .map(e->Integer.parseInt(e.getKey().id())).collect(Collectors.toSet());
        Map<Node,Double> costs=BellmanFord.shortestPaths(userMovie,start,hops.keySet(),allowedRating);
        Map<Integer,List<Map<String,Object>>> peerRatings=new HashMap<>();
        for(Map<String,Object> row:jdbc.queryForList("SELECT user_id,movie_id,rating FROM dataset_ratings WHERE rating>=?",threshold)) {
            int user=((Number)row.get("user_id")).intValue();
            if(peers.contains(user)) peerRatings.computeIfAbsent(((Number)row.get("movie_id")).intValue(),ignored->new ArrayList<>()).add(row);
        }
        Map<Integer,MovieDto> byId=movies.all().stream().collect(Collectors.toMap(MovieDto::movie_id,m->m));
        List<Map<String,Object>> results=new ArrayList<>();
        for(int candidate:candidates) {
            List<Map<String,Object>> ratings=peerRatings.getOrDefault(candidate,List.of());
            if(ratings.isEmpty()) continue;
            double avg=ratings.stream().mapToDouble(r->((Number)r.get("rating")).doubleValue()).average().orElse(0);
            Map<String,Object> item=new LinkedHashMap<>(); item.put("movie",byId.get(candidate));
            item.put("recommendation_score",Math.round(avg*1000.0)/1000.0); item.put("recommended_by_users",ratings.size());
            item.put("affinity_cost",costs.getOrDefault(Node.movie(candidate),Double.POSITIVE_INFINITY));
            item.put("reason","similar_users"); results.add(item);
        }
        List<Map<String,Object>> ranked=MergeSort.sort(results,Comparator
                .comparingDouble((Map<String,Object> r)->(double)r.get("affinity_cost"))
                .thenComparing(Comparator.comparingInt((Map<String,Object> r)->(int)r.get("recommended_by_users")).reversed())
                .thenComparing(r->((MovieDto)r.get("movie")).title(),String.CASE_INSENSITIVE_ORDER));
        return Map.of("movie",seed,"items",GreedyTopN.select(ranked,limit),"total",ranked.size(),
                "algorithm",List.of("BFS","Kruskal","UFDS","Bellman-Ford","Merge Sort","Greedy Top-N"),"communities_used",communities.size());
    }

    public Map<String,Object> byGenre(int movieId,int limit) {
        MovieDto seed=movies.get(movieId).orElse(null); if(seed==null) return null;
        Map<Node,Integer> hops=Bfs.traverse(movieGenre,Node.movie(movieId),2,ignored->true);
        Map<Integer,MovieDto> byId=movies.all().stream().collect(Collectors.toMap(MovieDto::movie_id,m->m));
        List<Map<String,Object>> results=new ArrayList<>();
        for(var hit:hops.entrySet()) if(hit.getKey().type().equals("movie")&&hit.getValue()==2&& !hit.getKey().equals(Node.movie(movieId))) {
            int id=Integer.parseInt(hit.getKey().id()); MovieDto candidate=byId.get(id);
            List<String> common=seed.genres().stream().filter(candidate.genres()::contains).toList();
            if(!common.isEmpty()) results.add(Map.of("movie",candidate,"shared_genres",common,"reason","same_genre"));
        }
        List<Map<String,Object>> ranked=MergeSort.sort(results,Comparator
                .comparingInt((Map<String,Object> r)->((List<?>)r.get("shared_genres")).size()).reversed()
                .thenComparing(Comparator.comparingDouble((Map<String,Object> r)->((MovieDto)r.get("movie")).average_rating()).reversed()));
        return Map.of("movie",seed,"items",GreedyTopN.select(ranked,limit),"total",ranked.size(),"algorithm",List.of("BFS","Merge Sort","Greedy Top-N"));
    }

    public Map<String,Object> personalized(UUID appUserId,int limit) {
        List<Integer> seeds=jdbc.query("SELECT movie_id FROM user_ratings WHERE user_id=? AND rating>=? ORDER BY rating DESC LIMIT 5",
                (rs,row)->rs.getInt(1),appUserId,minRating);
        if(seeds.isEmpty()) return Map.of("items",movies.search(null,null,"popular",1,limit,1).get("items"),"total",0,"cold_start",true);
        Map<Integer,Map<String,Object>> best=new HashMap<>();
        for(int seedId:seeds) {
            Map<String,Object> response=collaborative(seedId,Math.max(limit,20),minRating);
            if(response==null) continue;
            for(Object raw:(List<?>)response.get("items")) {
                Map<?,?> item=(Map<?,?>)raw; MovieDto movie=(MovieDto)item.get("movie");
                if(seeds.contains(movie.movie_id())) continue;
                Map<String,Object> existing=best.get(movie.movie_id());
                if(existing==null || ((Number)item.get("affinity_cost")).doubleValue()<((Number)existing.get("affinity_cost")).doubleValue())
                    best.put(movie.movie_id(),new LinkedHashMap<>((Map<String,Object>)item));
            }
        }
        List<Map<String,Object>> ranked=MergeSort.sort(new ArrayList<>(best.values()),Comparator
                .comparingDouble((Map<String,Object> item)->((Number)item.get("affinity_cost")).doubleValue())
                .thenComparing(Comparator.comparingInt((Map<String,Object> item)->((Number)item.get("recommended_by_users")).intValue()).reversed()));
        return Map.of("items",GreedyTopN.select(ranked,limit),"total",ranked.size(),"cold_start",false,
                "algorithm",List.of("BFS","Kruskal","UFDS","Bellman-Ford","Merge Sort","Greedy Top-N"));
    }
    private record UserPair(int a,int b) {}
}
