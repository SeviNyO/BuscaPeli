import unittest
from unittest.mock import patch

from app.algorithms.bellman_ford import bellman_ford
from app.algorithms.graph import Graph, bfs
from app.algorithms.greedy import greedy_top_n
from app.algorithms.kruskal import Edge, kruskal
from app.algorithms.merge_sort import merge_sort
from app.algorithms.ufds import UFDS
from app.movies import GENRES_EN, normalize, search_movies


class AlgorithmTests(unittest.TestCase):
    def test_adjacency_list_graph_and_bfs_two_hops(self):
        graph = Graph()
        graph.add_edge(("movie", 1), ("user", 5), 1.0, True)
        graph.add_edge(("user", 5), ("movie", 2), 1.5, True)
        distances, parents = bfs(graph, ("movie", 1), 2, lambda weight: weight <= 1.5)
        self.assertEqual(distances[("movie", 2)], 2)
        self.assertEqual(parents[("movie", 2)], ("user", 5))

    def test_ufds_path_compression_union_by_rank(self):
        sets = UFDS(4)
        self.assertTrue(sets.union(0, 1))
        self.assertTrue(sets.union(2, 3))
        self.assertTrue(sets.union(1, 3))
        self.assertFalse(sets.union(0, 2))
        self.assertEqual(sets.components, 1)

    def test_kruskal_stops_at_target_component_count(self):
        edges = [Edge("a", "b", 0.2), Edge("b", "c", 0.4), Edge("a", "c", 0.8)]
        accepted, groups = kruskal(["a", "b", "c"], edges, target_components=2)
        self.assertEqual(len(accepted), 1)
        self.assertEqual(len(groups), 2)

    def test_bellman_ford_relaxes_weighted_edges(self):
        graph = Graph()
        graph.add_edge("s", "a", 2)
        graph.add_edge("s", "b", 5)
        graph.add_edge("a", "b", 1)
        distances = bellman_ford(graph, "s")
        self.assertEqual(distances["b"], 3)

    def test_merge_sort_and_greedy_top_n(self):
        ranked = merge_sort([5, 1, 4, 2], key=lambda item: item)
        self.assertEqual(ranked, [1, 2, 4, 5])
        self.assertEqual(greedy_top_n(ranked, 2), [1, 2])

    def test_spanish_genre_mapping_and_fuzzy_title_search(self):
        self.assertEqual(GENRES_EN[normalize("Ciencia ficción")], "Sci-Fi")
        dataset = [{"movie_id": 1, "title": "Titanic (1997)", "genres": ["Drama", "Romance"],
                    "average_rating": 4.5, "rating_count": 100, "tmdb_id": 597, "imdb_id": "0120338"}]
        with patch("app.movies.all_movies", return_value=dataset):
            result = search_movies(search="titanik", genre="Todos")
        self.assertTrue(result["suggested"])
        self.assertEqual(result["items"][0]["movie_id"], 1)


if __name__ == "__main__":
    unittest.main()
