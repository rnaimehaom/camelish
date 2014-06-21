package com.im.examples

import chemaxon.jchem.db.*
import chemaxon.util.ConnectionHandler
import groovy.sql.*
import java.sql.*

final String STRUCTURE_TABLE_NAME = 'jchem_structures'
final int REPORTING_CHUNK_SIZE = 10000
String szr = new File('src/misc/standardizer.xml').text

Sql db = Sql.newInstance('jdbc:mysql://localhost/chembl_18?useCursorFetch=true&defaultFetchSize=1000', 'chembl', 'chembl')
UpdateHandler uh
try {
    ConnectionHandler conh = new ConnectionHandler(db.connection, 'jchemproperties')
    if (!DatabaseProperties.propertyTableExists(conh)) {
        DatabaseProperties.createPropertyTable(conh)    
    }
    if (UpdateHandler.isStructureTable(conh, STRUCTURE_TABLE_NAME)) {
        UpdateHandler.dropStructureTable(conh, STRUCTURE_TABLE_NAME)
    }

    StructureTableOptions opts = new StructureTableOptions(STRUCTURE_TABLE_NAME, TableTypeConstants.TABLE_TYPE_MOLECULES)
    opts.extraColumnDefinitions = ',MOLREGNO INT(9)'
    opts.standardizerConfig = szr
    UpdateHandler.createStructureTable(conh, opts)

    uh = new UpdateHandler(conh, UpdateHandler.INSERT, STRUCTURE_TABLE_NAME, 'MOLREGNO');
    int count = 0
    db.eachRow("select molregno, molfile from compound_structures") {
        count++
        if (count % REPORTING_CHUNK_SIZE == 0) {
            println "processing row $count"
        }
        try {
            uh.structure = it[1]
            uh.setValueForAdditionalColumn(1, it[0])
            uh.execute()  
        } catch (SQLException e) {
            println "Failed to insert structure ${it[0]}: $e.message"
        }
    }
} finally {
    uh?.close()
    db?.close()
}


