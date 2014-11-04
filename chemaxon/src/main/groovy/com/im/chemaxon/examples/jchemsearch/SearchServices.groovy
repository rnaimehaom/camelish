package com.im.chemaxon.examples.jchemsearch

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import com.im.chemaxon.camel.db.DefaultJChemSearcher
import chemaxon.struc.Molecule
import org.postgresql.ds.PGSimpleDataSource

PGSimpleDataSource ds = new PGSimpleDataSource()
ds.setServerName("localhost")
ds.setDatabaseName("chemdbs");
ds.setUser("chemdbs");
ds.setPassword("chemdbs");

SimpleRegistry registry = new SimpleRegistry()
registry.put('mydb', ds)

def dbSearcher = new DefaultJChemSearcher()
dbSearcher.searchOptions = 't:d'
dbSearcher.structureTable = 'DRUGBANK_FEB_2014'
dbSearcher.connection = ds.getConnection()
dbSearcher.outputMode = DefaultJChemSearcher.OutputMode.STREAM
dbSearcher.outputFormat = 'cxsmiles'

CamelContext camelContext = new DefaultCamelContext(registry)
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {

            from('jetty:http://0.0.0.0:8080/chemsearch/drugbank')
            // Jetty uses stream so body can only be read once.
            // So to avoid problems grab it as a String immediately
            .convertBodyTo(String.class) 
            .log('Processing search for ${body}')
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain")) 
            .process(dbSearcher)

        }
    })

camelContext.start()
println "Services started"

// run search to load the structure cache
ProducerTemplate t = camelContext.createProducerTemplate()
def out1 = t.requestBodyAndHeader('http://localhost:8080/chemsearch/drugbank', 'C1=CCCNC1', 
    DefaultJChemSearcher.HEADER_SEARCH_OPTIONS, 't:s')


println "Finished"

synchronized (this) { this.wait() }