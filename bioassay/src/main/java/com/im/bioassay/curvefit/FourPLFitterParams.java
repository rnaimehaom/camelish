package com.im.bioassay.curvefit;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class FourPLFitterParams {

    public Double bottom, top, inflection, slope, sumSquares;
    public int maxIterations = 10000;
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
     * Default is zero, so if you know your data is
     * always ascending or descending you can speed up the process slightly by
     * setting to 1 or -1 accordingly.
     */
    public double defaultSlope = 0d;
    
    public FourPLFitterParams() {
        minDeltas.put("inflection", defaultConvergenceCriteria);
        minDeltas.put("slope", defaultConvergenceCriteria);
        minDeltas.put("sumSquares", defaultConvergenceCriteria);
        minDeltas.put("topBottom", defaultConvergenceCriteria);
    }

}
