package com.im.examples

import chemaxon.jchem.db.JChemSearch
import chemaxon.sss.search.JChemSearchOptions
import com.im.chemaxon.camel.db.JChemSearcher
import com.im.chemaxon.io.MoleculeIOUtils
import groovy.util.logging.Log
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.processor.aggregate.AggregationStrategy
import java.sql.Connection
import java.sql.DriverManager
import java.util.logging.Logger

Logger logger = Logger.getLogger(this.class.getName())
JChemSearchOptions opts = new JChemSearchOptions(JChemSearch.SIMILARITY)
opts.dissimilarityThreshold = 0.25f
JChemSearcher searcher = new JChemSearcher('MAYBRIDGESCREENINGCOLLECTION', opts) {
    
    @Override
    protected void handleSearchParams(Exchange exchange, JChemSearch jcs) {
        jcs.queryStructure = exchange.context.typeConverter.convertTo(String.class, exchange.in.body)
    }

    @Override
    protected void handleSearchResults(Exchange exchange, JChemSearch jcs) {
        SearchResult result = new SearchResult(query: jcs.queryStructure, hits: jcs.results, scores: jcs.dissimilarity)
        logger.fine("Found ${jcs.results.length} hits")
        exchange.out.setBody(result)
    }
}
searcher.connection = DriverManager.getConnection('jdbc:derby:/Users/timbo/IJCProjects/SearchableDBs/.config/localdb/db;upgrade=true')

CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            from("direct:filesgohere")
            .log('Processing ${file:name}')
            .split().method(MoleculeIOUtils.class, "mrecordIterator")
            .aggregationStrategy(new SimSearchResultAggregator())
            .log('Processing record ${property.CamelSplitIndex}')
            .process(searcher) // runs the similarity search
            .end()
            .log("Searching complete")
            .process() { exch -> // remove entries that are too similar to a starting molecule
                exch.in.body.filter(0.05)
            }
            .process() { exch -> // print out the cd_ids and scores
                exch.in.body.hitscores.each { k, v -> println "$k => $v" }
                println "Total of ${exch.in.body.hitscores.size()} hits"
            }
        }
    })

camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
t.sendBody('direct:filesgohere', new File('/Users/timbo/IJCProjects/SearchableDBs/66-randoms.sdf'))
sleep(2000)
camelContext.stop()

try {
    DriverManager.getConnection('jdbc:derby:/Users/timbo/IJCProjects/SearchableDBs/.config/localdb/db;shutdown=true')
} catch (Exception e) {
    logger.info("DB shutdown")
}
// END of main script

class SearchResult {
    String query
    int[] hits
    float[] scores
}
@Log
class ResultAggregate {
    Map<Integer, Float> hitscores = [:]
    
    public void filter(float filter) {
        Iterator iter = hitscores.iterator()
        while (iter.hasNext()) {
            Map.Entry e = iter.next()
            if (e.value < filter) {
                iter.remove() 
                log.info("Removed ${e.key} with score of ${e.value}")
            }
        }
    }
}

class SimSearchResultAggregator implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        SearchResult result = newExchange.in.body
        if (oldExchange == null) {
            ResultAggregate agg = new ResultAggregate()
            result.hits.eachWithIndex { cdid, index ->
                agg.hitscores[cdid] = result.scores[index]
            }
            newExchange.in.body = agg
            return newExchange
        } else {
            ResultAggregate agg = oldExchange.in.body
            result.hits.eachWithIndex { cdid, index ->
                Float currentScore = agg.hitscores.get(cdid)
                float newScore = result.scores[index]
                if (currentScore == null) {
                    agg.hitscores.put(cdid, newScore)
                } else if (currentScore > newScore) {
                    agg.hitscores.put(cdid, newScore)
                } 
            }
            return oldExchange
        }
    }
}