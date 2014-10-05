package com.im.chemaxon.examples.jchemsearch

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import org.apache.camel.CamelContext
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

def searcher = new DefaultJChemSearcher()
searcher.searchOptions = 't:s'
searcher.structureTable = 'MOLPORT_SEP_2014'
searcher.connection = ds.getConnection()
searcher.outputMode = DefaultJChemSearcher.OutputMode.TEXT
searcher.outputFormat = 'smiles'

CamelContext camelContext = new DefaultCamelContext(registry)
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            from("direct:querygohere")
            .to('direct:molportsearch')
            .log('Results: ${body}')

            from('direct:molportsearch')
            .log('Processing ${body}')
            .process(searcher)

        }
    })

camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
//t.sendBody('direct:querygohere', '[#8]-[#6](=O)-[#6]-1=[#6]-[#6]=[#7]-[#6]=[#6]-1')
t.sendBody('direct:querygohere', '[H][#6]-1-[#6]([H])-[#7](-[#6]([H])-[#6]([H])-[#7]-1[H])-[#6]-1=[#6]([H])-[#6]=[#6]-[#6]=[#6]-1[H]')
sleep(100)
camelContext.stop()