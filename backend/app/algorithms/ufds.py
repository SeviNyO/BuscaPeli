class UFDS:
    def __init__(self, elements):
        self.parent = list(range(elements)) if isinstance(elements, int) else {item: item for item in elements}
        self.rank = [0] * elements if isinstance(elements, int) else {item: 0 for item in elements}
        self.components = elements if isinstance(elements, int) else len(self.parent)

    def find(self, item):
        parent = self.parent[item]
        if parent != item:
            self.parent[item] = self.find(parent)
        return self.parent[item]

    def union(self, first, second):
        root_first, root_second = self.find(first), self.find(second)
        if root_first == root_second:
            return False
        if self.rank[root_first] < self.rank[root_second]:
            root_first, root_second = root_second, root_first
        self.parent[root_second] = root_first
        if self.rank[root_first] == self.rank[root_second]:
            self.rank[root_first] += 1
        self.components -= 1
        return True

    def groups(self):
        result = {}
        for item in self.parent:
            result.setdefault(self.find(item), set()).add(item)
        return list(result.values())
