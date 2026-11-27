from dataclasses import dataclass

from app.algorithms.ufds import UFDS


@dataclass(frozen=True)
class Edge:
    start: object
    end: object
    weight: float


def kruskal(vertices, edges, target_components=1):
    sets = UFDS(vertices)
    accepted = []
    for edge in sorted(edges, key=lambda item: (item.weight, str(item.start), str(item.end))):
        if sets.components <= target_components:
            break
        if sets.union(edge.start, edge.end):
            accepted.append(edge)
    return accepted, sets.groups()
