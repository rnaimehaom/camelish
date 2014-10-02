package com.im.chemaxon.examples.jchemsearch

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import com.im.chemaxon.camel.db.DefaultJChemSearcher
import chemaxon.struc.Molecule

MysqlDataSource ds = new MysqlDataSource()
ds.setServerName("localhost")
ds.setPortNumber(3306)
ds.setDatabaseName("vendordbs");
ds.setUser("vendordbs");
ds.setPassword("vendordbs");

SimpleRegistry registry = new SimpleRegistry()
registry.put('mydb', ds)

def searcher = new DefaultJChemSearcher()
searcher.searchOptions = 't:s'
searcher.structureTable = 'MOLPORT_JUN_2014'
searcher.connection = ds.getConnection()
searcher.outputMode = DefaultJChemSearcher.OutputMode.TEXT
searcher.outputFormat = 'smiles'

CamelContext camelContext = new DefaultCamelContext(registry)
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            from("direct:querygohere")
            .log('Processing ${body}')
            .process(searcher)
            .log('Results: ${body}')
            .to('mock:result')

        }
    })

camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
//t.sendBody('direct:querygohere', '[#8]-[#6](=O)-[#6]-1=[#6]-[#6]=[#7]-[#6]=[#6]-1')
t.sendBody('direct:querygohere', '[H][#6]-1-[#6]([H])-[#7](-[#6]([H])-[#6]([H])-[#7]-1[H])-[#6]-1=[#6]([H])-[#6]=[#6]-[#6]=[#6]-1[H]')
sleep(2000)
camelContext.stop()