package com.im.examples

import groovy.sql.*
import java.sql.*
import javax.sql.*
import org.postgresql.ds.PGSimpleDataSource
import org.apache.camel.*
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry

PGSimpleDataSource ds = new PGSimpleDataSource()
ds.serverName = 'localhost'
ds.portNumber = 49153
ds.databaseName = 'chemcentral'
ds.user = 'chembl'
ds.password = 'chembl'




SimpleRegistry registry = new SimpleRegistry()
registry.put('chemcentral', ds)
        
CamelContext camelContext = new DefaultCamelContext(registry)

camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
                          
            from('direct:chemblmolquery')
            .to('jdbc:chemcentral?outputType=StreamList&statement.fetchSize=100&resetAutoCommit=false')  //&statement.fetchSize=100
            .split(body()).streaming()
            .log('.')
            
        }
    })

camelContext.start()


String s = "SELECT * FROM chembl_19.compound_structures limit 10000"
        
println "SQL: $s"

ProducerTemplate t = camelContext.createProducerTemplate()
t.sendBody('direct:chemblmolquery', s)

sleep(4000)

