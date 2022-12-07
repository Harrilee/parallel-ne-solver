package site.harrilee;

import site.harrilee.dataProcessing.ReadData;
import site.harrilee.parallel.ParallelNE;
import site.harrilee.serial.SerialNE;

import java.io.IOException;


public class neSolver {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java -jar neSolver.jar [data file] [serial/parallel] [frankWolfe/columnGeneration]");
            System.out.println("Arg length: " + args);
            System.exit(1);
        }

        String dataFile = args[0];
        String method = args[1];
        String algorithm = args[2];

        ReadData.GraphData graphData = new ReadData().readData(dataFile + "_net.xlsx");

        long startTime = System.currentTimeMillis();
        double result = -1;
        int numPartitions = -1;

        if (method.equals("serial")) {
            SerialNE ne = new SerialNE();
            if (algorithm.equals("frankWolfe")) {
                ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
            } else if (algorithm.equals("columnGeneration")) {
                ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
            } else {
                System.out.println("Algorithm " + algorithm + " not supported. Please choose from frankWolfe or columnGeneration");
                System.exit(1);
            }
            SerialNE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
            result = neOutput.totalTime;
        } else if (method.equals("parallel")) {
            ParallelNE ne = new ParallelNE();
            if (algorithm.equals("frankWolfe")) {
                ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
            } else if (algorithm.equals("columnGeneration")) {
                ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
            } else {
                System.out.println("Algorithm " + algorithm + " not supported. Please choose from frankWolfe or columnGeneration");
                System.exit(1);
            }
            ParallelNE.NEOutput neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
            result = neOutput.totalTime;
            numPartitions = ne.numPartitions;
        }

        long endTime = System.currentTimeMillis();


        System.out.print("\n\n--------------------------OUTPUT--------------------------\n");
        System.out.printf("Algorithm: %s\n", algorithm);
        System.out.printf("Method: %s\n", method);
        System.out.printf("Dataset: %s\n", dataFile);
        System.out.printf("Num Partitions: %d\n", numPartitions);
        System.out.printf("Exec Time: %.3f seconds\n", (endTime - startTime) / 1000.0);
        System.out.printf("Output: %.3f seconds\n", result);
        System.out.print("----------------------------------------------------------\n");
    }
}