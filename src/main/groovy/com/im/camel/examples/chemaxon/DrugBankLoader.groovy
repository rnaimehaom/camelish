package com.im.camel.examples.chemaxon

import com.im.chemaxon.io.MoleculeIOUtils
import com.im.camel.processor.ChunkBasedReporter
import chemaxon.jchem.db.*
import chemaxon.marvin.io.*
import chemaxon.util.ConnectionHandler
import com.im.chemaxon.camel.db.JCBTableInserterUpdater
import groovy.sql.*
import java.sql.*
import org.apache.camel.*
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext

final String PATH = '/Users/timbo/data/structures/drugbank/'
final String STRUCTURE_TABLE_NAME = 'DRUGBANK_FEB_2014'
final String STRUCTURE_FILE_NAME = 'all.sdf'
final int REPORTING_CHUNK_SIZE = 1000
final String SZR_LOC = 'src/misc/standardizer.xml'

File errF = new File(PATH + STRUCTURE_FILE_NAME + '_errrors')
errF.delete()

Connection con = DriverManager.getConnection('jdbc:mysql://localhost/vendordbs', 'vendordbs', 'vendordbs')

String szr = new File(SZR_LOC).text

ConnectionHandler conh = new ConnectionHandler(con, 'JCHEMPROPERTIES')
if (!DatabaseProperties.propertyTableExists(conh)) {
    DatabaseProperties.createPropertyTable(conh)    
}
if (UpdateHandler.isStructureTable(conh, STRUCTURE_TABLE_NAME)) {
    UpdateHandler.dropStructureTable(conh, STRUCTURE_TABLE_NAME)
}

StructureTableOptions opts = new StructureTableOptions(STRUCTURE_TABLE_NAME, TableTypeConstants.TABLE_TYPE_MOLECULES)
opts.extraColumnDefinitions = ',DRUGBANK_ID CHAR(7), DRUG_GROUPS VARCHAR(100)'
opts.standardizerConfig = szr
UpdateHandler.createStructureTable(conh, opts)

JCBTableInserterUpdater updateHandlerProcessor = new JCBTableInserterUpdater(UpdateHandler.INSERT, 
    STRUCTURE_TABLE_NAME, 'DRUGBANK_ID, DRUG_GROUPS') {

    @Override
    protected void setValues(Exchange exchange, UpdateHandler updateHandler) {
        MRecord record = exchange.in.getBody(MRecord.class)
        def props = record.propertyContainer
        updateHandler.structure = record.string
        updateHandler.setValueForAdditionalColumn(1, MPropHandler.convertToString(props, 'DRUGBANK_ID'))       
        updateHandler.setValueForAdditionalColumn(2, MPropHandler.convertToString(props, 'DRUG_GROUPS'))       
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
            .split().method(MoleculeIOUtils.class, 'mrecordIterator').streaming()
            //.log('Processing line ${body}')
            .process(updateHandlerProcessor)
            .process(new ChunkBasedReporter())
            //.to("mock:theend")
            
            from('direct:errors')
            .log('Error: ${exception.message}')
            .transform(body().append('\n'))
            .to('file:' + PATH + '?fileExist=Append&fileName=' + STRUCTURE_FILE_NAME + '_errrors')
        }
    })
camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
t.sendBody('direct:start', new File(PATH + STRUCTURE_FILE_NAME))
                                           
sleep(1000)

Sql db = new Sql(con)
int rows = db.firstRow('select count(*) from ' + STRUCTURE_TABLE_NAME)[0]
println "Found $rows rows"

camelContext.stop()
