# Pseudo Code

## Column Generation

```py
def column_generation(T, od_pair):
    """
    T: 2D array of travel times, T[i][j] is the travel time from i to j, while T[i][j] is an array representing the "a"s in travel function: t = a1 + a2 * w + a3 * w^2...
    if T[i][j] == [0], then there is no path from i to j
    od_pair: an 2D array of (origin, destination) by index
    
    A sample of T:
    T = [
        [[0], [0,1], [0]],
        [[0,1], [0], [0,1]],
        [[0,1], [0,1], [0]]
    ]
    A sample of od_pair:
    od_pair = [(0, 2)]
    """
    # Initialize
    initialize()

    # Main Loop
    while not converged():
        # Solve Master Problem
        solve_master()

        # Solve Subproblem
        solve_subproblem()

        # Add New Column
        add_new_column()
```
