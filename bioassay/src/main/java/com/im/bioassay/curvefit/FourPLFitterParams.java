package com.im.bioassay.curvefit;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class FourPLFitterParams {

    public static final int DEFAULT_MAX_ITERATIONS = 10000;
    public static final double DEFAULT_INITIAL_SLOPE = 0d;

    public Double bottom, top, inflection, slope, sumSquares;
    public int maxIterations = DEFAULT_MAX_ITERATIONS;
    /**
     * The convergence criteria. A map of values. Keys are bottom, top, slope,
     * inflection. These are set to 0.001 by default.
     */
    public Map<String, Double> minDeltas = new HashMap<String, Double>();
    public double defaultDelta = 0.25;
    public double defaultConvergenceCriteria = 0.001;
    public double initialSlopeDeltaMin = 0.5d;
    public double initialTopBottomDeltaMin = 1.0d;
    /**
     * Default is zero, so if you know your data is always ascending or
     * descending you can speed up the process slightly by setting to 1 or -1
     * accordingly.
     */
    public double initialSlope = DEFAULT_INITIAL_SLOPE;

    public FourPLFitterParams() {
        minDeltas.put("inflection", defaultConvergenceCriteria);
        minDeltas.put("slope", defaultConvergenceCriteria);
        minDeltas.put("sumSquares", defaultConvergenceCriteria);
        minDeltas.put("topBottom", defaultConvergenceCriteria);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Fit params: ");
        if (top != null) {
            b.append("Top: ").append(top);
        }
        if (bottom != null) {
            b.append("Bottom: ").append(bottom);
        }
        return b.toString();
    }

}
