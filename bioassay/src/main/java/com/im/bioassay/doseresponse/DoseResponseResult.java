package com.im.bioassay.doseresponse;

import com.im.bioassay.curvefit.FourPLModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by timbo on 19/04/2014.
 */
public class DoseResponseResult {

    String id;
    FourPLModel fitModel;
    List<Double> xValues;
    List<List<Double>> yValues = new ArrayList<List<Double>>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FourPLModel getFitModel() {
        return fitModel;
    }

    public void setFitModel(FourPLModel model) {
        this.fitModel = model;
    }

    public List<Double> getXValues() {
        return xValues;
    }

    public void setXValues(List<Double> xValues) {
        this.xValues = xValues;
    }

    public List<List<Double>> getYValues() {
        return yValues;
    }

    public void setYValues(List<List<Double>> yValues) {
        this.yValues = yValues;
    }


}
