package serial;

import main.serial.Graph;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GraphTest {
    @Test
    void testGraph1() {
        int[][] graphArray = new int[][]{{0, 4, 0, 0, 0, 0, 0, 8, 0},
                {4, 0, 8, 0, 0, 0, 0, 11, 0},
                {0, 8, 0, 7, 0, 4, 0, 0, 2},
                {0, 0, 7, 0, 9, 14, 0, 0, 0},
                {0, 0, 0, 9, 0, 10, 0, 0, 0},
                {0, 0, 4, 14, 10, 0, 2, 0, 0},
                {0, 0, 0, 0, 0, 2, 0, 1, 6},
                {8, 11, 0, 0, 0, 0, 1, 0, 7},
                {0, 0, 2, 0, 0, 0, 6, 7, 0}};
        Graph graph = new Graph(graphArray);
        assertEquals("[0, 4, 12, 19, 21, 11, 9, 8, 14]", graph.toDistString());
        assertEquals("[-1, 0, 1, 2, 5, 6, 7, 0, 2]", graph.toPathString());
    }

    @Test
    void testGraph2() {
        int[][] graphArray = new int[][]{{0, 3, 0},
                {0, 0, 0},
                {0, 0, 0}};
        Graph graph = new Graph(graphArray);
        assertEquals("[0, 3, 2147483647]", graph.toDistString());
        assertEquals("[-1, 0, -1]", graph.toPathString());
    }
}
