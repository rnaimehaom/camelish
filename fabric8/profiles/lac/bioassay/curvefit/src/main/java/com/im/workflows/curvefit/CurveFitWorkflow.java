package com.im.workflows.curvefit;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;

import com.im.bioassay.camel.aggregation.DoseResponseResultAggregationStrategy;
import com.im.bioassay.doseresponse.*;

/**
 * Configures all our Camel routes, components, endpoints and beans
 */
@ContextName("curveFitCamelContext")
@Startup
@ApplicationScoped
public class CurveFitWorkflow extends RouteBuilder {

    /* The fitter is a component in the workflow that can be configured.
     * Workflow editor will allow these components to be inspected and their params
     * set. In this case using the DoseResponseFitter.getParams() method.
     */
    private DoseResponseFitter fitter = new DoseResponseFitter();

    public DoseResponseFitter getFitter() {
        return fitter;
    }

    @Override
    public void configure() throws Exception {
        // you can configure the route rule with Java DSL here

        // approach 1: all processing is done withing the route. The curve fit
        // is done by a bean
        
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

        // approach 2: curvefit endpoint is used to process the data. This endpoint
        // is a back end service (e.g. emulating a web service)
        
        from("file:///Users/timbo/tmp/examples/ic50_2?move=orig")
                .log("Processing ${file:name}")
                .split().method(DoseResponseUtils.class, "doseResponseIterator")
                .aggregationStrategy(new DoseResponseResultAggregationStrategy())
                .log("Processing record ${property.CamelSplitIndex}")
                .to("curvefit:default")
                .log("Procesed record ${property.CamelSplitIndex}")
                .end()
                .log("Fitting complete")
                .bean(DoseResponseUtils.class, "fromDoseResponseDataset")
                .log("Finished - writing results to file")
                .to("file:///Users/timbo/tmp/examples/ic50_2/out");

        // approach 3: send to queue to process. The queue has 5 consumers processing 
        // in parallel to improve throughput. In the is case the queue is a seda queue
        // so runnign withing same JVM, but this is supposed to emulate using a JMS
        // queue in a distributed environment.
        // A routing slip is used to dynamically route the results to the required destination
        
        from("file:///Users/timbo/tmp/examples/ic50_queue?move=orig")
                .log("Processing ${file:name}")
                .setHeader("CurveFitDestination", constant("direct:curvefitdestination"))
                .to("seda:curvefit");

        from("seda:curvefit?concurrentConsumers=5")
                .split().method(DoseResponseUtils.class, "doseResponseIterator")
                .aggregationStrategy(new DoseResponseResultAggregationStrategy())
                .log("Processing record ${property.CamelSplitIndex}")
                .bean(fitter, "fit")
                .log("Procesed record ${property.CamelSplitIndex}")
                .end()
                .log("Fitting complete")
                .routingSlip("CurveFitDestination");
        
        from("direct:curvefitdestination")
                .bean(DoseResponseUtils.class, "fromDoseResponseDataset")
                .log("Finished - writing results to file")
                .to("file:///Users/timbo/tmp/examples/ic50_queue/out");

        //from("file:///Users/timbo/tmp/examples/ic50_amq?move=orig")
        //        .log("Processing ${file:name}")
        //        .to("amq:incomingCurvefit");
        from("timer:dummy2?period=10000").log("timer fired");

    }

}
