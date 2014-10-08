package com.im.chemaxon.examples.jchemsearch

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
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
dbSearcher.searchOptions = 't:s'
dbSearcher.structureTable = 'DRUGBANK_FEB_2014'
dbSearcher.connection = ds.getConnection()
dbSearcher.outputMode = DefaultJChemSearcher.OutputMode.STREAM
dbSearcher.outputFormat = 'cxsmiles'

CamelContext camelContext = new DefaultCamelContext(registry)
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {

            from('jetty:http://0.0.0.0:8080/chemsearch/drugbank')
            .log('Processing search for ${body}')
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain")) 
            .process(dbSearcher)

        }
    })

camelContext.start()
println "Services started"

// run search to load the structure cache
ProducerTemplate t = camelContext.createProducerTemplate()
def out1 = t.requestBody('http://localhost:8080/chemsearch/drugbank', 'CCCc1ccncc1CCC')
println "Results 1:\n$out1"
def out2 = t.requestBodyAndHeader('http://localhost:8080/chemsearch/drugbank', 'OC(=O)C1=CCCNC1', 
    DefaultJChemSearcher.HEADER_SEARCH_OPTIONS, 'sep=, t:d,exactStereoSearch:n')
println "Results 2:\n$out2"
println "finished"

synchronized (this) { this.wait() }