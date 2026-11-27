from collections import deque


class Graph:
    def __init__(self):
        self.vertices = []
        self.label_to_vertex = {}
        self.adjacency = []

    def add_vertex(self, label):
        if label not in self.label_to_vertex:
            self.label_to_vertex[label] = len(self.vertices)
            self.vertices.append(label)
            self.adjacency.append({})

    def add_edge(self, start, end, weight=None, bidirectional=False):
        self.add_vertex(start)
        self.add_vertex(end)
        self.adjacency[self.label_to_vertex[start]][end] = weight
        if bidirectional:
            self.adjacency[self.label_to_vertex[end]][start] = weight

    def get_weight(self, start, end):
        index = self.label_to_vertex.get(start)
        return self.adjacency[index].get(end) if index is not None else None

    def get_edges(self, vertex):
        index = self.label_to_vertex.get(vertex)
        return list(self.adjacency[index].items()) if index is not None else []

    def get_neighbours(self, vertex):
        return [neighbour for neighbour, _ in self.get_edges(vertex)]

    def get_all_edges(self):
        return [(start, end, weight) for i, start in enumerate(self.vertices)
                for end, weight in self.adjacency[i].items()]

    def get_all_vertices(self):
        return list(self.vertices)

    def copy(self):
        return {vertex: [dict(self.adjacency[i]), False] for i, vertex in enumerate(self.vertices)}


def bfs(graph: Graph, source, max_depth=None, edge_filter=lambda weight: True):
    if source not in graph.label_to_vertex:
        return {}, {}
    distance = {source: 0}
    parent = {source: None}
    queue = deque([source])
    while queue:
        current = queue.popleft()
        if max_depth is not None and distance[current] >= max_depth:
            continue
        for neighbour, weight in graph.get_edges(current):
            if neighbour not in distance and edge_filter(weight):
                distance[neighbour] = distance[current] + 1
                parent[neighbour] = current
                queue.append(neighbour)
    return distance, parent
