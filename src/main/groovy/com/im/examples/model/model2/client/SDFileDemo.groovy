package com.im.examples.model.model2.client

import com.im.examples.model.model2.*
import com.im.examples.model.model2.server.DemoServer

import com.im.camel.processor.ChunkBasedReporter
import com.im.chemaxon.io.MoleculeIOUtils
import com.im.chemaxon.processor.ChemTermsProcessor

import org.apache.camel.*
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.processor.aggregate.AggregationStrategy

import org.apache.camel.builder.RouteBuilder

/** this simulates a client of the server that creates a dataset by loading data
 * from an SD file
 */

def server = new DemoServer()
CamelContext camelContext
try {
      
    execute("Cleaning...") {
        server.clean()
        println "  clean complete"
    }
    
    camelContext = new DefaultCamelContext()
    
    def aggregator = new MapToDataSetAggregationStrategy()
    def ctProcessor = new ChemTermsProcessor()
    ctProcessor.add('logP()', 'logp')
    ctProcessor.add('psa()', 'psa')
    

    aggregator.server = server
    
    camelContext.addRoutes(new RouteBuilder() {
            def void configure() {

                from('direct:fileloader')
                .unmarshal().gzip()
                .split().method(MoleculeIOUtils.class, 'mrecordIterator').streaming().aggregationStrategy(aggregator)
                .process(new ChunkBasedReporter(100))
                .end()
                .log("Finished")
                
                
                from('direct:propcalculator')
                .split(body())
                //.process(ctProcessor)
                .log('processing ${body}')
                
            }     
         })
    
    camelContext.start()
    
    ProducerTemplate t = camelContext.createProducerTemplate()
    execute("Processing SDF...") {
        String file = 'data/src_files/dhfr_standardized.sdf.gz'
        println "  loading file $file"
        t.sendBodyAndHeader('direct:fileloader', new File(file), 'DataSetName', 'dhfr_standardized.sdf')
        println "  file processed"
    }
    
    println "Server now contains these datasets:"
    server.datasets.each { k,v ->
        println "  $k -> ${v.members.size()} items"
    }
    
    execute("Adding properties...") {
        List<RowSet.Row> rows = server.fetchData('dhfr_standardized.sdf', null)
        println "  processing ${rows.size()} rows"
        t.sendBody('direct:propcalculator', rows)
    }
    
} finally {
    camelContext?.stop()
    println "Context stopped"
    server.close()
    println "Server closed"
}


static def execute(String desc, Closure closure) {
    println desc
    long t0 = System.currentTimeMillis()
    def result = closure()
    long t1 = System.currentTimeMillis()
    println "  took ${t1-t0}ms"
    return result
}


class MapToDataSetAggregationStrategy implements AggregationStrategy {
    
    DemoServer server
 
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        DataSet dataset = findDataset(oldExchange, newExchange)
        Map data = newExchange.in.getBody(Map.class)
        Integer index = newExchange.getProperty(Exchange.SPLIT_INDEX, Integer.class)
        dataset.createRow(index, data)
        newExchange.in.body = dataset
        return newExchange
    }
    
    private DataSet findDataset(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            // first record so we need to create the new dataset
            String datasetName = newExchange.in.getHeader('DataSetName')
            println "Creating dataset named datasetName"
            return server.createDataSet(datasetName)
        } else {
            return oldExchange.in.getBody(DataSet.class)
        }
    }
    
}

