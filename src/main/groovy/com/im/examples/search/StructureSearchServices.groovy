package com.im.examples.search

import com.im.chemaxon.camel.db.DefaultJChemSearcher
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.model.rest.RestBindingMode
import com.im.loaders.Utils

import javax.sql.DataSource

/**
 * Created by timbo on 26/04/2014.
 */

String PORT = '43256'
String BASE_URL = 'localhost:' + PORT + '/search'

ConfigObject database = new ConfigSlurper().parse(new File('loaders/database.properties').toURL())
ConfigObject drugbank = new ConfigSlurper().parse(new File('loaders/drugbank.properties').toURL())

DataSource dataSource = Utils.createDataSource(database, drugbank.user, drugbank.password) 


def drugbankSearcher = new DefaultJChemSearcher(
    searchOptions: "t:d", 
    structureTable: drugbank.schema + '.' + drugbank.table,
    outputMode: DefaultJChemSearcher.OutputMode.TEXT,
    outputFormat: "cxsmiles",
    dataSource: dataSource
)


CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            
            restConfiguration().component("restlet").host("localhost").port(PORT).bindingMode(RestBindingMode.json)
            
            
            rest("/search")
            .post("/drugbank").type(StructureQuery.class).to("direct:drugbanksearch")
            .post("/chembl").to("direct:nyi")
            .post("/emoleculesbb").to("direct:nyi")
            
            from('direct:nyi')
            .log('Not yet supported')
 
            from('direct:drugbanksearch')
            .log('Query is : ${body}')
            .setHeader(DefaultJChemSearcher.HEADER_SEARCH_OPTIONS, simple('${body.searchOptions}'))
            .transform(simple('${body.query}'))
            .process(drugbankSearcher)
            
        }
    })
camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
//String result = t.requestBodyAndHeader('direct:drugbanksearch', 'Cc1ccncc1C', 
//    DefaultJChemSearcher.HEADER_SEARCH_OPTIONS, 't:s', String.class)
//println "Results:\n$result"


//def resp1 = t.requestBody("direct:drugbanksearch", new StructureQuery('Cc1ccncc1C', 't:s'))
//println "resp1: $resp1"

def resp2 = t.requestBody("http4:$BASE_URL/drugbank", new StructureQuery('Cc1ccncc1C', 't:s'))
println "resp2: $resp2"

sleep(1000)
camelContext.stop()
