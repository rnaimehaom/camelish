package com.im.camel.examples.bioassay

import com.im.bioassay.camel.aggregation.DoseResponseResultAggregationStrategy
import org.apache.camel.*
import org.apache.camel.impl.*
import org.apache.camel.builder.*
import com.im.bioassay.doseresponse.*

CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
    def void configure() {
        from("file:///Users/timbo/tmp/examples/ic50?move=orig")
                .log('Processing ${file:name}')
                .split().method(DoseResponseUtils.class, "doseResponseIterator")
                    .aggregationStrategy(new DoseResponseResultAggregationStrategy())
                    .log('Processing record ${property.CamelSplitIndex}')
                    .bean(DoseResponseFitter.class, "fit")
                .end()
                .log("Fitting complete")
                .bean(DoseResponseUtils.class, "fromDoseResponseDataset")
                .log("Finished - writing results to file")
                .to("file:///Users/timbo/tmp/examples/ic50/out")
    }
})
camelContext.start()
sleep(20000)
camelContext.stop()
