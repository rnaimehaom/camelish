package com.im.examples

import chemaxon.jchem.db.JChemSearch
import chemaxon.sss.search.JChemSearchOptions
import com.im.chemaxon.camel.db.AbstractJChemSearcher
import com.im.chemaxon.io.MoleculeIOUtils
import groovy.util.logging.Log
import groovy.sql.Sql
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

ConfigObject database = new ConfigSlurper().parse(new File('loaders/database.properties').toURL())
ConfigObject drugbank = new ConfigSlurper().parse(new File('loaders/drugbank.properties').toURL())
Sql db = Sql.newInstance(database.url, 'vendordbs', 'vendordbs')

AbstractJChemSearcher searcher = new AbstractJChemSearcher(drugbank.table, 'sep=, t:i,dissimilarityThreshold:0.25') {
    
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
searcher.connection = db.connection

CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            
            from("file:data/filedrop/JChemSearcherExample2?move=out")
            .log('Processing ${file:name}')
            .to('direct:uncompressFile')
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
            
            from('direct:uncompressFile')
            .choice()
            .when(header("CamelFileName").endsWith(".gz")).unmarshal().gzip()
            .when(header("CamelFileName").endsWith(".zip")).unmarshal().zip()
            .endChoice()
                
        }
    })

camelContext.start()

println "Ready to process. Drop files into data/filedrop/JChemSearcherExample2 to process"
println "Will run for 5 mins or use Ctrl-C to finish"

sleep(300000)
db.close()

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