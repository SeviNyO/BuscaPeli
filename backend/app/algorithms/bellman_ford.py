from math import inf


def bellman_ford(graph, source, allowed=None, edge_filter=lambda weight: True):
    vertices = set(allowed if allowed is not None else graph.get_all_vertices())
    distances = {vertex: inf for vertex in vertices}
    if source not in vertices:
        return distances
    distances[source] = 0.0
    for _ in range(max(0, len(vertices) - 1)):
        changed = False
        for start in vertices:
            if distances[start] == inf:
                continue
            for end, weight in graph.get_edges(start):
                if end in vertices and edge_filter(weight) and distances[start] + weight < distances[end]:
                    distances[end] = distances[start] + weight
                    changed = True
        if not changed:
            break
    return distances
