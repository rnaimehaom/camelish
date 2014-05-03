package com.im.bioassay.doseresponse;

import java.lang.CloneNotSupportedException;

public class IC50 implements Cloneable {

    private double bottom;
    private double top;
    private double hill;
    private double conc;
    private String ic50Modifier;
    private double sumSquares;
    private int refHillDirection;
    private int refConcDirection;

    private double[] estimatedYValues;

    public IC50(double bottom, double top, double hill, double conc) {
        this.bottom = bottom;
        this.top = top;
        this.hill = hill;
        this.conc = conc;
    }

    public IC50() {
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("This should never happen");
        }
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("IC50 [Conc=");
        if (ic50Modifier != null) {
            b.append(ic50Modifier);
        }
        b.append(conc)
                .append(", Hill=")
                .append(hill)
                .append(", Bottom=")
                .append(bottom)
                .append(", Top=")
                .append(top)
                .append(", Fit=")
                .append(sumSquares)
                .append("]");
         return b.toString();
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public double getBottom() {
        return bottom;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public double getTop() {
        return top;
    }

    public void setHill(double hill) {
        this.hill = hill;
    }

    public double getHill() {
        return hill;
    }

    public void setConc(double conc) {
        this.conc = conc;
    }

    public double getConc() {
        return conc;
    }

    public void setSumSquares(double sumSquares) {
        this.sumSquares = sumSquares;
    }

    public double getSumSquares() {
        return sumSquares;
    }

    public int getConcDirection() {
        return refConcDirection;
    }

    public void setConcDirection(int concDirection) {
        this.refConcDirection = concDirection;
    }

    public int getHillDirection() {
        return refHillDirection;
    }

    public void setHillDirection(int hillDirection) {
        this.refHillDirection = hillDirection;
    }

    public double[] getEstimatedYValues() {
        return estimatedYValues;
    }

    public void setEstimatedYValues(double[] estimatedYValues) {
        this.estimatedYValues = estimatedYValues;
    }

    public String getIC50Modifier() {
        return ic50Modifier;
    }

    public void setIC50Modifier(String ic50Modifier) {
        this.ic50Modifier = ic50Modifier;
    }

}
