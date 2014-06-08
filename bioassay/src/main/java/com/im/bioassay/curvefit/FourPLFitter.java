package com.im.bioassay.curvefit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Curve fitter for the 4 parameter logistic equation.
 * <p>
 * The 4PL equation is: Y = D+(A-D)/(1+(x/C)^B) where: 
 * <br>
 * A = Minimum asymptote.
 * In a bioassay where you have a standard curve, this can be thought of as the
 * response value at 0 concentration (or infinite for descending curves).
 * <br>
 * B = Hill's slope. The Hill's slope refers to the steepness of the curve. It could
 * either be close to 1 or -1 depending on whether the data is ascending or
 * descending.
 * <br>
 * C = Inflection point. The inflection point is defined as the
 * point on the curve where the curvature changes direction or signs. C is the
 * value of X where Y=(D-A)/2. This is typically referred to as the IC50, EC50
 * etc.
 * <br>
 * D = Maximum asymptote. In an bioassay where you have a standard curve,
 * this can be thought of as the response value for infinite X (or zero X for
 * descending curves)
 * <p/>
 * <p>
 * Typical usage:  <code>
 * double[] x = ...; // X values
 * double[] y = ...; // correspondin Y values
 * FourParmaterLogisticCurveFitter fitter = new FourParmaterLogisticCurveFitter();
 * FourParmaterLogisticCurveModel best = fitter.calcBestModel(x, y);
 * </code> This will will run a fit with all four parameters (A, B, C, D) being
 * optimised. Alternatively, you can fix any of the 4 parameters by using the
 * alternative form of the constructor. e.g.  <code>
 * FourParmaterLogisticCurveFitter fitter = new FourParmaterLogisticCurveFitter(0d, 100d, null, null);
 * FourParmaterLogisticCurveModel best = fitter.calcBestModel(x, y);
 * </code> 
 * will fix the bottom and top values to 0 and 100, but let the
 * inflection point and slope to vary.
 * <p>
 * Various other methods specify parameters of the fitting process, such as
 * convergence criteria. See the setter JavaDocs for details.
 * </p>
 * <p/>
 * This method basically works by making an initial rough estimate for the fit
 * parameters and then making iterative changes to those values (increase and
 * decrease) to iteratively improve the fit (based on the sum of the squares of
 * the divergence of the actual values from the predicted values. The value by
 * which the parameters are incremented/decremented is reduced if the fit cannot
 * be improved until the fit parameters and sum of squares converges.
 * <p/>
 * <p>
 * If the reference point's concentration ever exceeds the maximum observed
 * concentration, the method will halt, and return the last reference point with
 * the &gt; modifier set. Similarly if it becomes less than the min value the
 * &lt; modifier is set.
 * <p/>
 *
 *
 * @author Tim Dudgeon, based on orginal by Chris Palmer
 * @version 1.0
 */
public class FourPLFitter
        extends CurveFitter<FourPLModel> {

    private static final Logger LOG = Logger.getLogger(FourPLFitter.class.getName());

    private Double inflection, slope, bottom, top;
    private int maxIterations = 10000;
    private Map<String, Double> minDeltas = new HashMap<String, Double>();
    private double defaultDelta = 0.25;
    private double defaultConvergenceCriteria = 0.001;
    private double initialSlopeDeltaMin = 0.5d;
    private double initialTopBottomDeltaMin = 1.0d;
    private double defaultSlope = 0d;

    public static void main(String args[]) {
        double[] x = new double[]{1d, 10d, 100d, 1000d, 10000d, 100000d};
        //double[] x = new double[]{1d, 0.1d, 0.01d, 0.001d, 0.0001d, 0.00001d};
        double[] y = new double[]{5d, 9d, 25d, 70d, 90d, 95d};
        //double[] y = new double[]{95d, 90d, 70d, 25d, 9d, 5d};

        FourPLFitter fitter = new FourPLFitter(null, null, null, null);
        FourPLModel best = fitter.calcBestModel(x, y);

        System.out.println("Best: " + best);
    }

    public FourPLFitter() {
        minDeltas.put("inflection", defaultConvergenceCriteria);
        minDeltas.put("slope", defaultConvergenceCriteria);
        minDeltas.put("sumSquares", defaultConvergenceCriteria);
        minDeltas.put("topBottom", defaultConvergenceCriteria);
    }

    /**
     * Constructor to use when you want to fix some of the fit parameters. e.g.
     * fix the min to zero and the max to 100. Those values that are supplied
     * are fixed, those that are null are allowed to vary in the fitting
     * process.
     *
     * @param bottom Corresponding to param A in the 4PL equation
     * @param top Corresponding to param B in the 4PL equation
     * @param inflection Corresponding to param C in the 4PL equation
     * @param slope Corresponding to param B in the 4PL equation
     *
     */
    public FourPLFitter(Double bottom, Double top, Double inflection, Double slope) {
        this();
        this.bottom = bottom;
        this.top = top;
        this.inflection = inflection;
        this.slope = slope;
    }

    /**
     * @return the bottom
     */
    public Double getBottom() {
        return bottom;
    }

    /**
     * @return the top
     */
    public Double getTop() {
        return top;
    }

    /**
     * @return the inflection
     */
    public Double getInflection() {
        return inflection;
    }

    /**
     * @return the slope
     */
    public Double getSlope() {
        return slope;
    }

    /**
     * The maximum number of iterations to use before giving up and returning
     * null. Default is 10,000
     *
     * @return the maxIterations
     */
    public int getMaxIterations() {
        return maxIterations;
    }

    /**
     * Set the maximum number of iterations.
     *
     * @param maxIterations the maxIterations to set
     */
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /** The convergence criteria.
     * A map of values. Keys are bottom, top, slope, inflection.
     * These are set to 0.001 by default.
     * 
     * @return the minDeltas
     */
    public Map<String, Double> getMinDeltas() {
        return minDeltas;
    }

    /**
     * @return the defaultDelta
     */
    public Double getDefaultDelta() {
        return defaultDelta;
    }

    /**
     * @return the defaultSlope
     */
    public Double getDefaultSlope() {
        return defaultSlope;
    }

    /**
     * Set the default slope. Default is zero, so if you know your data is
     * always ascending or descending you can speed up the process slightly by
     * setting to 1 or -1 accordingly.
     *
     * @param defaultSlope the defaultSlope to set
     */
    public void setDefaultSlope(Double defaultSlope) {
        this.defaultSlope = defaultSlope;
    }

    /** Calculate Y give X for the given fit model.
     *
     * @param model
     * @param x
     * @return
     */
    @Override
    public double calculateY(FourPLModel model, double x) {
        double logInflection = Math.log(model.getInflection()) / Math.log(10);
        double logX = Math.log(x) / Math.log(10);
        double y = model.getBottom() + ((model.getTop() - model.getBottom())
                / (1 + Math.pow(10, (model.getSlope() * (logInflection - logX)))));
        LOG.log(Level.FINE, "Calculated Y = {0}", y);
        return y;
    }

    /** the entry point to doing a curve fit.
     * 
     * @param xValues
     * @param yValues
     * @return The best model after convergence is achieved, or null if convergence 
     * does not happen within the specified number of iterations.
     */
    public FourPLModel calcBestModel(double[] xValues, double[] yValues) {

        // create initial model
        FourPLModel initModel = new FourPLModel();

        generateInitialModel(initModel, xValues, yValues);

        Engine e = new Engine();
        FourPLModel bestEffort = e.execute(
                initModel,
                xValues,
                yValues);

        return bestEffort;
    }

    /**
     * Generate initial model values for any that are not specified
     *
     * @param model The model,maybe with some initial values defined
     * @param xValues
     * @param yValues
     */
    private void generateInitialModel(
            FourPLModel model,
            double[] xValues,
            double[] yValues) {
        if (model.inflection == null) {
            // Get the initial X estimate
            model.inflection = estimateInflection(xValues, yValues);
        }
        if (model.slope == null) {
            model.slope = defaultSlope;
        }
        if (this.bottom == null) {
            model.bottom = findMin(yValues);
        } else {
            model.bottom = this.bottom;
        }
        if (this.top == null) {
            model.top = findMax(yValues);
        } else {
            model.top = this.top;
        }
    }

    /**
     * Provides initial estimate of the X value of the inflection point
     *
     * @param x
     * @param y
     * @return
     */
    private double estimateInflection(double[] x, double[] y) {
        double lastDistance = 9e8;
        double meanY = 50.0;
        int eIC50 = 0;
        for (int i = 0; i < y.length; i++) {
            double distance = 0.0d;
            if (y[i] < 0) {
                double aInh = Math.abs(y[i]) + meanY;
                if (aInh < lastDistance) {
                    lastDistance = aInh;
                    eIC50 = i;
                }
            } else if (y[i] > meanY) {
                distance = y[i] - meanY;
                if (distance < lastDistance) {
                    lastDistance = distance;
                    eIC50 = i;
                }
            } else {
                distance = meanY - y[i];
                if (distance < lastDistance) {
                    lastDistance = distance;
                    eIC50 = i;
                }
            }
        }
        LOG.log(Level.FINE, "Estimated IC50:{0}", x[eIC50]);
        return x[eIC50];
    }

    class Engine {

        private Map<String, Double> deltas = new HashMap<String, Double>();
        private boolean stop = false;

        /**
         * This method is the primary one in the class. It contains the
         * iterative loop that facilitates the search for the best fit curve
         * model. Create a new Engine for each fit. Do not re-use.
         *
         * @param init The initial model
         * @param xValues
         * @param yValues
         * @return
         */
        FourPLModel execute(
                FourPLModel initModel,
                double[] xValues,
                double[] yValues) {

            deltas.put("inflection", initModel.inflection * defaultDelta);
            deltas.put("slope", Math.max(initialSlopeDeltaMin, initModel.slope * defaultDelta));
            deltas.put("topBottom", Math.max(initialTopBottomDeltaMin, (initModel.top - initModel.bottom) / 25));

            initModel.sumSquares = calculateSumOfSquares(xValues, yValues, initModel);

            // First thing we do is to set the parameter change.
            determineFitParamChanges(initModel, null);

            //These are used to check that we don't have a runaway calculation that
            //can never stop!
            double maxObservedConc = findMax(xValues);
            double minObservedConc = findMin(xValues);

            FourPLModel refModel = initModel;
            FourPLModel bestModel = null;
            long tim = System.currentTimeMillis();
            int count = 0;
            do {
                count++;
                List<FourPLModel> models = generateNextModels(refModel);
                // get the Sum of Squares for each of the new models to determine the best guess
                // TODO - make this bit multithreaded
                for (FourPLModel model : models) {
                    model.sumSquares = calculateSumOfSquares(xValues, yValues, model);
                    LOG.log(Level.FINE, "Tested model {0}", model);
                }
                //figure out the best
                FourPLModel bestNewModel = findBestModel(models);
                if (bestNewModel.sumSquares < refModel.sumSquares) {
                    System.out.println("Improved model " + bestNewModel);
                    bestModel = bestNewModel;
                    // have we converged yet?
                    stop = hasConverged(bestModel, refModel);

                    if (bestModel.inflection > maxObservedConc) {
                        bestModel.modifier = ">";
                        bestModel.inflection = maxObservedConc;
                        LOG.info("Inflection value is higher than the MAX observed X value. \n"
                                + " Inflection has been reset to the max with a > modifier");
                        stop = true;
                    } else if (bestModel.inflection < minObservedConc) {
                        bestModel.modifier = "<";
                        bestModel.inflection = minObservedConc;
                        LOG.info("Inflection value is lower than the MIN observed X value. \n"
                                + " Inflection has been reset to the min with a < modifier");
                        stop = true;
                    }
                } else {
                    // none of the new ones were any better
                    bestModel = refModel;
                }
                if (count > maxIterations) {
                    LOG.log(Level.WARNING, "Terminating - too many iterations{0}", bestModel);
                    return null;
                }
                if (!stop) {
                    // figure out the new fit parameters.
                    determineFitParamChanges(bestModel, refModel);
                    refModel = bestModel;
                }

            } while (!stop);

            bestModel.setIterations(count);
            bestModel.setTime(System.currentTimeMillis() - tim);
            LOG.log(Level.FINE, "Best fit: {0}", bestModel);
            return bestModel;
        }

        boolean hasConverged(FourPLModel nue, FourPLModel last) {
            LOG.log(Level.FINE, "Checking  {0}\n  against {1}", new Object[]{nue, last});
            if (last.sumSquares < nue.sumSquares || (last.sumSquares - nue.sumSquares) > minDeltas.get("sumSquares")) {
                LOG.fine("Not converged: SS");
                return false;
            }
            double inflectionChange = Math.abs(nue.inflection - last.inflection) / last.inflection;
            if (inflectionChange > minDeltas.get("inflection")) {
                LOG.finer("Not converged: Inflection ");
                return false;
            }
            double slopeChange = Math.abs(nue.slope - last.slope) / last.slope;
            if (slopeChange > minDeltas.get("slope")) {
                LOG.finer("Not converged: Slope ");
                return false;
            }
            double bottomChange = Math.abs(nue.bottom - last.bottom) / last.bottom;
            if (bottomChange > minDeltas.get("topBottom")) {
                LOG.finer("Not converged: Bottom ");
                return false;
            }
            double topChange = Math.abs(nue.top - last.top) / last.top;
            if (topChange > minDeltas.get("topBottom")) {
                LOG.finer("Not converged: Top ");
                return false;
            }
            LOG.fine("Converged");
            return true;
        }

        List<FourPLModel> generateNextModels(FourPLModel refModel) {
            List<FourPLModel> models = new ArrayList<FourPLModel>();
            // TODO - exclude the model we have just come from
            if (inflection == null) {
                models.add(new FourPLModel(refModel.getBottom(), refModel.getTop(), refModel.slope, refModel.inflection + deltas.get("inflection"), "inflection", true));
                models.add(new FourPLModel(refModel.getBottom(), refModel.getTop(), refModel.slope + deltas.get("slope"), refModel.inflection, "slope", true));
            }
            if (slope == null) {
                models.add(new FourPLModel(refModel.getBottom(), refModel.getTop(), refModel.slope, refModel.inflection - deltas.get("inflection"), "inflection", false));
                models.add(new FourPLModel(refModel.getBottom(), refModel.getTop(), refModel.slope - deltas.get("slope"), refModel.inflection, "slope", false));
            }
            if (top == null) {
                models.add(new FourPLModel(refModel.getBottom(), refModel.getTop() + deltas.get("topBottom"), refModel.slope, refModel.inflection, "top", true));
                models.add(new FourPLModel(refModel.getBottom(), refModel.getTop() - deltas.get("topBottom"), refModel.slope, refModel.inflection, "top", false));
            }
            if (bottom == null) {
                models.add(new FourPLModel(refModel.getBottom() + deltas.get("topBottom"), refModel.getTop(), refModel.slope, refModel.inflection, "bottom", true));
                models.add(new FourPLModel(refModel.getBottom() - deltas.get("topBottom"), refModel.getTop(), refModel.slope, refModel.inflection, "bottom", false));
            }
            return models;
        }

        /**
         * This method handles the amount and direction of change for the fit
         * parameters. Based on the evaluation of the reference to the best
         * estimate, the method will determine whether or not to reduce the
         * deltas so that the fit can converge.
         *
         * @param currentBest
         * @param referencePoint
         * @return If no further changes are possible so we should stop fitting
         */
        private void determineFitParamChanges(
                FourPLModel current,
                FourPLModel previous) {

            Map<String, Double> updates = new HashMap<String, Double>();
            if (current == previous) {
                // no change last time so reduce the deltas
                reduceDelta("inflection", updates, 0.5d);
                reduceDelta("slope", updates, 0.5d);
                reduceDelta("topBottom", updates, 0.5d);
                deltas.putAll(updates);
            }
        }

        private void reduceDelta(String name, Map updates, double qty) {
            double currentDelta = deltas.get(name);
            double newDelta = currentDelta * qty;
            updates.put(name, newDelta);
            System.out.println("Updated delta " + name + " to " + updates.get(name));
        }
    }
}
