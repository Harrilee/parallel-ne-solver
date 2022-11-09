package ReadData;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import main.dataProcessing.ReadData;

import java.io.IOException;


public class ReadDataTest {
    double[][][] tripRtFuncBPR9 = new double[][][]{
            {{}, {}, {}, {}, {5, 0.15, 12, 4}, {6, 0.15, 18, 4}, {}, {}, {}},
            {{}, {}, {}, {}, {3, 0.15, 35, 4}, {9, 0.15, 35, 4}, {}, {}, {}},
            {{}, {}, {}, {}, {}, {}, {}, {}, {}},
            {{}, {}, {}, {}, {}, {}, {}, {}, {}},
            {{}, {}, {}, {}, {}, {9, 0.15, 20, 4}, {2, 0.15, 11, 4}, {}, {8, 0.15, 26, 4}},
            {{}, {}, {}, {}, {4, 0.15, 11, 4}, {}, {}, {6, 0.15, 33, 4}, {7, 0.15, 32, 4}},
            {{}, {}, {3, 0.15, 25, 4}, {6, 0.15, 24, 4}, {}, {}, {}, {2, 0.15, 19, 4}, {}},
            {{}, {}, {8, 0.15, 39, 4}, {6, 0.15, 43, 4}, {}, {}, {4, 0.15, 36, 4}, {}, {}},
            {{}, {}, {}, {}, {}, {}, {4, 0.15, 26, 4}, {8, 0.15, 30, 4}, {}},
    };
    double[][] odPs9 = new double[][]{
            {0, 0, 10, 20, 0, 0, 0, 0, 0},
            {0, 0, 30, 40, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

    double[][][] tripRtFuncBPRBraess = new double[][][]{
            {{}, {}, {1e-8, 1e9, 1, 1}, {50, 0.02, 1, 1}},
            {{}, {}, {}, {}},
            {{}, {50, 0.02, 1, 1}, {}, {10, 0.1, 1, 1}},
            {{}, {1e-8, 1e9, 1, 1}, {}, {}},
    };

    double[][] odPsBraess = new double[][]{
            {0, 6, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
    };

    @Test
    void testReadDataNineNode() throws IOException {
        ReadData readData = new ReadData();
        ReadData.GraphData graphData = readData.readData("NineNode_net.xlsx");
        Assertions.assertArrayEquals(tripRtFuncBPR9, graphData.tripRtFunc);
        Assertions.assertArrayEquals(odPs9, graphData.odPs);
    }

    @Test
    void testReadDataBraess() throws IOException {
        ReadData readData = new ReadData();
        ReadData.GraphData graphData = readData.readData("Braess_net.xlsx");
        Assertions.assertArrayEquals(tripRtFuncBPRBraess, graphData.tripRtFunc);
        Assertions.assertArrayEquals(odPsBraess, graphData.odPs);
    }
}
