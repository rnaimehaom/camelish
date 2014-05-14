package com.im.bioassay.doseresponse;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Managed Ventures, LLC
 * @version 1.0
 */

import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * This class is responsible for performing the work to calculate the best fit
 * IC50 curve, based on the empirical data provided.  There are only two public
 * methods, plus one get/set pair.  The two methods enable the calling method
 * to perform the calculation with either the default MAX inhibition value, or
 * the calling method can explicitly set the MAX value.  The get/set pair enables
 * the calling method to update the Concentration threshold at which the
 * calculation will halt.  The default threshold is set to a 0.1% change in
 * concentration.  The other stopping conditions can be updated in future versions
 * of the method.  <p>
 * <p/>
 * This method will utilize a 9-point algorithm, based on two variable parameters,
 * each of which can have three potential values: increased, decreased, or no change.
 * The public method will prepare an initial estimate for the starting point of
 * the IC50 from the empirical results, selecting the concentration that is the
 * median of the results, and 1.0 for the Hill Coefficient.  After each iteration,
 * the direction and amount of change in Hill and IC50 will be calculated.  As
 * each subsequent guess gets closer and closer to the actual values (as determined
 * by the Least Sum of Squares algorithm), the window in which the calculations
 * take place will shrink, converging on the actual values.  The accuracy/convergence
 * is measured by taking the difference between the reference IC50/Hill pair and
 * the pair which has the least sum of squares.  The lower of the two becomes the
 * reference point, and the process continues.  As the same reference point continues to
 * persist and is again the "winner", then the change in concentration is
 * reduced in an effort to converge on the correct value.
 * <p/>
 * To stop, the algorithm checks the following conditions:
 * concentrationDeviation &lt; CONC_THRESHOLD)
 * sumOfSquaresDeviation &lt; 0.001
 * CONC_INCREASE &lt; CONC_THRESHOLD
 * <p/>
 * Upon reaching each of these simultaneously, the algorithm will then set the
 * current reference point as the best fit IC50 value and return this to the
 * calling method.
 * <p/>
 * If the reference point's concentration ever exceeds the maximum observed
 * concentration, the method will halt, and return the last reference point.
 * This implies that the empirical evidence was insufficient to calculate an
 * accurate IC50 value.
 * <p/>
 * The 4PL equation is:
 * F(x) = D+(A-D)/(1+(x/C)^B)
 * where:
 * A = Minimum asymptote. In a bioassay where you have a standard curve, this can be thought of
 * as the response value at 0 standard concentration.
 *  
 * B = Hill's slope. The Hill's slope refers to the steepness of the curve. It could either be
 * positive or negative.
 *  
 * C = Inflection point. The inflection point is defined as the point on the curve where the
 * curvature changes direction or signs. C is the concentration of analyte where y=(D-A)/2.
 *  
 * D = Maximum asymptote. In an bioassay where you have a standard curve, this can be thought of
 * as the response value for infinite standard concentration.
 * <p/>
 *
 * @author Chris Palmer, Tim Dudgeon
 * @version 1.0
 */

public class IC50CurveFitter {

    private static Logger log = Logger.getLogger(IC50CurveFitter.class.getName());

    private double CONC_THRESHOLD = 0.001; // 0.1% change
    private double MINIMUM_INHIBITION_DEFAULT = 0.0;
    private double MAXIMUM_INHIBITION_DEFAULT = 100;
    private double INITIAL_HILL_COEFFICIENT = 1.0;

    /**
     * @param concValues
     * @param inhibitionValues
     * @return
     */
    public IC50 calcBestIC50(double[] concValues, double[] inhibitionValues) {
        return this.calcBestIC50(concValues, inhibitionValues, MAXIMUM_INHIBITION_DEFAULT, MINIMUM_INHIBITION_DEFAULT);
    }


    public IC50 calcBestIC50(double[] concValues, double[] inhibitionValues, double maxInhibitionValue) {
        return this.calcBestIC50(concValues, inhibitionValues, MAXIMUM_INHIBITION_DEFAULT, MINIMUM_INHIBITION_DEFAULT);
    }

    //added this signature to enable manual setting of both the Min and Max percent inhibitions.
    public IC50 calcBestIC50(double[] concValues, double[] inhibitionValues, double maxInhibitionValue, double minInhibitionValue) {
        double bottom = minInhibitionValue;
        double top = maxInhibitionValue; //inhibitionValues[inhibitionValues.length-1];
        double hill = INITIAL_HILL_COEFFICIENT;

        //Get the initial IC50 from the estimate and a HILL_COEFFFICIENT of 1.0,
        //then we need to find the magic IC50 value.
        IC50 initIC50 = new IC50(bottom, top, hill, this.estimateConc(inhibitionValues, concValues)); //this.estimateConc(_observedInh,_observedConc)

        IC50 bestEffort = this.calcSS(initIC50, new IC50(0, 0, 0, 0), concValues, inhibitionValues);

        return bestEffort;
    }


    //In case the calling method wants to change the default, 0.1% change
    public void setConcChangeThreshold(double change) {
        this.CONC_THRESHOLD = change;
    }

    public double getConcChangeThreshold() {
        return CONC_THRESHOLD;
    }

    /**
     * This method is the primary one in the class.  It contains the iterative
     * loop that facilitates the search for the best fit IC50.
     *
     * @param initIC50
     * @param zeroIC50
     * @param observedConc
     * @param observedInh
     * @return
     */
    private IC50 calcSS(IC50 initIC50, IC50 zeroIC50, double[] observedConc, double[] observedInh) {

        //First thing we do is to set the amount of the hill change and the
        //amount of the concentration change.  Because these haven't been set
        // we expect that we have not yet found our direction...
        determineGlobalParamChange(initIC50, zeroIC50);

        //This is used to check that we don't have a runaway calculation that
        //can never stop!
        double maxObservedConc = findMax(observedConc);
        int num = 0;
        IC50 refIC50 = initIC50;
        IC50 bestEffortIC50 = null;
        double lss = 0;
        double curSS = 0.0;
        boolean ic50TooHigh = false;
        IC50[] cycle = new IC50[9];
        long tim = System.currentTimeMillis();
        do {
            //System.err.println("Current Best:: c: " + refIC50.getConc() + " h: " + refIC50.getHill());
            num++;
            //build our 9-points of interest, setting each parameter in every direction (increase, decrease, no change).
            //This gives us 2 parameters that can change, each can be one of 3 potential values --> 9 combinations, thus 9 points of interest
            //This is the crux of the algorithm...
            cycle[SS] = (IC50) refIC50.clone();
            cycle[SU] = new IC50(refIC50.getBottom(), refIC50.getTop(), refIC50.getHill(), refIC50.getConc() + CONC_INCREASE);
            cycle[DU] = new IC50(refIC50.getBottom(), refIC50.getTop(), refIC50.getHill() + HILL_DECREASE, refIC50.getConc() + CONC_INCREASE);
            cycle[DS] = new IC50(refIC50.getBottom(), refIC50.getTop(), refIC50.getHill() + HILL_DECREASE, refIC50.getConc());
            cycle[DD] = new IC50(refIC50.getBottom(), refIC50.getTop(), refIC50.getHill() + HILL_DECREASE, refIC50.getConc() + CONC_DECREASE);
            cycle[SD] = new IC50(refIC50.getBottom(), refIC50.getTop(), refIC50.getHill(), refIC50.getConc() + CONC_DECREASE);
            cycle[UD] = new IC50(refIC50.getBottom(), refIC50.getTop(), refIC50.getHill() + HILL_INCREASE, refIC50.getConc() + CONC_DECREASE);
            cycle[US] = new IC50(refIC50.getBottom(), refIC50.getTop(), refIC50.getHill() + HILL_INCREASE, refIC50.getConc());
            cycle[UU] = new IC50(refIC50.getBottom(), refIC50.getTop(), refIC50.getHill() + HILL_INCREASE, refIC50.getConc() + CONC_INCREASE);

            //get the Sum of Squares for each of the points to determine the best guess
            for (int i = 0; i < cycle.length; i++) {
                double d = this.sumOfSquares(observedConc, observedInh, cycle[i]);
                cycle[i].setSumSquares(d);
            }
            //figure out the best effort...
            int bestIndex = findLeastSS(cycle);
            //set the bestEffort to the best!
            bestEffortIC50 = cycle[bestIndex];

    /*figure out the new changes for parameters.  We are either moving the box
      within which we are searching (i.e. trying to find our direction),
      or trying to converge on a point within a box that has quit moving!  */
            determineGlobalParamChange(bestEffortIC50, refIC50);

            /** Here we will get the metrics of the current iteration and compare
             *  them to the previous efforts to see if we have reached a small
             *  enough interval to enable us to stop. */
            curSS = bestEffortIC50.getSumSquares();
            double concentrationDeviation = Math.sqrt(Math.pow((refIC50.getConc() - bestEffortIC50.getConc()), 2));
            if (concentrationDeviation < CONC_THRESHOLD)
                stop = true;
            double sumOfSquaresDeviation = ((curSS - lss) / curSS);
            if ((stop) && (sumOfSquaresDeviation < 0.001) && (CONC_INCREASE < CONC_THRESHOLD)) {
                bestEffortIC50.setSumSquares(curSS);
                bestEffortIC50.setIC50Modifier("");
                stop = true;
            } else
                stop = false;

            /** preparing for next iteration!! We set the Least Sum of Squares to
             the lowest SS from the past iteration, to enable a comparison so
             we can make sure we are decreasing, i.e. getting closer to 0 */
            lss = ((curSS == 0.0) ? 1.0 : curSS);
            refIC50 = bestEffortIC50;

            if (refIC50.getConc() > maxObservedConc) {
                refIC50.setIC50Modifier(">");
                refIC50.setConc(maxObservedConc);
                ic50TooHigh = true;
                log.info("IC50 value is higher than the MAX observed concentration. \n" +
                        " IC50 has been reset to the max with a modifier of Modi: " + refIC50.getIC50Modifier());
                stop = true;
            }

        } while (!stop);

        log.fine("Conc: " + refIC50.getConc());
        log.fine("Hill: " + refIC50.getHill());
        log.fine("Number of iterations: " + num);
        log.fine("Time in millis: " + (System.currentTimeMillis() - tim));
        log.fine("Modi: " + refIC50.getIC50Modifier());

        return refIC50;
    }

    /**
     * This method handles the amount and direction of change for the Hill and
     * IC50 value.  Based on the evaluation of the reference to the best estimate,
     * the method will determine whether or not to converge the values, or to
     * move the window in which the calculations take place.
     *
     * @param currentBest
     * @param referencePoint
     */
    private void determineGlobalParamChange(IC50 currentBest, IC50 referencePoint) {
        double deltaHill = currentBest.getHill() - referencePoint.getHill();
        double deltaConc = currentBest.getConc() - referencePoint.getConc();

        if ((concChangeSet) && (hillChangeSet)) {
            if (deltaHill > 0) { // the move from ref --> curr was an increase in hill
                currentBest.setHillDirection(1);
                if (referencePoint.getHillDirection() < 0) {//--> the reference point came from a DECREASE in HILL
                    this.MIN_HILL = referencePoint.getHill();
                    if (MIN_HILL < MAX_HILL)
                        this.hillConverging = true;
                }
            } else if (deltaHill < 0) {
                currentBest.setHillDirection(-1);
                //may not need the following if statement
                if (referencePoint.getHillDirection() > 0) { //--> the reference point came from an INCREASE in HILL
                    this.MAX_HILL = referencePoint.getHill();
                    if (MAX_HILL > MIN_HILL)//may not need this entire if statement
                        this.hillConverging = true;
                }
            }//this means that the hill didn't change....
            else {
                HILL_NO_CHANGE++;
                currentBest.setHillDirection(0);
            }
            if (MAX_HILL > MIN_HILL) {//this implies that both have been set...
                this.hillConverging = true;
            }

            if (deltaConc > 0) { // the move from ref --> curr was an increase in conc
                CONC_NO_CHANGE = 0;
                currentBest.setConcDirection(1);
                if (referencePoint.getConcDirection() < 0) {//--> the reference point came from a DECREASE in CONC
                    this.MIN_CONC = referencePoint.getConc();
                    if (MIN_CONC < MAX_CONC)
                        this.concConverging = true;
                }
            } else if (deltaConc < 0) {
                CONC_NO_CHANGE = 0;
                currentBest.setConcDirection(-1);
                if (referencePoint.getConcDirection() > 0) { //--> the reference point came from an INCREASE in CONC
                    this.MAX_CONC = referencePoint.getConc();
                    if (MAX_CONC > MIN_CONC)
                        this.concConverging = true;
                }
            }//this means that the conc didn't change....
            else {
                CONC_NO_CHANGE++;
                currentBest.setConcDirection(0);
            }


            if ((hillConverging) && (concConverging)) {
                CONC_INCREASE = Math.abs(MAX_CONC - currentBest.getConc()) / 2;
                CONC_DECREASE = -1 * Math.abs((currentBest.getConc() - MIN_CONC) / 2);

                HILL_INCREASE = Math.abs(MAX_HILL - currentBest.getHill()) / 2;
                HILL_DECREASE = -1 * Math.abs(currentBest.getHill() - MIN_HILL) / 2;
            } else {
                if (CONC_NO_CHANGE < 2) {
                    if ((MAX_CONC > 0))
                        CONC_INCREASE = Math.min(Math.abs(MAX_CONC - currentBest.getConc()) / 2, 0.5);
                    else {
                        if (deltaConc > 50)
                            CONC_INCREASE = deltaConc / 2;
                        else
                            CONC_INCREASE = 0.5;//Math.min(Math.abs(currentBest.getConc()-referencePoint.getConc())/2,0.5);
                    }
                    if (MIN_CONC < 10000)
                        CONC_DECREASE = Math.max((-1 * Math.abs((currentBest.getConc() - MIN_CONC)) / 2), -5.0);
                    else
                        CONC_DECREASE = -1 * CONC_INCREASE;
                } else {
                    double m = Math.max(Math.abs(currentBest.getConc() - referencePoint.getConc()), CONC_INCREASE);
                    CONC_INCREASE = Math.min(m / 2, 0.5);
                    CONC_DECREASE = -1 * CONC_INCREASE;
                }
            }
        } else {
            CONC_INCREASE = (currentBest.getConc() - referencePoint.getConc()) / 2;
            CONC_DECREASE = -1 * ((currentBest.getConc() - referencePoint.getConc()) / 2);
            concChangeSet = true;
            HILL_INCREASE = 0.1;//Math.abs(currentBest.getHill() - referencePoint.getHill());
            HILL_DECREASE = -0.1;//HILL_INCREASE/-2;
            hillChangeSet = true;
            return;
        }
    }


    private int findLeastSS(IC50[] ss) {
        double lss = 9e8;
        int ptr = 0;
        for (int i = 0; i < ss.length; i++) {
            double curSS = ss[i].getSumSquares();
            if (curSS < lss) {
                lss = curSS;
                ptr = i;
            }
        }
        return ptr;
    }

    private double findLeastSSNum(IC50[] ss) {
        double lss = 9e8;
        int ptr = 0;
        for (int i = 0; i < ss.length; i++) {
            double curSS = ss[i].getSumSquares();
            if (curSS < lss)
                lss = curSS;
        }
        return lss;
    }

    /**
     * Provides initial estimate of concentration that achieves 50% inhibition
     *
     * @param inh
     * @param conc
     * @return
     */
    private double estimateConc(double[] inh, double[] conc) {
        double lastDistance = 9e8;
        double meanY = 50.0;
        int eIC50 = 0;
        for (int i = 0; i < inh.length; i++) {
            double distance = 0.00;
            if (inh[i] < 0) {
                double aInh = Math.abs(inh[i]) + meanY;
                if (aInh < lastDistance) {
                    lastDistance = aInh;
                    eIC50 = i;
                }
            } else if (inh[i] > meanY) {
                distance = inh[i] - meanY;
                if (distance < lastDistance) {
                    lastDistance = distance;
                    eIC50 = i;
                }
            } else {
                distance = meanY - inh[i];
                if (distance < lastDistance) {
                    lastDistance = distance;
                    eIC50 = i;
                }
            }

        }
        log.fine("Estimated IC50:" + conc[eIC50]);
        return conc[eIC50];
    }

    private double findMax(double[] aa) {
        double max = 0.0;
        for (int i = 0; i < aa.length; i++) {
            max = Math.max(max, aa[i]);
        }
        return max;
    }

    /**
     * Calculates the sum of squares to quantify the amount of error between the
     * actual values and the theoretical values.
     *
     * @param x
     * @param y
     * @param ic50
     * @return
     */
    private double sumOfSquares(double[] x, double[] y, IC50 ic50) {
        double sumOfSquareYs = 0.00;
        double[] fit = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            double dataY = y[i];
            fit[i] = fitY_Value(x[i], ic50);
            //System.out.println("CONC X: " + x[i] + " actY: " + y[i] + " estY: " + fit[i]);
            sumOfSquareYs += Math.pow(dataY - fit[i], 2);
        }
        ic50.setEstimatedYValues(fit);
        return sumOfSquareYs;
    }

    /**
     * This method fits the Y value in the Dose-Response curve given by the
     * formula:
     * Y =
     *
     * @param x
     * @param ic50
     * @return
     */
    private double fitY_Value(double dd, IC50 ic50) {
        if (ic50.getHill() < 0.25) {
            ic50.setHill(0.25);
        }
        double logIC50 = Math.log(ic50.getConc()) / Math.log(10);
        double logK = Math.log(dd) / Math.log(10);
        double hill = ic50.getHill();
        return ic50.getBottom() + ((ic50.getTop() - ic50.getBottom()) / (1 + Math.pow(10, (hill * (logIC50 - logK)))));
    }//

    private double fitY_Log(double x, IC50 ic50) {
        return new BigDecimal(ic50.getBottom() + (ic50.getTop() - ic50.getBottom()) / (1 + Math.pow(10, ((Math.log(ic50.getConc()) / Math.log(10) - x) * 1.0)))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    private boolean stop = false;
    private int CONC_NO_CHANGE = 0;
    private double CONC_DECREASE = 0.0;
    private double CONC_INCREASE = 0.0;
    private int HILL_NO_CHANGE = 0;
    private double HILL_DECREASE = 0.0;
    private double HILL_INCREASE = 0.0;

    private double MIN_CONC = 100000.0;
    private double MAX_CONC = -1000000.0;
    private double MIN_HILL = 100000.0;
    private double MAX_HILL = -1000000.0;

    private boolean concChangeSet = false;
    private boolean concConverging = false;
    private boolean hillChangeSet = false;
    private boolean hillConverging = false;

    //hill, conc
    private final int SS = 0;
    private final int SU = 1;
    private final int UU = 2;
    private final int US = 3;
    private final int UD = 4;
    private final int SD = 5;
    private final int DD = 6;
    private final int DS = 7;
    private final int DU = 8;


}
