def merge_sort(items, key=lambda value: value):
    if len(items) < 2:
        return list(items)
    middle = len(items) // 2
    left = merge_sort(items[:middle], key)
    right = merge_sort(items[middle:], key)
    result = []
    left_index = right_index = 0
    while left_index < len(left) and right_index < len(right):
        if key(left[left_index]) <= key(right[right_index]):
            result.append(left[left_index])
            left_index += 1
        else:
            result.append(right[right_index])
            right_index += 1
    return result + left[left_index:] + right[right_index:]
