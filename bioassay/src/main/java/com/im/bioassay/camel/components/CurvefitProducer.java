/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.bioassay.camel.components;

import com.im.bioassay.curvefit.FourPLFitterParams;
import com.im.bioassay.doseresponse.DoseResponseFitter;
import com.im.bioassay.doseresponse.DoseResponseResult;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

/**
 *
 * @author timbo
 */
public class CurvefitProducer extends DefaultProducer {
    private CurvefitEndpoint endpoint;
    
    public CurvefitProducer(CurvefitEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        DoseResponseResult result = exchange.getIn().getBody(DoseResponseResult.class);
        DoseResponseFitter fitter = new DoseResponseFitter();
        FourPLFitterParams params = fitter.getParams();
        params.top = endpoint.getTop();
        params.bottom = endpoint.getBottom();
        params.slope = endpoint.getSlope();
        params.inflection = endpoint.getInflection();
        params.initialSlope = endpoint.getInitialSlope();
        params.maxIterations = endpoint.getMaxIterations();
        fitter.fit(result);
    }
    
}
