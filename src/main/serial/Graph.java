package main.serial;


import java.util.Arrays;

public class Graph {
    /*
    A basic graph with dijkstra
    Represent each row to be the origins, and each column to be destinations
    this.graph[i][j] = 0 represents there's no arc between $i$ and $j$
     */
    public int[][] graph;
    public int size;
    int[] dist;
    int[] parent;

    public Graph(int[][] graph) {
        this.graph = graph;
        this.size = graph.length;
    }

    public int[] dijkstra(int startIdx) {
        // Step 0: initialization
        this.dist = new int[this.size];  // return value for minimum distance
        this.parent = new int[this.size]; // keep track of the parent of current node
        for (int i = 0; i<this.size;i++){
            this.parent[i] = -1;
        }
        Boolean[] sptSet = new Boolean[this.size]; // shortest spanning tree set

        for (int i = 0; i < this.size; i++) {
            this.dist[i] = Integer.MAX_VALUE;
            sptSet[i] = false;
        }

        this.dist[startIdx] = 0;


        // Step 2: main loop of dijkstra, iterate through each node
        for (int count = 0; count < this.size - 1; count++) {
            // Step 2.1: find out the closest u
            int u = -1;
            int minDist = Integer.MAX_VALUE;
            for (int i = 0; i < this.size; i++) {
                if (!sptSet[i] && dist[i] < minDist) {
                    minDist = dist[i];
                    u = i;
                }
            }
            if (u == -1){
                return dist;  // Graph not fully visited
            }
            // Step 2.2: mark u as visited, keep track of u
            sptSet[u] = true;
            // Step 2.3: update distances
            for (int v = 0; v < this.size; v++) {
                if (!sptSet[v] && this.graph[u][v] != 0 && this.graph[u][v] + this.dist[u] < this.dist[v] && this.dist[u] != Integer.MAX_VALUE) {
                    this.dist[v] = this.graph[u][v] + this.dist[u];
                    this.parent[v] = u;
                }
            }
        }
        return dist;
    }

    public String toDistString() {
        /*
        get distance to each node in the format of string
         */
        return Arrays.toString(this.dist);
    }

    public String toPathString() {
        /*
        get path to each node in the format of string
         */
        return Arrays.toString(this.parent);
    }
}
