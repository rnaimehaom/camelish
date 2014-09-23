package com.im.workflows.curvefit;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;

//import com.im.bioassay.camel.aggregation.DoseResponseResultAggregationStrategy;
//import com.im.bioassay.doseresponse.*;

/**
 * Configures all our Camel routes, components, endpoints and beans
 */
@ContextName("curveBoxContext")
@Startup
@ApplicationScoped
public class CurveFitBoxWorkflows extends RouteBuilder {

    /* The fitter is a component in the workflow that can be configured.
     * Workflow editor will allow these components to be inspected and their params
     * set. In this case using the DoseResponseFitter.getParams() method.
     */
//    private DoseResponseFitter fitter = new DoseResponseFitter();
//
//    public DoseResponseFitter getFitter() {
//        return fitter;
//    }

    @Override
    public void configure() throws Exception {
        // you can configure the route rule with Java DSL here

        // approach 1: all processing is done withing the route. The curve fit
        // is done by a bean
        
        from("file:///Users/timbo/tmp/examples/tobox")
            .to("box://files/uploadFile?inBody=fileUploadRequest&userName=tdudgeon@informaticsmatters.com&userPassword=cQi2UKCs&clientId=dvybmjbotydy7fe8mi9ffdxv87rimp4g&clientSecret=rKancWm6wtcrpc0fLrAtAgPiqWY0gTxO");

        
        from("timer:dummy2?period=10000").log("box timer");
    }

}
