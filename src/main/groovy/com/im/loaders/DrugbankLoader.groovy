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
import javax.sql.DataSource
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
        println "Running with args: $args"
        def instance = new DrugbankLoader('loaders/drugbank.properties')
        instance.run(args)
    }
    
    DrugbankLoader(String config) {
        super(new File(config).toURL())
    }
    
    void executeRoutes(CamelContext camelContext) {
        ProducerTemplate t = camelContext.createProducerTemplate()
        String file = props.path + '/' + props.file
        println "Loading file $file"
        t.sendBody('direct:start', new File(file))
        println "Finished loading"
                              
        Sql db = new Sql(dataSource.getConnection())
        int rows = db.firstRow('select count(*) from ' + props.schema + '.' + props.table)[0]
        println "Table now has $rows rows"
    }
    
    void createRoutes(CamelContext camelContext) {
        final String cols = getColumnNamesFromColumnDefs(props.extraColumnDefs).join(',')
        //println "extracols = $cols"

        JCBTableInserterUpdater updateHandlerProcessor = new DefaultJCBInserter(
            props.schema + '.' + props.table, cols, props.fields)

        ConnectionHandler conh = new ConnectionHandler(dataSource.getConnection(), props.schema + '.jchemproperties')
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
                    .process(new ChunkBasedReporter(props.reportingChunk))
            
                    from('direct:errors')
                    .log('Error: ${exception.message}')
                    .log('Error: ${exception.stacktrace}')
                    .transform(body().append('\n'))
                    .to('file:' + props.path + '?fileExist=Append&fileName=' + props.file + '_errrors')
                }
            })
    }
	
}

