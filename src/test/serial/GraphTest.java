package serial;

import main.serial.Graph;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class GraphTest {
    @Test
    void testGraphDijkstra1() {
        double[][] graphArray = new double[][]{{0, 4, 0, 0, 0, 0, 0, 8, 0},
                {4, 0, 8, 0, 0, 0, 0, 11, 0},
                {0, 8, 0, 7, 0, 4, 0, 0, 2},
                {0, 0, 7, 0, 9, 14, 0, 0, 0},
                {0, 0, 0, 9, 0, 10, 0, 0, 0},
                {0, 0, 4, 14, 10, 0, 2, 0, 0},
                {0, 0, 0, 0, 0, 2, 0, 1, 6},
                {8, 11, 0, 0, 0, 0, 1, 0, 7},
                {0, 0, 2, 0, 0, 0, 6, 7, 0}};
        Graph graph = new Graph(graphArray, 1);
        graph.dijkstra(0);
        assertArrayEquals(new double[]{0, 4, 12, 19, 21, 11, 9, 8, 14}, graph.dists[0]);
        assertArrayEquals(new int[]{-1, 0, 1, 2, 5, 6, 7, 0, 2}, graph.parents[0]);
    }


    @Test
    void testGraphDijkstra2() {
        double[][] graphArray = new double[][]{{0, 3, 0},
                {0, 0, 0},
                {0, 0, 0}};
        Graph graph = new Graph(graphArray, 1);
        graph.dijkstra(0);
        assertArrayEquals(new double[]{0, 3, 2147483647}, graph.dists[0]);
        assertArrayEquals(new int[]{-1, 0, -1}, graph.parents[0]);
    }

    @Test
    void testShortestPath1() {
        double[][] graphArray = new double[][]{{0, 4, 0, 0, 0, 0, 0, 8, 0},
                {4, 0, 8, 0, 0, 0, 0, 11, 0},
                {0, 8, 0, 7, 0, 4, 0, 0, 2},
                {0, 0, 7, 0, 9, 14, 0, 0, 0},
                {0, 0, 0, 9, 0, 10, 0, 0, 0},
                {0, 0, 4, 14, 10, 0, 2, 0, 0},
                {0, 0, 0, 0, 0, 2, 0, 1, 6},
                {8, 11, 0, 0, 0, 0, 1, 0, 7},
                {0, 0, 2, 0, 0, 0, 6, 7, 0}};
        Graph graph = new Graph(graphArray, 2);
        graph.getShortestPath(0, 3);
        // path
        assertArrayEquals(new int[]{0,1,2,3}, graph.getShortestPath(0, 3));
        // time
        assertEquals(19, graph.getShortestTime(0,3));
    }
}
