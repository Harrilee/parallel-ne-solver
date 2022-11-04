package main.serial;


import java.util.Arrays;

/**
 * A basic graph with dijkstra's algorithm, the shortest path, and data caching.
 * Represent each row to be the origins, and each column to be destinations
 * this.graph[i][j] = 0 represents there's no arc between `i` and `j`
 */
public class Graph {
    public double[][] graph;
    public int size;
    public double[][] dists;
    public int[][] parents;

    public Graph(double[][] graph) {
        this.graph = graph;
        this.size = graph.length;
        this.dists = new double[this.size][];
        this.parents = new int[this.size][];
    }

    /**
     * Dijkstra's algorithm, find the shortest path from `start` to all other nodes.
     * With modifications including caching distances and paths.
     *
     * @param startIdx the origin vertex
     */
    public double[] dijkstra(int startIdx) {
        // Step 0: initialization
        double[] dist = new double[this.size];  // return value for minimum distance
        int[] parent = new int[this.size]; // keep track of the parent of current node
        for (int i = 0; i < this.size; i++) {
            parent[i] = -1;
        }
        Boolean[] sptSet = new Boolean[this.size]; // shortest spanning tree set

        for (int i = 0; i < this.size; i++) {
            dist[i] = Integer.MAX_VALUE;
            sptSet[i] = false;
        }

        dist[startIdx] = 0;


        // Step 2: main loop of dijkstra, iterate through each node
        for (int count = 0; count < this.size - 1; count++) {
            // Step 2.1: find out the closest u
            int u = -1;
            double minDist = Double.MAX_VALUE;
            for (int i = 0; i < this.size; i++) {
                if (!sptSet[i] && dist[i] < minDist) {
                    minDist = dist[i];
                    u = i;
                }
            }
            if (u == -1) {
                return dist;  // Graph not fully visited
            }
            // Step 2.2: mark u as visited, keep track of u
            sptSet[u] = true;
            // Step 2.3: update distances
            for (int v = 0; v < this.size; v++) {
                if (!sptSet[v] && this.graph[u][v] != 0 && this.graph[u][v] + dist[u] < dist[v] && dist[u] != Integer.MAX_VALUE) {
                    dist[v] = this.graph[u][v] + dist[u];
                    parent[v] = u;
                }
            }
        }
        this.dists[startIdx] = dist;
        this.parents[startIdx] = parent;
        return dist;
    }


    /**
     * Get the shortest path from startIdx to endIdx
     *
     * @param startIdx the index of the start node
     * @param endIdx   the index of the end node
     * @return the shortest path from startIdx to endIdx, in the form of a list of indices - [startIdx, ..., endIdx]
     */
    public int[] getShortestPath(int startIdx, int endIdx) {

        if (this.parents[startIdx] == null) {
            dijkstra(startIdx);
        }
        int[] path = new int[this.size];
        int idx = this.size - 1;
        int current = endIdx;
        while (current != -1) {
            path[idx] = current;
            current = this.parents[startIdx][current];
            idx--;
        }
        return Arrays.copyOfRange(path, idx + 1, this.size);
    }

    public double getShortestTime(int startIdx, int endIdx) {

        if (this.dists[startIdx] == null) {
            dijkstra(startIdx);
        }
        return this.dists[startIdx][endIdx];
    }
}
