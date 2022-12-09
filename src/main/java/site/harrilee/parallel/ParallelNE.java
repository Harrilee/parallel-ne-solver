package site.harrilee.parallel;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;

import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import org.jetbrains.annotations.NotNull;
import scala.Function3;
import scala.Function4;
import scala.Tuple2;


public class ParallelNE {

    final int LOOP_LIMIT = 5;
    final double EPSILON = 1e-6;

    public double[][] travelTimes; // travel time for each OD pair, used for column generation
    public double[][][] maskedNetwork; // masked network for each OD pair, used for column generation

    public static JavaSparkContext jsc;
    public SparkSession spark;
    public SparkContext sc;

    public int numPartitions;
    public long timeGetNewTfc = 0;
    public long timeFindOptimalStep = 0;
    public long timeMainLoop = 0;

    /**
     * Return values for Frank-Wolfe algorithm and Column Generation algorithm
     */
    public static class NEOutput {
        public double[][] curTfc;
        public double z;
        public double totalTime;
    }

    public ParallelNE() {
        this.spark = SparkSession
                .builder()
                .appName("neSolver")
                .config("spark.master", "local[*]")
                .getOrCreate();

        this.jsc = new JavaSparkContext(this.spark.sparkContext());
        this.jsc.setLogLevel("ERROR");

        System.out.println("\n\n-----------------SPARK CONFIGURATION-----------------\n");
        Tuple2<String, String> sc[] = this.jsc.getConf().getAll();
        for (int i = 0; i < sc.length; i++) {
            System.out.println(sc[i]);
        }
        System.out.println("-----------------------------------------------------\n");
    }


    /**
     * Calculate Z for linear functions
     *
     * @param broadcastTripRtFunc trip rate function (linear function), let broadcastTripRtFunc[i][j] = [a1, a2, ... an], which represents
     *                            the travel time between origin i and destination j to be `a1 + a2 * t + a3 * t^2 + ...`
     * @param curTfcRDD           current traffic, let curTfcRDD[i][j] = t, which represents the travel time between origin i and destination j
     */
    double getZLinear(
            Broadcast<double[][][]> broadcastTripRtFunc,
            JavaPairRDD<Integer, double[]> curTfcRDD
    ) {
        double z;
        z = curTfcRDD.map(t -> {
            int origin = t._1();
            double[] curTfcOri = t._2();
            double zOri = 0;
            double[][] tripRtFuncOri = broadcastTripRtFunc.getValue()[origin];
            for (int i = 0; i < tripRtFuncOri.length; i++) {
                for (int k = 0; k < tripRtFuncOri[i].length; k++) {
                    zOri += tripRtFuncOri[i][k] / (k + 1) * Math.pow(curTfcOri[i], (k + 1));
                }
            }
            return zOri;
        }).reduce(Double::sum);


        return z;
    }

    /**
     * Calculate Z for BPR functions
     */

    double getZBPR(
            Broadcast<double[][][]> broadcastTripRtFunc,
            JavaPairRDD<Integer, double[]> curTfcRDD
    ) {
        double z;
        z = curTfcRDD.map(t -> {
            int origin = t._1();
            double[] curTfcOri = t._2();
            double zOri = 0;
            double[][] tripRtFuncOri = broadcastTripRtFunc.getValue()[origin];
            for (int i = 0; i < tripRtFuncOri.length; i++) {
                if (tripRtFuncOri[i] == null || tripRtFuncOri[i].length == 0) {
                    zOri += 0;
                } else {
                    zOri += tripRtFuncOri[i][0] *
                            (curTfcOri[i] + tripRtFuncOri[i][1] / (tripRtFuncOri[i][3] + 1) / (Math.pow(tripRtFuncOri[i][2], tripRtFuncOri[i][3])) * Math.pow(curTfcOri[i], tripRtFuncOri[i][3] + 1));

                }
            }
            return zOri;
        }).reduce(Double::sum);

        return z;
    }

    /**
     * Calculate Z for linear or BPR functions
     *
     * @return z
     */
    public double getZ(
            Broadcast<double[][][]> broadcastTripRtFunc,
            JavaPairRDD<Integer, double[]> curTfcRDD,
            Broadcast<String> broadcastTripRtFuncType
    ) {
        if (broadcastTripRtFuncType.getValue().equals("linear")) {
            return getZLinear(broadcastTripRtFunc, curTfcRDD);
        } else if (broadcastTripRtFuncType.getValue().equals("BPR")) {
            return getZBPR(broadcastTripRtFunc, curTfcRDD);
        } else {
            throw new IllegalArgumentException("tripRtFuncType must be either linear or BPR");
        }
    }


    public class GetNewBroadcastTripRt implements Function4<
            Broadcast<double[][][]>,
            JavaPairRDD<Integer, double[]>,
            Broadcast<String>,
            Broadcast<Integer>,
            Broadcast<double[][]>
            > {

        /**
         * Get a broadcast graph representing traffic time
         *
         * @param broadcastTripRtFunc     broadcast trip rate function
         * @param broadcastTripRtFuncType broadcast trip rate function type
         * @param broadcastSize           broadcast size
         * @return broadcast graph representing traffic time
         */
        public Broadcast<double[][]> apply(
                Broadcast<double[][][]> broadcastTripRtFunc,
                @NotNull JavaPairRDD<Integer, double[]> curTfcRDD,
                Broadcast<String> broadcastTripRtFuncType,
                @NotNull Broadcast<Integer> broadcastSize
        ) {
            JavaPairRDD<Integer, double[]> tripRtRDD = curTfcRDD.mapToPair(tuple2 -> {

                Integer origin = tuple2._1();
                double[] curTfc = tuple2._2();
                double[][] tripRtFunc = broadcastTripRtFunc.value()[origin];
                String tripRtFuncType = broadcastTripRtFuncType.value();

                double[] tripRt;

                if (tripRtFuncType.equals("linear")) {
                    tripRt = new double[curTfc.length];
                    for (int i = 0; i < curTfc.length; i++) {
                        tripRt[i] = 0;
                        for (int k = 0; k < tripRtFunc[i].length; k++) {
                            tripRt[i] += tripRtFunc[i][k] * Math.pow(curTfc[i], k);
                        }
                    }
                } else if (tripRtFuncType.equals("BPR")) {
                    tripRt = new double[curTfc.length];
                    for (int i = 0; i < curTfc.length; i++) {
                        if (tripRtFunc[i] == null || tripRtFunc[i].length == 0) {
                            tripRt[i] = 0;
                        } else {
                            tripRt[i] = tripRtFunc[i][0] * (1 + tripRtFunc[i][1] * Math.pow(curTfc[i] / tripRtFunc[i][2], tripRtFunc[i][3]));
                        }
                    }
                } else {
                    tripRt = null;
                }

                return new Tuple2<>(origin, tripRt);
            });

            Map<Integer, double[]> tripMap = tripRtRDD.collectAsMap();
            double[][] tripRt = new double[broadcastSize.value()][broadcastSize.value()];
            for (int i = 0; i < broadcastSize.value(); i++) {
                double[] tripRtOri = tripMap.get(i);
                if (tripRtOri == null) {
                    tripRt[i] = new double[broadcastSize.value()];
                } else {
                    tripRt[i] = tripRtOri;
                }
            }
            return ParallelNE.jsc.broadcast(tripRt);

        }
    }


    /**
     * Get new all-or-nothing assignment based on current traffic
     */
    public JavaPairRDD<Integer, double[]> getNewTfc(
            Broadcast<double[][]> broadcastTripRt,
            JavaRDD<Integer> originsRDD,
            JavaRDD<Integer> nodesRDD,
            Broadcast<Integer> broadcastFirstThruNode,
            Broadcast<Integer> broadcastSize,
            Broadcast<double[][]> broadcastOdPs
    ) {
        long startTime = System.currentTimeMillis();

        double[][] curTfc = originsRDD.map(origin -> {
            double[][] tripRt = broadcastTripRt.getValue();
            int firstThruNode = broadcastFirstThruNode.getValue();
            int size = broadcastSize.getValue();
            double[][] odPs = broadcastOdPs.getValue();
            Graph curGraph = new Graph(tripRt, firstThruNode);
            curGraph.dijkstra(origin);
            double[][] newTfcOri = new double[size][size];
            for (int j = 0; j < size; j++) {
                if (odPs[origin][j] > 0) {
                    int[] shortestPath = curGraph.getShortestPath(origin, j);
                    for (int k = 0; k < shortestPath.length - 1; k++) {
                        newTfcOri[shortestPath[k]][shortestPath[k + 1]] += odPs[origin][j];
                    }
                }
            }
            return newTfcOri;
        }).reduce((tfc1, tfc2) -> {
            int size = broadcastSize.getValue();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    tfc1[i][j] += tfc2[i][j];
                }
            }
            return tfc1;
        });
        JavaPairRDD<Integer, double[]> curTfcRDD = nodesRDD.mapToPair(origin -> new Tuple2<>(origin, curTfc[origin]));

        long endTime = System.currentTimeMillis();
        this.timeGetNewTfc += endTime - startTime;
        return curTfcRDD;
    }


//    /**
//     * Update the `Restricted Master Problem` of the column generation algorithm
//     * Given masked network, add new OD-pair to the masked traffic stored in this.maskedNetwork
//     * Travel time for each OD-pair is stored in this.travelTimes
//     *
//     * @param tripRtFunc     traffic rate function
//     * @param odPs           OD pairs
//     * @param curTfc         current traffic
//     * @param tripRtFuncType traffic function type, either "linear" or "BPR"
//     * @param initialUpdate  whether this is the first update
//     * @return whether the new OD-pair is added to the masked traffic
//     */
//    public boolean updateNetwork(double[][][] tripRtFunc, double[][] odPs, double[][] curTfc, String tripRtFuncType, boolean initialUpdate, int firstThruNode) {
//        int size = tripRtFunc.length;
//        boolean updated = false;
//        if (initialUpdate) { // initialization jobs
//            this.travelTimes = new double[size][size];
//            curTfc = new double[size][size];
//        }
//        double[][] curGraph = getTripRt(tripRtFunc, curTfc, tripRtFuncType);
//        Graph graph = new Graph(curGraph, firstThruNode);
//        for (int i = 0; i < size; i++) {
//            for (int j = 0; j < size; j++) {
//                if (odPs[i][j] > 0) {
//                    int[] shortestPath = graph.getShortestPath(i, j);
//                    double shortestTime = graph.getShortestTime(i, j);
//                    if (shortestTime < this.travelTimes[i][j] || this.travelTimes[i][j] == 0) {
//                        for (int k = 0; k < shortestPath.length - 1; k++) { // add the shortest path to masked network
//                            if (this.maskedNetwork[shortestPath[k]][shortestPath[k + 1]] == null || this.maskedNetwork[shortestPath[k]][shortestPath[k + 1]].length == 0) {
//                                this.maskedNetwork[shortestPath[k]][shortestPath[k + 1]] = tripRtFunc[shortestPath[k]][shortestPath[k + 1]];
//                                updated = true;
//                            }
//                        }
//                        if (!initialUpdate) {
//                            // if this is the first update, we need to have a feasible solution
//                            // thus we cannot have early return
//                            // else we can apply early return
//                            this.travelTimes[i][j] = shortestTime;
////                            return true;
//                        }
//                    }
//                }
//            }
//        }
//        return updated;
//    }

    /**
     * Calculate the weighted average of X1 and X2, where X1 and X2 are both 2D arrays
     *
     * @param X1     the first 2D array
     * @param X2     the second 2D array
     * @param weight weight of X1, while the weight of X2 would be (1-weight)
     * @return the weighted average array
     */
    JavaPairRDD<Integer, double[]> calcWeightedAverage2DArray(JavaPairRDD<Integer, double[]> X1, JavaPairRDD<Integer, double[]> X2, double weight, Broadcast<Integer> broadcastSize) {
        int size = broadcastSize.value();
        return X1.join(X2).mapToPair(t -> {
            int k = t._1();
            double[] v1 = t._2._1();
            double[] v2 = t._2._2();
            double[] v = new double[size];
            for (int i = 0; i < size; i++) {
                v[i] = v1[i] * weight + v2[i] * (1 - weight);
            }
            return new Tuple2<>(k, v);
        });
    }


    /**
     * Frank-Wolfe algorithm for the network equilibrium problem
     *
     * @param tripRtFunc     trip rate function
     * @param odPs           odPs[i][j] represents the travel demand from origin i to destination j
     * @param tripRtFuncType traffic function type, either "linear" or "BPR"
     * @return NEOutput
     * @see NEOutput
     */
    public NEOutput frankWolfe(double[][][] tripRtFunc, double[][] odPs, String tripRtFuncType, int firstThruNode) {
        long startTimeMainLoop = System.currentTimeMillis();


        NEOutput neOutput = new NEOutput();
        int size = tripRtFunc.length;


        double z, newZ;
        int iter = 0;
        /* ------------------------------------------------------------------- */
        /* ---------------------- Step -1: Preparations ---------------------- */
        /* ------------------------------------------------------------------- */
        // broadcast graph data
        Broadcast<double[][][]> broadcastTripRtFunc = this.jsc.broadcast(tripRtFunc);
        Broadcast<double[][]> broadcastOdPs = this.jsc.broadcast(odPs);
        Broadcast<String> broadcastTripRtFuncType = this.jsc.broadcast(tripRtFuncType);
        Broadcast<Integer> broadcastFirstThruNode = this.jsc.broadcast(firstThruNode);
        Broadcast<Integer> broadcastSize = this.jsc.broadcast(size);

        // find all origins to distribute
        List<Integer> origins = new ArrayList<>(size);
        List<Integer> nodes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            nodes.add(i);
            for (int j = 0; j < size; j++) {
                if (odPs[i][j] > 0) {
                    origins.add(i);
                    break;
                }
            }
        }

        /* ------------------------------------------------------------------------------ */
        /* --------- Step 0: find a feasible solution (using the shortest path) --------- */
        /* ------------------------------------------------------------------------------ */

        JavaRDD<Integer> originsRDD = jsc.parallelize(origins);
        JavaRDD<Integer> nodesRDD = jsc.parallelize(nodes);
        this.numPartitions = originsRDD.getNumPartitions();
        // initialize all traffic to 0
        JavaPairRDD<Integer, double[]> curTfcRDD = nodesRDD.mapToPair(origin -> {
            double[] curTfcOri = new double[broadcastSize.value()];
            for (int i = 0; i < broadcastSize.value(); i++) {
                curTfcOri[i] = 0;
            }
            return new Tuple2<>(origin, curTfcOri);
        });
        Function4<Broadcast<double[][][]>, JavaPairRDD<Integer, double[]>, Broadcast<String>, Broadcast<Integer>, Broadcast<double[][]>> getNewBroadcastTripRt = new GetNewBroadcastTripRt();
        Broadcast<double[][]> broadcastTripRt = getNewBroadcastTripRt.apply(broadcastTripRtFunc, curTfcRDD, broadcastTripRtFuncType, broadcastSize);
        // assign the shortest path to the current network
        curTfcRDD = getNewTfc(broadcastTripRt, originsRDD, nodesRDD, broadcastFirstThruNode, broadcastSize, broadcastOdPs);
        while (true) {

            /* --------------------------------------------------------------- */
            /* ---------  Step 1: solution of linearized sub problem --------- */
            /* --------------------------------------------------------------- */
            broadcastTripRt = getNewBroadcastTripRt.apply(broadcastTripRtFunc, curTfcRDD, broadcastTripRtFuncType, broadcastSize);
            JavaPairRDD<Integer, double[]> newTfcRDD = getNewTfc(broadcastTripRt, originsRDD, nodesRDD, broadcastFirstThruNode, broadcastSize, broadcastOdPs);
            z = getZ(broadcastTripRtFunc, curTfcRDD, broadcastTripRtFuncType);

            //System.out.println("Iteration " + iter + ": " + z);

            /* -------------------------------------------------- */
            /* --------- Step 2: find optimal step size --------- */
            /* -------------------------------------------------- */
            //  (we can use the ParTan method as well, below is a simple method)
            long startTime = System.currentTimeMillis();
            double stepSize = 0.5;
            for (int i = 0; i < this.LOOP_LIMIT; i++) {
                // calculate gradient
                JavaPairRDD<Integer, double[]> gradientAvg = calcWeightedAverage2DArray(curTfcRDD, newTfcRDD, stepSize, broadcastSize);
                JavaPairRDD<Integer, double[]> gradientAvgPlusEpsilon = calcWeightedAverage2DArray(curTfcRDD, newTfcRDD, stepSize + this.EPSILON, broadcastSize);
                double zAvg = getZ(broadcastTripRtFunc, gradientAvg, broadcastTripRtFuncType);
                double zAvgPlusEpsilon = getZ(broadcastTripRtFunc, gradientAvgPlusEpsilon, broadcastTripRtFuncType);
                double gradient = (zAvgPlusEpsilon - zAvg) / this.EPSILON;
                if (gradient < 0) {
                    stepSize += 1 / Math.pow(2, i + 1);
                } else {
                    stepSize -= 1 / Math.pow(2, i + 1);
                }
            }
            long endTime = System.currentTimeMillis();
            this.timeFindOptimalStep += (endTime - startTime);

            /* -------------------------------------------- */
            /* ------ Step 3: update current traffic ------ */
            /* -------------------------------------------- */
            curTfcRDD = calcWeightedAverage2DArray(curTfcRDD, newTfcRDD, stepSize, broadcastSize);

            /* ------------------------------------ */
            /* -----Step 4: check convergence ----- */
            /* ------------------------------------ */
            newZ = getZ(broadcastTripRtFunc, curTfcRDD, broadcastTripRtFuncType);
            if (z - newZ < this.EPSILON) {
                break;
            } else {
                iter++;
            }
        }
        long endTimeMainLoop = System.currentTimeMillis();
        this.timeMainLoop += (endTimeMainLoop - startTimeMainLoop);

        // End step: gather output values


        neOutput.curTfc = curTfcRDD.map(t -> {
            int v = t._1();
            double[] w = t._2();
            double[][] w2d = new double[w.length][w.length];
            System.arraycopy(w, 0, w2d[v], 0, w.length);
            return w2d;
        }).reduce((double[][] a, double[][] b) -> {
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a.length; j++) {
                    a[i][j] += b[i][j];
                }
            }
            return a;
        });

        neOutput.z = z;
        // calculate the total trip rate
        double totalTime = 0;
        broadcastTripRt = getNewBroadcastTripRt.apply(broadcastTripRtFunc, curTfcRDD, broadcastTripRtFuncType, broadcastSize);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                totalTime += broadcastTripRt.getValue()[i][j] * neOutput.curTfc[i][j];
            }
        }
        neOutput.totalTime = totalTime;
        return neOutput;
    }

//    /**
//     * Column generation algorithm for the network equilibrium problem.
//     * This function calls the frankWolfe function to solve the linearized sub problem.
//     *
//     * @param tripRtFunc     trip rate function
//     * @param odPs           odPs[i][j] represents the travel demand from origin i to destination j
//     * @param tripRtFuncType traffic function type, either "linear" or "BPR"
//     * @return NEOutput
//     * @see NEOutput
//     */
//    public NEOutput columnGeneration(double[][][] tripRtFunc, double[][] odPs, String tripRtFuncType, int firstThruNode) {
//        NEOutput neOutput = new NEOutput();
//        int size = tripRtFunc.length;
//        // Step 0: find a feasible solution (using the shortest path)
//        this.maskedNetwork = new double[size][size][];
//        updateNetwork(tripRtFunc, odPs, neOutput.curTfc, tripRtFuncType, true, firstThruNode); // curTfc is 0
//        double z;
//        int iter = 0;
//
//        while (true) {
//            // utils
//            iter++;
//            // count used links
//            int usedLinkCount = 0;
//            for (int i = 0; i < size; i++) {
//                for (int j = 0; j < size; j++) {
//                    if (this.maskedNetwork[i][j] != null && this.maskedNetwork[i][j].length > 0) {
//                        usedLinkCount++;
//                    }
//                }
//            }
//            System.out.println("Iteration " + iter + ". Used link count: " + usedLinkCount);
//            // Step 1: solve master problem
//            neOutput = frankWolfe(this.maskedNetwork, odPs, tripRtFuncType, firstThruNode);
//            // Step 2: solve sub problem
//            boolean updated = updateNetwork(tripRtFunc, odPs, neOutput.curTfc, tripRtFuncType, false, firstThruNode);
//            if (!updated) {
//                break;
//            }
//        }
//        System.out.println("Column generation iteration " + iter);
//        return neOutput;
//    }
}