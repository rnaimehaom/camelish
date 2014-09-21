package com.im.bioassay.camel.components;

import com.im.bioassay.curvefit.FourPLFitterParams;
import java.util.logging.Logger;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;

/**
 *
 * @author timbo
 */
@UriEndpoint(scheme = "curvefit")
public class CurvefitEndpoint extends DefaultEndpoint {

    private static Logger LOG = Logger.getLogger(CurvefitEndpoint.class.getName());

    @UriParam
    private Double top;

    @UriParam
    private Double bottom;
    
    @UriParam
    private Double inflection;
    
    @UriParam
    private Double slope;
    
    @UriParam
    private double initialSlope = FourPLFitterParams.DEFAULT_INITIAL_SLOPE;
    
    @UriParam
    private int maxIterations= FourPLFitterParams.DEFAULT_MAX_ITERATIONS;

    public CurvefitEndpoint(String uri, CurvefitComponent component) {
        super(uri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new CurvefitProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("You can't read messages from this endpoint");
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * @return the top
     */
    public Double getTop() {
        return top;
    }

    /**
     * @param top the top to set
     */
    public void setTop(Double top) {
        LOG.info("Setting top: " + top);
        this.top = top;
    }

    /**
     * @return the bottom
     */
    public Double getBottom() {
        return bottom;
    }

    /**
     * @param bottom the bottom to set
     */
    public void setBottom(Double bottom) {
        this.bottom = bottom;
    }

    /**
     * @return the inflection
     */
    public Double getInflection() {
        return inflection;
    }

    /**
     * @param inflection the inflection to set
     */
    public void setInflection(Double inflection) {
        this.inflection = inflection;
    }

    /**
     * @return the slope
     */
    public Double getSlope() {
        return slope;
    }

    /**
     * @param slope the slope to set
     */
    public void setSlope(Double slope) {
        this.slope = slope;
    }

    /**
     * @return the maxIterations
     */
    public int getMaxIterations() {
        return maxIterations;
    }

    /**
     * @param maxIterations the maxIterations to set
     */
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * @return the initialSlope
     */
    public double getInitialSlope() {
        return initialSlope;
    }

    /**
     * @param initialSlope the initialSlope to set
     */
    public void setInitialSlope(double initialSlope) {
        this.initialSlope = initialSlope;
    }

}
