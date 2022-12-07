package serial;

import site.harrilee.dataProcessing.ReadData;
import site.harrilee.dataProcessing.ReadData.GraphData;
import site.harrilee.serial.SerialNE;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class AllGraphSerialTest {
    @Test
    public void testBraessNetFW() throws IOException {
        GraphData graphData = new ReadData().readData("Braess_net.xlsx");
        SerialNE ne = new SerialNE();
        SerialNE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
        System.out.println(neOutput.totalTime);
    }

    @Test
    public void testBraessNetCG() throws IOException {
        GraphData graphData = new ReadData().readData("Braess_net.xlsx");
        SerialNE ne = new SerialNE();
        SerialNE.NEOutput neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
        System.out.println(neOutput.totalTime);
    }

    @Test
    public void testNineNodeNetFW() throws IOException {
        GraphData graphData = new ReadData().readData("NineNode_net.xlsx");
        SerialNE ne = new SerialNE();
        SerialNE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
        System.out.println(neOutput.totalTime);
    }

    @Test
    public void testNineNodeNetCG() throws IOException {
        GraphData graphData = new ReadData().readData("NineNode_net.xlsx");
        SerialNE ne = new SerialNE();
        SerialNE.NEOutput neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
        System.out.println(neOutput.totalTime);
    }
//
//    @Test
//    public void testSiouxFallsNetFW() throws IOException {
//        GraphData graphData = new ReadData().readData("SiouxFalls_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testSiouxFallsNetCG() throws IOException {
//        GraphData graphData = new ReadData().readData("SiouxFalls_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testHullNetFW() throws IOException {
//        GraphData graphData = new ReadData().readData("Hull_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testHullNetCG() throws IOException {
//        GraphData graphData = new ReadData().readData("Hull_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testNewHullNetFW() throws IOException {
//        GraphData graphData = new ReadData().readData("NewHull_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testNewHullNetCG() throws IOException {
//        GraphData graphData = new ReadData().readData("NewHull_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testWinnipegNetFW() throws IOException {
//        GraphData graphData = new ReadData().readData("Winnipeg_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testWinnipegNetCG() throws IOException {
//        GraphData graphData = new ReadData().readData("Winnipeg_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testAnaheimNetFW() throws IOException {
//        GraphData graphData = new ReadData().readData("Anaheim_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testAnaheimNetCG() throws IOException {
//        GraphData graphData = new ReadData().readData("Anaheim_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testBarcelonaNetFW() throws IOException {
//        GraphData graphData = new ReadData().readData("Barcelona_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testBarcelonaNetCG() throws IOException {
//        GraphData graphData = new ReadData().readData("Barcelona_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testTerrassaNetFW() throws IOException {
//        GraphData graphData = new ReadData().readData("Terrassa_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testTerrassaNetCG() throws IOException {
//        GraphData graphData = new ReadData().readData("Terrassa_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testChicagoSketchaNetFW() throws IOException {
//        GraphData graphData = new ReadData().readData("ChicagoSketch_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }
//
//    @Test
//    public void testChicagoSketchNetCG() throws IOException {
//        GraphData graphData = new ReadData().readData("ChicagoSketch_net.xlsx");
//        NE ne = new NE();
//        NE.NEOutput neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//        System.out.println(neOutput.totalTime);
//    }

}
