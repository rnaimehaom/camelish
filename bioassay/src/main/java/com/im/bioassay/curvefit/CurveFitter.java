/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.bioassay.curvefit;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public abstract class CurveFitter<T extends CurveModel> {

    private static final Logger LOG = Logger.getLogger(CurveFitter.class.getName());

    /**
     * Calculate the Y for the given X
     *
     * @param model The curve model to fit to
     * @param x The specified X value
     * @return The calculated Y value
     */
    public abstract double calculateY(T model, double x);

    public static double findMin(double[] aa) {
        double min = Double.MAX_VALUE;
        for (double d : aa) {
            min = Math.min(min, d);
        }
        return min;
    }

    public static double findMax(double[] aa) {
        double max = Double.MIN_VALUE;
        for (double d : aa) {
            max = Math.max(max, d);
        }
        return max;
    }

    /**
     * Calculate the sum of squares against the model for the given X-Y values
     *
     * @param x the set of X values
     * @param y the set of Y values
     * @return The sum of the squares
     */
    public double calculateSumOfSquares(double[] x, double[] y, T model) {

        double sumOfSquareYs = 0.00;
        double[] fit = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            double dataY = y[i];
            fit[i] = calculateY(model, x[i]);
            sumOfSquareYs += Math.pow(dataY - fit[i], 2);
        }
        LOG.log(Level.FINE, "SumOfSquares = {0}", sumOfSquareYs);
        return sumOfSquareYs;
    }

    protected T findBestModel(List<T> models) {
        T best = null;
        for (T model : models) {
            if (best == null) {
                best = model;
            } else if (best.getSumSquares() > model.getSumSquares()) {
                best = model;
            }
        }
        return best;
    }
}
