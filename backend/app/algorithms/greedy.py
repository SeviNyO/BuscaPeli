def greedy_top_n(ranked_items, count):
    result = []
    for item in ranked_items:
        if len(result) >= max(0, count):
            break
        result.append(item)
    return result
