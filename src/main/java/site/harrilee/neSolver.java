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
        long timeGetNewTfc = -1;
        long timeFindOptimalStep = -1;
        long timeMainLoop = -1;

        if (method.equals("serial")) {
                        SerialNE ne = new SerialNE();
            SerialNE.NEOutput neOutput = null;
            startTime = System.currentTimeMillis();
            if (algorithm.equals("frankWolfe")) {
                neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
            } else if (algorithm.equals("columnGeneration")) {
                neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
            } else {
                System.out.println("Algorithm " + algorithm + " not supported. Please choose from frankWolfe or columnGeneration");
                System.exit(1);
            }
            timeGetNewTfc = ne.timeGetNewTfc;
            timeFindOptimalStep = ne.timeFindOptimalStep;
            timeMainLoop = ne.timeMainLoop;
            result = neOutput.totalTime;
        } else if (method.equals("parallel")) {
                        ParallelNE ne = new ParallelNE();
            ParallelNE.NEOutput neOutput = null;
            if (algorithm.equals("frankWolfe")) {
                neOutput = ne.frankWolfe(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
//            } else if (algorithm.equals("columnGeneration")) {
//                neOutput = ne.columnGeneration(graphData.tripRtFunc, graphData.odPs, "BPR", graphData.firstThruNode);
            } else {
                System.out.println("Algorithm " + algorithm + " not supported. Please choose from frankWolfe or columnGeneration");
                System.exit(1);
            }
            result = neOutput.totalTime;
            timeGetNewTfc = ne.timeGetNewTfc;
            timeFindOptimalStep = ne.timeFindOptimalStep;
            timeMainLoop = ne.timeMainLoop;
            numPartitions = ne.numPartitions;
        }

        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;


        System.out.print("\n\n--------------------------OUTPUT--------------------------\n");
        System.out.printf("Algorithm: %s\n", algorithm);
        System.out.printf("Method: %s\n", method);
        System.out.printf("Dataset: %s\n", dataFile);
        System.out.printf("Num Partitions: %d\n", numPartitions);
        System.out.printf("Total Exec Time: %.3f\n", totalTime / 1000.0);
        System.out.printf("GetNewTfc Time: %.3f\n", timeGetNewTfc / 1000.0);
        System.out.printf("FindOptStep Time: %.3f\n", timeFindOptimalStep / 1000.0);
        System.out.printf("Main Loop Time: %.3f\n", timeMainLoop / 1000.0);
        System.out.printf("Algorithm Output: %.3f\n", result);
        System.out.print("----------------------------------------------------------\n");
    }
}