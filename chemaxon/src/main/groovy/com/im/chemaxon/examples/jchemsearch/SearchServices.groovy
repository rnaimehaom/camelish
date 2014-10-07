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
dbSearcher.outputMode = DefaultJChemSearcher.OutputMode.TEXT
dbSearcher.outputFormat = 'cxsmiles'

CamelContext camelContext = new DefaultCamelContext(registry)
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {

            from('jetty:http://0.0.0.0:8080/chemsearch/drugbank')
            .log('Processing ${body}')
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain")) 
            .process(dbSearcher)

        }
    })

camelContext.start()

// run search to load the structrue cache
ProducerTemplate t = camelContext.createProducerTemplate()
Object out = t.sendBody('http://localhost:8080/chemsearch/drugbank', 'Cc1ccncc1C')
println "Results:\n$out"
println "Services started"
synchronized (this) { this.wait() }