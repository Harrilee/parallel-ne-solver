# Pseudo Code

## Column Generation

```py
def column_generation(T, od_pairs):
    """
    T: 2D array of travel times, T[i][j] is the travel time from i to j, while T[i][j] is an array representing the "a"s in travel function: t = a1 + a2 * w + a3 * w^2...
    if T[i][j] == [0], then there is no path from i to j
    od_pair: an 2D array of (origin, destination, traffic) by index
    
    A sample of T:
    T = [
        [[0], [2,1], [0]],
        [[3,1,2], [0], [0,1,1]],
        [[0,1], [0,1], [0]]
    ]
    A sample of od_pair:
    od_pair = [(0, 2, 100), (1, 2, 200), (2, 1, 20)]
    """
    # Step 0: find shortest path for each od_pair
    current_traffic = [[0 for i in range(len(T))] for j in range(len(T))] # current traffic on each arc
    for od_pair in od_pairs:
        shortest_paths.append(dijkstra(od_pair, T, od_pairs, current_traffic))
    
    restricted_T = mask_T(T, shortest_paths)  # for arcs that are not in shortest path, set their travel time to 0

    # Main Loop
    while True:
        # Step 1: solve master problem
        current_traffic, z = solve_master_problem(restricted_T, od_pairs)

        # Step 2: solve sub problem
        new_path_found = False
        for od_pair in od_pairs:
            shortest_path = dijkstra(od_pair, T, current_traffic)
            if shortest_path not in shortest_paths:
                shortest_paths.append(shortest_path)
                restricted_T = mask_T(T, shortest_paths)
                new_path_found = True
                break
        if not new_path_found:
            break
    return current_traffic, z
```

## Frank-Wolfe Algorithm

```py
def frank_wolfe(T, od_pairs):
    """
    Use Frank-Wolfe algorithm to solve the convex optimization problem
    """
    # Step 0: find shortest path for each od_pair
    current_traffic = [[0 for i in range(len(T))] for j in range(len(T))]
    shortest_paths = []
    for od_pair in od_pairs:
        shortest_paths.append(dijkstra(od_pair, T, current_traffic))
    current_traffic = assign_traffic(od_pairs, shortest_paths, T)

    # Main Loop
    z = float('inf')
    while True:
        # Step 1: Solution of linearlized subproblem
        shortest_paths = []
        for od_pair in od_pairs:
            shortest_path = dijkstra(od_pair, T, current_traffic)
            shortest_paths.append(shortest_path)
        better_traffic = assign_traffic(od_pairs, shortest_paths, T)
        # Step 2: find optimal step size
        # todo: check ParTan method, it may bring a better solution
        step_size = 0.5
        for i in range(STEP_SIZE_LOOP_TIMES):
            gradient = calculate_gradient(T, od_pairs, current_traffic, better_traffic, step_size)
            if gradient < 0:
                step_size += 1/2**(i+1)
            else:
                step_size -= 1/2**(i+1)
        # Step 3: update current traffic
        current_traffic = current_traffic + step_size * (better_traffic - current_traffic)
        # Step 4: check convergence
        new_z = calculate_z(T, od_pairs, current_traffic)
        if (z - new_z) / z < EPSILON:
            break
        z = new_z
    return current_traffic, z
```
