package com.im.camel.examples.chemaxon

import com.im.chemaxon.io.MoleculeIOUtils
import chemaxon.jchem.db.*
import chemaxon.util.ConnectionHandler
import com.im.chemaxon.camel.db.JCBTableInserterUpdater
import groovy.sql.*
import java.sql.*
import org.apache.camel.*
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext

final String PATH = '/Users/timbo/data/structures/emolecules/'
final String EMOLS_TABLE_NAME = 'EMOLECULES_2014_MAY'
final String EMOLS_FILE_NAME = 'emolecules-May2014.smi'

File errF = new File(PATH + EMOLS_FILE_NAME + '_errrors')
errF.delete()

//Connection con = DriverManager.getConnection('jdbc:derby:memory:emols;create=true')
Connection con = DriverManager.getConnection('jdbc:mysql://localhost/vendordbs', 'vendordbs', 'vendordbs')


ConnectionHandler conh = new ConnectionHandler(con, 'JCHEMPROPERTIES')
if (!DatabaseProperties.propertyTableExists(conh)) {
    DatabaseProperties.createPropertyTable(conh)    
}
if (UpdateHandler.isStructureTable(conh, EMOLS_TABLE_NAME)) {
    UpdateHandler.dropStructureTable(conh, EMOLS_TABLE_NAME)
}
StructureTableOptions opts = new StructureTableOptions(EMOLS_TABLE_NAME, TableTypeConstants.TABLE_TYPE_MOLECULES)
opts.extraColumnDefinitions = ', version_id integer, parent_id integer'
UpdateHandler.createStructureTable(conh, opts)

JCBTableInserterUpdater updateHandlerProcessor = new JCBTableInserterUpdater(UpdateHandler.INSERT, EMOLS_TABLE_NAME, 'version_id, parent_id') {

    @Override
    protected void setValues(Exchange exchange, UpdateHandler updateHandler) {
        String data = exchange.in.getBody(String.class)
        String[] vals = data.split(' ')
        updateHandler.structure = vals[0]
        updateHandler.setValueForAdditionalColumn(1, vals[1])
        updateHandler.setValueForAdditionalColumn(2, vals[2])        
    }
}
updateHandlerProcessor.connectionHandler = conh

CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            onException()
            .handled(true)
            .to('direct:errors')
            
            from('direct:start')
            .split(body().tokenize("\n")).streaming()
            //.log('Processing line ${body}')
            .process(updateHandlerProcessor)
            //.to("mock:theend")
            
            from('direct:errors')
            .log('Error: ${body}')
            .transform(body().append('\n'))
            .to('file:' + PATH + '?fileExist=Append&fileName=' + EMOLS_FILE_NAME + '_errrors')
        }
    })
camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
t.sendBody('direct:start', new File(PATH + EMOLS_FILE_NAME))
                                           
sleep(1000)

Sql db = new Sql(con)
int rows = db.firstRow('select count(*) from ' + EMOLS_TABLE_NAME)[0]
println "Found $rows rows"

camelContext.stop()

sleep(1000)

//try {
//    DriverManager.getConnection('jdbc:derby:mem:emols;shutdown=true')
//} catch (Exception e) {
//    println "DB shutdown"
//}