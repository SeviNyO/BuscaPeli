from collections import defaultdict
from itertools import combinations
from uuid import UUID

from app.algorithms.bellman_ford import bellman_ford
from app.algorithms.graph import Graph, bfs
from app.algorithms.greedy import greedy_top_n
from app.algorithms.kruskal import Edge, kruskal
from app.algorithms.merge_sort import merge_sort
from app.database import fetch_all, fetch_one
from app.movies import all_movies, get_movie, search_movies


class RecommendationEngine:
    def __init__(self):
        self.user_movie = Graph()
        self.movie_genre = Graph()
        self.community_by_user = {}
        self.min_rating = 4.0

    def precompute(self, min_common: int, target_communities: int, min_rating: float):
        self.min_rating = min_rating
        self.user_movie = Graph()
        for row in fetch_all("SELECT user_id,movie_id,rating FROM dataset_ratings"):
            rating = float(row["rating"])
            self.user_movie.add_edge(("movie", row["movie_id"]), ("user", row["user_id"]), 5.5 - rating, True)
        self.movie_genre = Graph()
        for row in fetch_all("SELECT movie_id,genre FROM movie_genres"):
            self.movie_genre.add_edge(("movie", row["movie_id"]), ("genre", row["genre"]), 1, True)
        self.community_by_user = self._communities(min_common, target_communities, min_rating)

    def _communities(self, min_common, target_communities, min_rating):
        users = [row["user_id"] for row in fetch_all("SELECT user_id FROM dataset_users")]
        viewers_by_movie = defaultdict(list)
        for row in fetch_all("SELECT user_id,movie_id FROM dataset_ratings WHERE rating >= %s", (min_rating,)):
            viewers_by_movie[row["movie_id"]].append(row["user_id"])
        common_movies = defaultdict(int)
        for viewers in viewers_by_movie.values():
            for first, second in combinations(sorted(set(viewers)), 2):
                common_movies[(first, second)] += 1
        user_nodes = [("user", user_id) for user_id in users]
        edges = [Edge(("user", first), ("user", second), 1 / shared)
                 for (first, second), shared in common_movies.items() if shared >= min_common]
        _, groups = kruskal(user_nodes, edges, target_communities)
        return {user_id: group_id for group_id, group in enumerate(groups)
                for kind, user_id in group if kind == "user"}

    def collaborative(self, movie_id: int, limit: int = 12, threshold: float = 4.0):
        seed = get_movie(movie_id)
        if seed is None:
            return None
        start = ("movie", movie_id)
        allowed_edge = lambda cost: cost <= 5.5 - threshold
        distances, _ = bfs(self.user_movie, start, 2, allowed_edge)
        seed_users = {node[1] for node, depth in distances.items()
                      if depth == 1 and node[0] == "user"}
        communities = {self.community_by_user[user] for user in seed_users if user in self.community_by_user}
        peers = {user for user, group in self.community_by_user.items() if group in communities}
        if not peers:
            peers = seed_users
        candidates = {node[1] for node, depth in distances.items()
                      if depth == 2 and node[0] == "movie" and node[1] != movie_id}
        costs = bellman_ford(self.user_movie, start, set(distances), allowed_edge)
        peer_ratings = fetch_all(
            "SELECT user_id,movie_id,rating FROM dataset_ratings WHERE user_id = ANY(%s) AND rating >= %s",
            (list(peers), threshold)) if peers else []
        by_candidate = defaultdict(list)
        for row in peer_ratings:
            by_candidate[row["movie_id"]].append(float(row["rating"]))
        movies = {movie["movie_id"]: movie for movie in all_movies()}
        results = []
        for candidate in candidates:
            ratings = by_candidate.get(candidate, [])
            if not ratings or candidate not in movies:
                continue
            results.append({"movie": movies[candidate], "recommendation_score": round(sum(ratings) / len(ratings), 3),
                            "recommended_by_users": len(ratings), "affinity_cost": costs.get(("movie", candidate), float("inf")),
                            "reason": "similar_users"})
        ranked = merge_sort(results, key=lambda item: (item["affinity_cost"], -item["recommended_by_users"],
                                                        item["movie"]["title"].casefold()))
        return {"movie": seed, "items": greedy_top_n(ranked, limit), "total": len(ranked),
                "algorithm": ["BFS", "Kruskal", "UFDS", "Bellman-Ford", "Merge Sort", "Greedy Top-N"],
                "communities_used": len(communities)}

    def by_genre(self, movie_id: int, limit: int = 12):
        seed = get_movie(movie_id)
        if seed is None:
            return None
        distances, _ = bfs(self.movie_genre, ("movie", movie_id), 2)
        movies = {movie["movie_id"]: movie for movie in all_movies()}
        results = []
        for node, depth in distances.items():
            if node[0] != "movie" or depth != 2 or node[1] == movie_id or node[1] not in movies:
                continue
            candidate = movies[node[1]]
            shared = sorted(set(seed["genres"]) & set(candidate["genres"]))
            if shared:
                results.append({"movie": candidate, "shared_genres": shared, "reason": "same_genre"})
        ranked = merge_sort(results, key=lambda item: (-len(item["shared_genres"]),
                                                        -item["movie"]["average_rating"], item["movie"]["title"].casefold()))
        return {"movie": seed, "items": greedy_top_n(ranked, limit), "total": len(ranked),
                "algorithm": ["BFS", "Merge Sort", "Greedy Top-N"]}

    def personalized(self, user_id: UUID, limit: int = 20):
        seeds = fetch_all("""
            SELECT movie_id FROM user_ratings WHERE user_id=%s AND rating >= %s
            ORDER BY rating DESC, updated_at DESC LIMIT 5
        """, (user_id, self.min_rating))
        if not seeds:
            return {"items": search_movies(sort="popular", page_size=limit)["items"], "total": 0, "cold_start": True}
        seed_ids = {row["movie_id"] for row in seeds}
        best = {}
        for row in seeds:
            response = self.collaborative(row["movie_id"], max(limit, 20), self.min_rating)
            if not response:
                continue
            for item in response["items"]:
                movie_id = item["movie"]["movie_id"]
                if movie_id not in seed_ids and (movie_id not in best or item["affinity_cost"] < best[movie_id]["affinity_cost"]):
                    best[movie_id] = item
        ranked = merge_sort(list(best.values()), key=lambda item: (
            item["affinity_cost"], -item["recommended_by_users"], item["movie"]["title"].casefold()))
        return {"items": greedy_top_n(ranked, limit), "total": len(ranked), "cold_start": False,
                "algorithm": ["BFS", "Kruskal", "UFDS", "Bellman-Ford", "Merge Sort", "Greedy Top-N"]}
