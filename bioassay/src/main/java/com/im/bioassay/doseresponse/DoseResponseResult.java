package com.im.bioassay.doseresponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by timbo on 19/04/2014.
 */
public class DoseResponseResult {

    String id;
    IC50 ic50;
    List<Double> xValues;
    List<List<Double>> yValues = new ArrayList<List<Double>>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IC50 getIC50() {
        return ic50;
    }

    public void setIC50(IC50 ic50) {
        this.ic50 = ic50;
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
