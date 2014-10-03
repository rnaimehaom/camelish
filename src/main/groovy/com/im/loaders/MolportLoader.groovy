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
class MolportLoader extends AbstractLoader {
   
    static void main(String[] args) {
        println "Running with $args"
        def instance = new MolportLoader('loaders/molport.properties')
        instance.run(args)
    }
    
    MolportLoader(String config) {
        super(config)
    }
    
    void executeRoutes(CamelContext camelContext, Connection con) {
        synchronized (this) { this.wait() }
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
            
                    from('file:' + props.data.path + '?antInclude=*.gz')
                    .log('Processing file ${header.CamelFileName}')
                    .unmarshal().gzip()
                    .split().method(MoleculeIOUtils.class, 'mrecordIterator').streaming()
                    //.log('Processing line ${body}')
                    .process(updateHandlerProcessor)
                    .process(new ChunkBasedReporter(10000))
                    //.to("mock:theend")
            
                    from('direct:errors')
                    .log('Error: ${exception.message}')
                    .transform(body().append('\n'))
                    .to('file:' + props.data.path + '?fileExist=Append&fileName=' + 'molport_errrors')
                }
            })
    }
	
}

