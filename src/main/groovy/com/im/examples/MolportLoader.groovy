package com.im.examples

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

final String PATH = '/Users/timbo/data/structures/molport/All Available BB/load'
final String STRUCTURE_FILE_NAME = 'hsbb-024-000-000--024-999-999.sdf.gz'
final String STRUCTURE_TABLE_NAME = 'MOLPORT_JUN_2014'
final int REPORTING_CHUNK_SIZE = 1000
final String SZR_LOC = 'src/misc/standardizer.xml'

File errF = new File('/Users/timbo/data/structures/molport/MOLPORT_JUN_2014_errrors')
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
opts.extraColumnDefinitions = ',REGID VARCHAR(32)'
opts.standardizerConfig = szr
UpdateHandler.createStructureTable(conh, opts)

JCBTableInserterUpdater updateHandlerProcessor = new JCBTableInserterUpdater(UpdateHandler.INSERT, 
    STRUCTURE_TABLE_NAME, 'REGID') {

    @Override
    protected void setValues(Exchange exchange, UpdateHandler updateHandler) {
        MRecord record = exchange.in.getBody(MRecord.class)
        def props = record.propertyContainer
        updateHandler.structure = record.string
        updateHandler.setValueForAdditionalColumn(1, MPropHandler.convertToString(props, 'PUBCHEM_EXT_DATASOURCE_REGID'))       
     }
}
updateHandlerProcessor.connectionHandler = conh

CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            onException()
            .handled(true)
            .to('direct:errors')
            
            from('file:/Users/timbo/data/structures/molport/All Available BB?antInclude=*.gz')
            .log('Processing file ${header.CamelFileName}')
            .unmarshal().gzip()
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
                                           
synchronized (this) { this.wait() }
