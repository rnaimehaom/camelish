package com.im.examples

import chemaxon.jchem.db.JChemSearch
import com.im.chemaxon.camel.db.AbstractJChemSearcher
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext

import groovy.sql.Sql

/**
 * Created by timbo on 26/04/2014.
 */

ConfigObject database = new ConfigSlurper().parse(new File('loaders/database.properties').toURL())
ConfigObject drugbank = new ConfigSlurper().parse(new File('loaders/drugbank.properties').toURL())
Sql db = Sql.newInstance(database.url, 'vendordbs', 'vendordbs')


AbstractJChemSearcher searcher = new AbstractJChemSearcher(drugbank.table, 't:s') {
    @Override
    protected void handleSearchParams(Exchange exchange, JChemSearch jcs) {
        jcs.setQueryStructure(exchange.in.body)
    }

    @Override
    protected void handleSearchResults(Exchange exchange, JChemSearch jcs) {
        int[] cdids = jcs.getResults()
        exchange.out.setBody(cdids)
    }

}
searcher.connection = db.connection

CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
    def void configure() {
        from('direct:jchemsearchexample')
                .log('Processing ${body}')
                .process(searcher)
                .log('Hits: ${body.length}')
                .to("mock:theend")
    }
})
camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
t.sendBody('direct:jchemsearchexample', 'Cc1ccncc1C')
t.sendBody('direct:jchemsearchexample', 'Cc1ccccc1C')

sleep(2000)
camelContext.stop()

try {
    DriverManager.getConnection('jdbc:derby:/Users/timbo/IJCProjects/SearchableDBs/.config/localdb/db;shutdown=true')
} catch (Exception e) {
    println "DB shutdown"
}