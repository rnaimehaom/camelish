package com.im.workflows.curvefit;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.cdi.Uri;

import com.im.bioassay.camel.aggregation.DoseResponseResultAggregationStrategy;
import com.im.bioassay.doseresponse.*;

/**
 * Configures all our Camel routes, components, endpoints and beans
 */
@ContextName("curveFitCamelContext")
@Startup
@ApplicationScoped
public class CurveFitWorkflow extends RouteBuilder {

    @Inject
    @Uri("file://target/out")
    private Endpoint resultEndpoint;

    /* The fitter is a component in the workflow that can be configured.
    * Workflow editor will allow these components to be inspected and their params
    * set. In this case using the DoseResponseFitter.getParams() method.
    */
    private DoseResponseFitter fitter = new DoseResponseFitter();
    public DoseResponseFitter getFitter() { return fitter; }

    @Override
    public void configure() throws Exception {
        // you can configure the route rule with Java DSL here
        
        from("file:///Users/timbo/tmp/examples/ic50?move=orig")
                .log("Processing ${file:name}")
                .split().method(DoseResponseUtils.class, "doseResponseIterator")
                    .aggregationStrategy(new DoseResponseResultAggregationStrategy())
                    .log("Processing record ${property.CamelSplitIndex}")
                    .bean(fitter, "fit")
                    .log("Procesed record ${property.CamelSplitIndex}")
                .end()
                .log("Fitting complete")
                .bean(DoseResponseUtils.class, "fromDoseResponseDataset")
                .log("Finished - writing results to file")
                .to("file:///Users/timbo/tmp/examples/ic50/out");
    }

    public Endpoint getResultEndpoint() {
        return resultEndpoint;
    }
}
