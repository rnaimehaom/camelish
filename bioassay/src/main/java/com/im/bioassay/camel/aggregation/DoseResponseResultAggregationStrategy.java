package com.im.bioassay.camel.aggregation;

import com.im.bioassay.doseresponse.DoseResponseDataset;
import com.im.bioassay.doseresponse.DoseResponseResult;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

/**
 * Created by timbo on 20/04/2014.
 */
public class DoseResponseResultAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        DoseResponseResult result = newExchange.getIn().getBody(DoseResponseResult.class);
        DoseResponseDataset dataset;
        if (oldExchange == null) {
            dataset = new DoseResponseDataset();
            dataset.getResults().add(result);
            newExchange.getIn().setBody(dataset);
            return newExchange;
        } else {
            dataset = oldExchange.getIn().getBody(DoseResponseDataset.class);
            dataset.getResults().add(result);
            return oldExchange;
        }
    }
}
