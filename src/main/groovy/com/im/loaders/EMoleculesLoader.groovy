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
class EMoleculesLoader extends AbstractLoader {
   
    static void main(String[] args) {
        println "Running with $args"
        def instance = new EMoleculesLoader('loaders/emolecules.properties')
        instance.run(args)
    }
    
    EMoleculesLoader(String config) {
        super(config)
    }
    
    void executeRoutes(CamelContext camelContext, Connection con) {
        println "Loading file ${props.data.path}/${props.data.file}"
        ProducerTemplate t = camelContext.createProducerTemplate()
        t.sendBody('direct:start', new File(props.data.path + '/' + props.data.file))
                                           
        sleep(100)

        Sql db = new Sql(con)
        int rows = db.firstRow('select count(*) from ' + props.db.table)[0]
        println "Found $rows rows"
    }
    
    void createRoutes(CamelContext camelContext, Connection con) {
        
        String cols = getColumnNamesFromColumnDefs(props.db.extraColumnDefs).join(',')

        JCBTableInserterUpdater updateHandlerProcessor = new JCBTableInserterUpdater(
            UpdateHandler.INSERT, props.db.table, cols) {

            @Override
            protected void setValues(Exchange exchange, UpdateHandler updateHandler) {
                String data = exchange.in.getBody(String.class)
                String[] vals = data.split(' ')
                updateHandler.structure = vals[0]
                updateHandler.setValueForAdditionalColumn(1, exchange.getContext().getTypeConverter().convertTo(Integer.class, vals[1]))
                updateHandler.setValueForAdditionalColumn(2, exchange.getContext().getTypeConverter().convertTo(Integer.class, vals[2]))  
            }
        }

        ConnectionHandler conh = new ConnectionHandler(con, 'JCHEMPROPERTIES')
        updateHandlerProcessor.connectionHandler = conh
        
        camelContext.addRoutes(new RouteBuilder() {
                def void configure() {
                    onException()
                    .handled(true)
                    .to('direct:errors')
            
                    from('direct:start')
                    .unmarshal().gzip()
                    .split(body().tokenize("\n")).streaming()
                    //.log('Line: ${body}')
                    .process(updateHandlerProcessor)
                    .process(new ChunkBasedReporter(10000))
            
                    from('direct:errors')
                    .log('Error: ${body}')
                    .transform(body().append('\n'))
                    .to('file:' + props.data.path + '?fileExist=Append&fileName=' + props.data.file + '_errrors')
                }
            })
    }
	
}

