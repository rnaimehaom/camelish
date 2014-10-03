package com.im.loaders

import chemaxon.jchem.db.*
import chemaxon.marvin.io.*
import chemaxon.util.ConnectionHandler
import com.im.chemaxon.io.MoleculeIOUtils
import com.im.camel.processor.ChunkBasedReporter
import com.im.chemaxon.camel.db.DefaultJCBInserter
import com.im.chemaxon.camel.db.JCBTableInserterUpdater
import groovy.sql.Sql
import java.sql.*
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext

/**
 *
 * @author timbo
 */
class DrugbankLoader extends AbstractLoader {
   
    static void main(String[] args) {
        println "Running with $args"
        def instance = new DrugbankLoader('loaders/drugbank.properties')
        instance.run(args)
    }
    
    DrugbankLoader(String config) {
        super(config)
    }
    
    void executeRoutes(CamelContext camelContext, Connection con) {
        ProducerTemplate t = camelContext.createProducerTemplate()
        t.sendBody('direct:start', new File(props.data.path + '/' + props.data.file))
                              
        Sql db = new Sql(con)
        int rows = db.firstRow('select count(*) from ' + props.db.table)[0]
        println "Table now has $rows rows"
    }
    
    void createRoutes(CamelContext camelContext, Connection con) {
        final String cols = getColumnNamesFromColumnDefs(props.db.extraColumnDefs).join(',')
        //println "extracols = $cols"

        JCBTableInserterUpdater updateHandlerProcessor = new DefaultJCBInserter(
            props.db.table, cols, props.data.fields)

        ConnectionHandler conh = new ConnectionHandler(con, 'JCHEMPROPERTIES')
        updateHandlerProcessor.connectionHandler = conh
        
        camelContext.addRoutes(new RouteBuilder() {
                def void configure() {
                    onException()
                    .handled(true)
                    .to('direct:errors')
            
                    from('direct:start')
                    .split().method(MoleculeIOUtils.class, 'mrecordIterator').streaming()
                    //.log('Processing line ${body}')
                    .process(updateHandlerProcessor)
                    .process(new ChunkBasedReporter())
                    //.to("mock:theend")
            
                    from('direct:errors')
                    .log('Error: ${exception.message}')
                    .transform(body().append('\n'))
                    .to('file:' + props.data.path + '?fileExist=Append&fileName=' + props.data.file + '_errrors')
                }
            })
    }
	
}

