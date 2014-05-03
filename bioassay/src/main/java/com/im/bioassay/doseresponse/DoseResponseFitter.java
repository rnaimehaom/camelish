package com.im.bioassay.doseresponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by timbo on 19/04/2014.
 */
public class DoseResponseFitter {

    IC50CurveFitter fitter = new IC50CurveFitter();

    public void fit(DoseResponseResult result) {

        // there could be multiple sets of Y values so we need to explode the lists.
        List<Double> xVals = new ArrayList<Double>();
        List<Double> yVals = new ArrayList<Double>();
        for (List<Double> yList : result.getYValues()) {
            xVals.addAll(result.getXValues()); // same X values
            yVals.addAll(yList);               // different Y values
        }
        assert xVals.size() == yVals.size();

        // and now build the double[] arrays (is there a simpler way?)
        double[] x = new double[xVals.size()];
        double[] y = new double[xVals.size()];

        int i = 0;
        for (Double d : xVals) {
            x[i] = d;
            i++;
        }
        i = 0;
        for (Double d : yVals) {
            y[i] = d;
            i++;
        }

        IC50 ic50 = fitter.calcBestIC50(x, y);
        result.setIC50(ic50);
    }
}
