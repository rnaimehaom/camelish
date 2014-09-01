package com.im.bioassay.curvefit;

/** Model for 4 parameter logistic fit.
 * Most commonly used for IC50 type data.
 *
 * @see {@link FourParmaterLogisticCurveFitter}
 * @author timbo
 */
public class FourPLModel extends CurveModel {

    protected Double bottom, top, inflection, slope, sumSquares;
    protected String modifier;
    protected int slopeDirection, inflectionDirection;

    public FourPLModel() { }
    
    protected FourPLModel(Double bottom, Double top, Double slope, Double inflection, String paramChanged, boolean paramIncreased) {
        this(bottom, top, slope, inflection);
        this.lastParamChanged = paramChanged;
        this.lastParamIncreased = paramIncreased;
    }
    
    public FourPLModel(Double bottom, Double top, Double slope, Double inflection) {
        this.bottom = bottom;
        this.top = top;
        this.inflection = inflection;
        this.slope = slope;
    }

   public FourPLModel(Double bottom, Double top, Double slope, String modifier, Double inflection) {
        this.bottom = bottom;
        this.top = top;
        this.inflection = inflection;
        this.slope = slope;
        this.modifier = modifier;
    }

   public FourPLModel(FourPLModel model) {
        this.bottom = model.bottom;
        this.top = model.top;
        this.inflection = model.inflection;
        this.slope = model.slope;
        this.modifier = model.modifier;
        this.slopeDirection = model.slopeDirection;
        this.inflectionDirection = model.inflectionDirection;
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
     * @return the sumSquares
     */
    public Double getSumSquares() {
        return sumSquares;
    }

    /**
     * @return the modifier
     */
    public String getModifier() {
        return modifier;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("4PL fit model: inflection=").append(inflection)
                .append(", slope=").append(slope)
                .append(", bottom=").append(bottom)
                .append(", top=").append(top)
                //.append(", modifier=").append(modifier)
                .append(", sumSquares=").append(sumSquares)
                .append(", time=").append(getTime())
                .append(", interations=").append(getIterations())
                ;
        return b.toString();
    }

}
