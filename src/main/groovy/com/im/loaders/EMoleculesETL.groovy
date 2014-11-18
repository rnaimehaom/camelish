package com.im.loaders

import groovy.sql.Sql
import java.sql.Connection
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
class EMoleculesETL extends AbstractETL {
    
    ConfigObject emolecules
    String emoleculesTable, concordanceTable
    
    int limit, offset
    int fetchSize = 500
    
    protected String createConcordanceTableSql, readStructuresSql, insertConcordanceSql, 
    createConcordanceStructureIdIndexSql, createConcordanceCdidIndexSql,
    insertPropertyDefinitionsSql, insertStructurePropsSql, deleteSourceSql
    
    static void main(String[] args) {
        println "Running with $args"
        def instance = new EMoleculesETL()
        instance.run()
    }
    
    EMoleculesETL() {
        emolecules = Utils.createConfig('loaders/emolecules.properties')
        
        this.concordanceTable = emolecules.schema + '.emolecules_concordance'
        this.emoleculesTable = emolecules.schema + '.' + emolecules.table
                
        this.offset = emolecules.offset
        this.limit = emolecules.limit
        
        generateSqls()
    }
    
    void generateSqls() {
        createConcordanceTableSql = """\
            |CREATE TABLE $concordanceTable (
            |  structure_id INTEGER NOT NULL,
            |  cd_id INTEGER NOT NULL,
            |  CONSTRAINT fk_con2stuctures FOREIGN KEY (structure_id) references $chemcentralStructureTable (cd_id) ON DELETE CASCADE,
            |  CONSTRAINT fk_con2cdid FOREIGN KEY (cd_id) references $emoleculesTable (cd_id) ON DELETE CASCADE
            |)""".stripMargin()
        
        readStructuresSql = "SELECT cd_id, cd_structure FROM $emoleculesTable".toString()
        limit && (readStructuresSql += " LIMIT $limit")
        offset && (readStructuresSql += " OFFSET $offset")
        
        insertConcordanceSql = "INSERT INTO $concordanceTable (structure_id, cd_id) VALUES (?, ?)".toString()
        
        createConcordanceStructureIdIndexSql = "CREATE INDEX idx_con_emolbb_structure_id on $concordanceTable (structure_id)"
        createConcordanceCdidIndexSql = "CREATE INDEX idx_con_emolbb_cd_id on $concordanceTable (cd_id)"
        
        deleteAliasesSql = "DELETE FROM $chemcentralStructureAliasesTable WHERE alias_type = ?"
        
        deleteSourceSql = "DELETE FROM $chemcentralSourcesTable WHERE source_name = ?"
        
        insertAliasesSql = """\
            |INSERT INTO $chemcentralStructureAliasesTable (structure_id, alias_type, alias_value)
            |  SELECT con.structure_id, '$emolecules.name', e.version_id
            |    FROM $concordanceTable con
            |    JOIN $emoleculesTable e ON e.cd_id = con.cd_id""".stripMargin()
        
        insertPropertyDefinitionsSql = """\
                |INSERT INTO $chemcentralPropertyDefintionsTable (source_id, property_description, original_id, est_size)
                |  VALUES (?, 'eMolecules building blocks record', null, $emolecules.estSize)""".stripMargin()
        
        insertStructurePropsSql = """\
                |INSERT INTO $chemcentralStructurePropertiesTable
                |  (structure_id, source_id, batch_id, property_id, property_data)
                |  SELECT con.structure_id, ?, e.version_id, ?, row_to_json(e)::jsonb
                |    FROM (SELECT cd_id, version_id, parent_id FROM $emoleculesTable) e
                |    JOIN $concordanceTable con ON con.cd_id = e.cd_id""".stripMargin()

    }
    
    void run() {

        DataSource dataSource = Utils.createDataSource(database, chemcentral.user, chemcentral.password)
        Sql db1, db2, db3
        db1 = new Sql(dataSource.connection)
        StructureLoader loader
        
        try {
            createConcordanceTable(db1)
                  
            db2 = new Sql(dataSource.connection)
            db3 = new Sql(dataSource.connection)
            loader = new StructureLoader(db3, chemcentralStructureTable, chemcentralPropertyTable)
            db2.connection.autoCommit = false
            db2.withStatement {
                it.fetchSize = fetchSize
            }
            
            loadData(db2, db1, loader)
            
            createConcordanceIndexes(db1)
            
            deleteSource(db1, emolecules.name)
            int sourceId = Utils.createSourceDefinition(dataSource, chemcentral.schema, 1, emolecules.name, emolecules.description, 'P', emolecules.owner, emolecules.maintainer, true)
            insertAliases(db1, emolecules.name)
            int propertyId = generatePropertyDefinition(db1, sourceId)
            generatePropertyValues(db1, [sourceId, propertyId])
            
        } finally {
            loader?.close()
            db3?.close()
            db2?.close()
            db1?.close()
        }
    }
    
    int generatePropertyDefinition(Sql db, int sourceId) {
        println "Inserting property defintion for $sourceId"
        def keys = db.executeInsert(insertPropertyDefinitionsSql, [sourceId])
        int id = keys[0][0]
        println "Property definition generated. ID = $id"
        return id
    }
    
    void createConcordanceTable(Sql db) {
        Utils.executeMayFail(db, 'drop concordance table', 'DROP TABLE ' + concordanceTable)
        Utils.execute(db, 'create concordance table', createConcordanceTableSql)
    }
    
    void loadData(Sql reader, Sql writer, StructureLoader loader) {
        println "Loading data from $emoleculesTable"
        
        int count = 0
        reader.eachRow(readStructuresSql) { row ->
            int cdid = loader.execute(new String(row['cd_structure']))
            //println cdid
            writer.executeInsert(insertConcordanceSql, [Math.abs(cdid), row['cd_id']])
            count++
            if (count % 1000 == 0) {
                println "Handled $count rows"
            }
        }
        println "Handled $count structures"
        
    }
    
    void createConcordanceIndexes(Sql db) {
        println "Creating concordance indexes"
        db.execute(createConcordanceStructureIdIndexSql)
        db.execute(createConcordanceCdidIndexSql)
        println "Indexes created"
    } 
    
}

