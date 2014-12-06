package com.im.examples.model.model2.client

import chemaxon.struc.Molecule

import com.im.examples.model.model2.*
import com.im.examples.model.model2.server.DemoServer

import com.im.camel.processor.ChunkBasedReporter
import com.im.camel.processor.ResultExtractor
import com.im.chemaxon.io.MoleculeIOUtils
import com.im.chemaxon.molecule.MoleculeConstants;
import com.im.chemaxon.processor.ChemTermsProcessor

import org.apache.camel.*
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.processor.aggregate.AggregationStrategy

import org.apache.camel.builder.RouteBuilder

/** this simulates a client of the server that creates a dataset by loading data
 * from an SD file and then enriches it with some calcuated properties
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
    aggregator.server = server
    
    camelContext.addRoutes(new RouteBuilder() {
            def void configure() {

                from('direct:fileloader')
                .unmarshal().gzip()
                .split().method(MoleculeIOUtils.class, 'mrecordIterator').streaming().aggregationStrategy(aggregator)
                .process(new ChunkBasedReporter(100))
                .end()
                .log("Finished")
             

                from('direct:propertyEnricher')
                .split(body())
                //.log('before ${body}')
                .process(new MoleculeBasedRowEnricher(new ChemTermsProcessor().
                            add('logP()', 'logp').
                            add('atomCount()', 'atom_count')
                    ))
                .log('after ${body}')
                
            }     
        })
    
    camelContext.start()
    
    ProducerTemplate t = camelContext.createProducerTemplate()
    execute("Processing SDF...") {
        String file = 'data/src_files/dhfr_standardized.sdf.gz'
        println "  loading file $file"
        t.sendBodyAndHeader('direct:fileloader', new File(file), 'DataSetName', 'dhfr_standardized')
        println "  file processed"
    }
    
    println "Server now contains these datasets:"
    server.datasets.each { k,v ->
        println "  $k -> ${v.members.size()} items"
    }
    
    execute("Adding properties...") {
        List<RowSet.Row> rows = server.fetchData('dhfr_standardized', null)
        println "  processing ${rows.size()} rows"
        //t.sendBody('direct:propertyEnricher', rows)
        t.sendBody('direct:propertyEnricher', rows)
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

// TODO move to outer class 
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

/** Allows a Row to be enriched with Molecule-based properties.
 * Processes a RowSet.Row but extracts the Molecule property (creating it from 
 * text representation if necessary), substituting the Row in the body for the Molecule,
 * delegating to another processor that processes the Molecule and calculates some
 * properties, then sets those propeties back to the Row and sets the Row back as
 * the body of the Exchange.
 * This class should be moved out to be mode more generally useful, but not clear where
 * to put it as it depends on datamodel and chemaxon. Maybe it can be made more generic
 * and independent of chemaxon by use of generics?
 * 
 */
class MoleculeBasedRowEnricher implements Processor, MoleculeConstants {
    
    private Processor processor
    private ResultExtractor extractor
    
    /** Constructor for a standard Processor and independent extractor
     */
    MoleculeBasedRowEnricher(Processor processor, ResultExtractor extractor) {
        this.processor = processor
        this.extractor = extractor
    }
    
    /** Single arg constructor for use when the Processor is also the ResultExtractor
     */ 
    MoleculeBasedRowEnricher(Processor processorExtractor) {
        this(processorExtractor, (ResultExtractor)processorExtractor)
    }
    
    void process(Exchange exchange) {
        RowSet.Row row = exchange.in.getBody(RowSet.Row.class)
        // TODO - make these properties configuration through header properties
        Molecule mol = row.data[__MOLECULE_FIELD_NAME]
        if (mol == null) { 
            String molstr = row.data[STRUCTURE_FIELD_NAME]
            if (molstr != null) {
                mol = MoleculeIOUtils.convertToMolecule(molstr)
                row.data[__MOLECULE_FIELD_NAME] = mol
            } else {
                println "WARNING: no molecule found, can't calculate"
                return
            }
        }
        try {
            exchange.in.body = mol
            processor.process(exchange)
            Map results = extractor.extractResults(mol)
            row.data.putAll(results)
        } finally {
            exchange.in.body = row
        }
    }
}

