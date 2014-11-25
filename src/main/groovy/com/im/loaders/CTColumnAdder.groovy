package com.im.loaders

import chemaxon.util.ConnectionHandler
import chemaxon.jchem.db.DatabaseProperties
import chemaxon.jchem.db.UpdateHandler
import java.sql.Connection
import groovy.transform.Canonical
import groovy.sql.Sql


class CTColumnAdder {
    
    ConfigObject database, chemcentral
    String chemcentralStructureTable, chemcentralPropertyTable
    
    List ctCols = [
        new CTDefinition('atom_count', 'integer', 'atomCount()'),
        new CTDefinition('psa', 'FLOAT', 'psa()'),
        new CTDefinition('logp', 'FLOAT', 'logp()'),
        new CTDefinition('hba', 'SMALLINT', 'acceptorCount()'),
        new CTDefinition('hbd', 'SMALLINT', 'donorCount()'),
        new CTDefinition('rot_bond_count', 'SMALLINT', 'rotatableBondCount()'),
        new CTDefinition('heavy_atom_count', 'SMALLINT', "atomCount() - atomCount('1')")
    ]
    
    
    CTColumnAdder() {
        database = Utils.createConfig('loaders/database.properties')
        chemcentral = Utils.createConfig('loaders/chemcentral.properties')
        this.chemcentralStructureTable = chemcentral.schema + '.structures'
        this.chemcentralPropertyTable = chemcentral.schema + '.jchemproperties'
    }
    
    static void main(String[] args) {
        def instance = new CTColumnAdder()
        instance.run()
    }
    
    void run() throws Exception {
        Connection con = Utils.createDataSource(database, chemcentral.user, chemcentral.password).connection
        Sql db = new Sql(con)
        try {
            ConnectionHandler conh = new ConnectionHandler(con, chemcentralPropertyTable) 
            DatabaseProperties dbp = new DatabaseProperties(conh, false)
        
            ctCols.each {
                it.addCTColumn(db, dbp, chemcentralStructureTable)
            }
            String[] cols = ctCols.collect { it.columnName } as String[]
            println "Calculating values"
            UpdateHandler.recalculateCTColumns(conh, chemcentralStructureTable, cols, null)
            println "Done"
        } finally {
            con?.close()
        }
    }
    
    
    @Canonical
    class CTDefinition {
        
        String columnName
        String columnDefintion
        String ctExpr
    
        void addCTColumn(Sql db, DatabaseProperties dbp, String tableName) {
            
            Utils.executeMayFail(db, "Deleting column $columnName", 'ALTER TABLE ' + tableName + ' DROP COLUMN ' + columnName)
            Utils.execute(db, "Adding column $columnName", 'ALTER TABLE ' + tableName + ' ADD COLUMN ' + columnName + ' ' + columnDefintion)
            println "Adding CT expression $ctExpr"
            dbp.setChemTermForColumn(tableName, columnName, ctExpr)
            println "Added CT expression"
            
        }
	
    }

}