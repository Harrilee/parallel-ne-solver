package main.serial;


public class NE {

    final int LOOP_LIMIT = 20;
    final double EPSILON = 1e-6;

    /**
     * Return values for Frank-Wolfe algorithm and Column Generation algorithm
     */
    public static class NEOutput {
        public double[][] curTfc;
        public double z;
    }

    /**
     * Get the trip route based on the current traffic and the trip rate function (linear function)
     *
     * @param tripRtFunc trip rate function (linear function), let tripRtFunc[i][j] = [a1, a2, ... an], which represents
     *                   the travel time between origin i and destination j to be `a1 + a2 * t + a3 * t^2 + ...`
     * @param curTfc     current traffic, let curTfc[i][j] = t, which represents the travel time between origin i and destination j
     * @return trip rate for each origin-destination pair
     */
    double[][] getTripRtLinear(double[][][] tripRtFunc, double[][] curTfc) {
        double[][] tripRt = new double[curTfc.length][curTfc[0].length];
        for (int i = 0; i < curTfc.length; i++) {
            for (int j = 0; j < curTfc[0].length; j++) {
                tripRt[i][j] = 0;
                for (int k = 0; k < tripRtFunc[i][j].length; k++) {
                    tripRt[i][j] += tripRtFunc[i][j][k] * Math.pow(curTfc[i][j], k);
                }
            }
        }
        return tripRt;
    }

    /**
     * Get the trip route based on the current traffic and the trip rate function (BPR function)
     *
     * @param tripRtFunc tripRtFUnc[i][j] = [Free flow time, B, Capacity, Power]
     * @param curTfc     current traffic, let curTfc[i][j] = t, which represents the travel time between origin i and destination j
     * @return trip rate for each origin-destination pair
     */
    double[][] getTripRtBPR(double[][][] tripRtFunc, double[][] curTfc) {

        double[][] tripRt = new double[curTfc.length][curTfc[0].length];
        for (int i = 0; i < curTfc.length; i++) {
            for (int j = 0; j < curTfc[0].length; j++) {
                if (tripRtFunc[i][j].length == 0) {
                    tripRt[i][j] = 0;
                } else {
                    tripRt[i][j] = tripRtFunc[i][j][0] * (1 + tripRtFunc[i][j][1] * Math.pow(curTfc[i][j] / tripRtFunc[i][j][2], tripRtFunc[i][j][3]));
                }

            }
        }
        return tripRt;
    }

    /**
     * Get the trip route based on the current traffic and the trip rate function, and traffic function type
     *
     * @param tripRtFunc     trip rate function
     * @param curTfc         current traffic
     * @param tripRtFuncType traffic function type, either "linear" or "BPR"
     * @return trip rate for each origin-destination pair
     */
    public double[][] getTripRt(double[][][] tripRtFunc, double[][] curTfc, String tripRtFuncType) {
        if (tripRtFuncType.equals("linear")) {
            return getTripRtLinear(tripRtFunc, curTfc);
        } else if (tripRtFuncType.equals("BPR")) {
            return getTripRtBPR(tripRtFunc, curTfc);
        } else {
            return null;
        }
    }

    /**
     * Calculate Z for linear functions
     *
     * @param tripRtFunc trip rate function (linear function), let tripRtFunc[i][j] = [a1, a2, ... an], which represents
     *                   the travel time between origin i and destination j to be `a1 + a2 * t + a3 * t^2 + ...`
     * @param curTfc     current traffic, let curTfc[i][j] = t, which represents the travel time between origin i and destination j
     */
    double getZLinear(double[][][] tripRtFunc, double[][] curTfc) {
        double z = 0;
        for (int i = 0; i < tripRtFunc.length; i++) {
            for (int j = 0; j < tripRtFunc.length; j++) {
                for (int k = 0; k < tripRtFunc[i][j].length; k++) {
                    z += tripRtFunc[i][j][k] / (k + 1) * Math.pow(curTfc[i][j], (k + 1));
                }
            }
        }
        return z;
    }

    /**
     * Calculate Z for BPR functions
     *
     * @param tripRtFunc tripRtFUnc[i][j] = [Free flow time, B, Capacity, Power]
     * @param curTfc     current traffic, let curTfc[i][j] = t, which represents the travel time between origin i and destination j
     */

    double getZBPR(double[][][] tripRtFunc, double[][] curTfc) {
        double z = 0;
        for (int i = 0; i < tripRtFunc.length; i++) {
            for (int j = 0; j < tripRtFunc.length; j++) {
                if (tripRtFunc[i][j].length != 0) {
                    z += tripRtFunc[i][j][0] * (curTfc[i][j] + tripRtFunc[i][j][1] / (tripRtFunc[i][j][3] + 1) / (Math.pow(tripRtFunc[i][j][2], tripRtFunc[i][j][3])) * Math.pow(curTfc[i][j], tripRtFunc[i][j][3] + 1));
                }
            }
        }
        return z;
    }

    /**
     * Calculate Z for linear or BPR functions
     *
     * @param tripRtFunc     trip rate function
     * @param curTfc         current traffic
     * @param tripRtFuncType traffic function type, either "linear" or "BPR"
     * @return z
     */
    public double getZ(double[][][] tripRtFunc, double[][] curTfc, String tripRtFuncType) {
        if (tripRtFuncType.equals("linear")) {
            return getZLinear(tripRtFunc, curTfc);
        } else if (tripRtFuncType.equals("BPR")) {
            return getZBPR(tripRtFunc, curTfc);
        } else {
            return 0;
        }
    }

    /**
     * Get new all-or-nothing assignment based on current traffic
     *
     * @param tripRtFunc     traffic rate function
     * @param odPs           OD pairs
     * @param curTfc         current traffic
     * @param tripRtFuncType traffic function type, either "linear" or "BPR"
     * @return
     */
    public double[][] getNewTfc(double[][][] tripRtFunc, double[][] odPs, double[][] curTfc, String tripRtFuncType) {
        int size = tripRtFunc.length;
        double[][] curGraph = getTripRt(tripRtFunc, curTfc, tripRtFuncType);
        double[][] newTfc = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                newTfc[i][j] = 0;
            }
        }
        Graph graph = new Graph(curGraph);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (odPs[i][j] > 0) {
                    int[] shortestPath = graph.getShortestPath(i, j);
                    for (int k = 0; k < shortestPath.length - 1; k++) {
                        newTfc[shortestPath[k]][shortestPath[k + 1]] += odPs[i][j];
                    }
                }
            }
        }
        return newTfc;
    }

    /**
     * Calculate the weighted average of X1 and X2, where X1 and X2 are both 2D arrays
     *
     * @param X1     the first 2D array
     * @param X2     the second 2D array
     * @param weight weight of X1, while the weight of X2 would be (1-weight)
     * @return the weighted average array
     */
    double[][] calcWeightedAverage2DArray(double[][] X1, double[][] X2, double weight) {
        int size = X1.length;
        double[][] weightedAverage = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                weightedAverage[i][j] = X1[i][j] * weight + X2[i][j] * (1 - weight);
            }
        }
        return weightedAverage;
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
    public NEOutput frankWolfe(double[][][] tripRtFunc, double[][] odPs, String tripRtFuncType) {

        NEOutput neOutput = new NEOutput();
        int size = tripRtFunc.length;
        // Step 0: find a feasible solution (using the shortest path)
        double[][] curTfc = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                curTfc[i][j] = 0;
            }
        }
        curTfc = getNewTfc(tripRtFunc, odPs, curTfc, tripRtFuncType);
        double z, newZ;
        int iter = 0;

        while (true) {
            // Step 1: solution of linearized sub problem
            double[][] newTfc = getNewTfc(tripRtFunc, odPs, curTfc, tripRtFuncType);
            z = getZ(tripRtFunc, curTfc, tripRtFuncType);

//            System.out.println("Iteration " + iter + ": " + z);

            // Step 2: find optimal step size (we can use the ParTan method as well, below is a simple method)
            double stepSize = 0.5;
            for (int i = 0; i < this.LOOP_LIMIT; i++) {
                // calculate gradient
                double[][] gradientAvg = calcWeightedAverage2DArray(curTfc, newTfc, stepSize);
                double[][] gradientAvgPlusEpsilon = calcWeightedAverage2DArray(curTfc, newTfc, stepSize + this.EPSILON);
                double zAvg = getZ(tripRtFunc, gradientAvg, tripRtFuncType);
                double zAvgPlusEpsilon = getZ(tripRtFunc, gradientAvgPlusEpsilon, tripRtFuncType);
                double gradient = (zAvgPlusEpsilon - zAvg) / this.EPSILON;
                if (gradient < 0) {
                    stepSize += 1 / Math.pow(2, i + 1);
                } else {
                    stepSize -= 1 / Math.pow(2, i + 1);
                }
            }

            // Step 3: update current traffic
            curTfc = calcWeightedAverage2DArray(curTfc, newTfc, stepSize);

            // Step 4: check convergence
            newZ = getZ(tripRtFunc, curTfc, tripRtFuncType);
            if (z - newZ < this.EPSILON) {
                break;
            } else {
                iter++;
            }
        }

        // End step: gather output values
        neOutput.curTfc = curTfc;
        neOutput.z = z;
        return neOutput;
    }
}