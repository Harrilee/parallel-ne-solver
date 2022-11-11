package serial;

import main.serial.NE;


import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;


public class NETest {
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
            {{}, {}, {}, {}, {}, {}},
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
    void testGetTripRtLinear() {
        double[][][] tripRtFuncLinear = new double[][][]{
                {{}, {4, 5, 6}, {7, 8}},
                {{1}, {}, {}},
                {{0, 0, 3}, {1, 2, 3, 4}, {}}
        };
        double[][] curTfc = new double[][]{
                {0, 2, 3},
                {4, 0, 6},
                {7, 8, 0}
        };
        double[][] tripRt = new double[][]{
                {0, 38, 31},
                {1, 0, 0},
                {147, 2257, 0}
        };
        NE ne = new NE();
        assertArrayEquals(tripRt, ne.getTripRt(tripRtFuncLinear, curTfc, "linear"));
    }

    @Test
    void testGetTripRtBPR() {

        double[][] curTfc = new double[][]{
                {0, 0, 0, 0, 5, 6, 0, 0, 0},
                {0, 0, 0, 0, 7, 3, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 2, 3, 0, 8},
                {0, 0, 0, 0, 1, 0, 0, 8, 9},
                {0, 0, 1, 2, 0, 0, 0, .0, 0},
                {0, 0, 8.1, 0.3, 0, 0, 0.6, 0, 0},
                {0, 0, 0, 0, 0, 0, 2.2, 101, 0},
        };
        double[][] tripRt = new double[][]{
                {0.0, 0.0, 0.0, 0.0, 5.022605613425926, 6.011111111111111, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 3.0007200000000003, 9.000072869637652, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 9.000135, 2.001659722696537, 0.0, 8.010755925912958},
                {0.0, 0.0, 0.0, 0.0, 4.000040980807322, 0.0, 0.0, 6.003108470125751, 7.006569910049437},
                {0.0, 0.0, 3.0000011520000003, 6.000043402777777, 0.0, 0.0, 0.0, 2.0, 0.0},
                {0.0, 0.0, 8.002232867196527, 6.000000002132326, 0.0, 0.0, 4.000000046296297, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.000030757326424, 162.16355703703704, 0.0},
        };
        NE ne = new NE();
        assertArrayEquals(tripRt, ne.getTripRt(tripRtFuncBPR9, curTfc, "BPR"));
    }


    @Test
    void testFrankWolfeBPR9() {
        NE ne = new NE();
        NE.NEOutput neOutput = ne.frankWolfe(tripRtFuncBPR9, odPs9, "BPR",1);
        System.out.println(neOutput.totalTime);
        assertTrue(neOutput.totalTime > 2455.87 - 5 && neOutput.totalTime < 2455.87 + 5);
    }

    @Test
    void testFrankWolfeBPRBraess() {
        NE ne = new NE();
        NE.NEOutput neOutput = ne.frankWolfe(tripRtFuncBPRBraess, odPsBraess, "BPR",1);
        System.out.println(neOutput.totalTime);
        assertTrue(neOutput.totalTime > 552 - 5 && neOutput.totalTime < 552 + 5);
    }

    @Test
    void testColumnGenerationBPR9() {
        NE ne = new NE();
        NE.NEOutput neOutput = ne.columnGeneration(tripRtFuncBPR9, odPs9, "BPR",1);
        System.out.println(neOutput.totalTime);
        assertTrue(neOutput.totalTime > 2455.87 - 5 && neOutput.totalTime < 2455.87 + 5);
    }

    @Test
    void testColumnGenerationBPRBraess() {
        NE ne = new NE();
        NE.NEOutput neOutput = ne.columnGeneration(tripRtFuncBPRBraess, odPsBraess, "BPR",1);
        System.out.println(neOutput.totalTime);
        assertTrue(neOutput.totalTime > 552 - 5 && neOutput.totalTime < 552 + 5);
    }
}
