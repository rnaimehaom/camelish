package com.im.loaders

import groovy.sql.Sql
import java.sql.Connection
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
class ChemblETL extends AbstractETL {
    
    ConfigObject chembl
    String chemblIdLookupTable, chemblAssaysTable, chemblActivitiesTable, chemblCompoundStructuresTable,
    concordanceTable
    
    int limit, offset
    int fetchSize = 500
    
    static final String DATASET_NAME = 'ChEMBL'
    
    private String createConcordanceTableSql, readChemblStructuresSql, insertConcordanceSql, 
    createStructureIdIndexSql, createMolregnoIndexSql,
    insertPropertyDefinitionsSql, insertStructurePropsSql, deleteSourceSql
    
    static void main(String[] args) {
        println "Running with $args"
        def instance = new ChemblETL()
        instance.run()
    }
    
    ChemblETL() {
        chembl = Utils.createConfig('loaders/chembl.properties')
        
        this.concordanceTable = chembl.schema + '.concordance'
        this.chemblIdLookupTable = chembl.schema + '.chembl_id_lookup'
        this.chemblActivitiesTable = chembl.schema + '.activities'
        this.chemblAssaysTable = chembl.schema + '.assays'
        this.chemblCompoundStructuresTable = chembl.schema + '.compound_structures'
                
        this.offset = chembl.offset
        this.limit = chembl.limit
        
        generateSqls()
    }
    
    void generateSqls() {
        createConcordanceTableSql = """\
            |CREATE TABLE $concordanceTable (
            |  structure_id INTEGER NOT NULL,
            |  molregno INTEGER NOT NULL,
            |  CONSTRAINT fk_con2stuctures FOREIGN KEY (structure_id) references $chemCentralStructureTable (cd_id) ON DELETE CASCADE,
            |  CONSTRAINT fk_con2molregno FOREIGN KEY (molregno) references $chemblCompoundStructuresTable (molregno) ON DELETE CASCADE
            |)""".stripMargin()
        
        readChemblStructuresSql = """\
            |SELECT molregno, molfile
            |  FROM ${chembl.schema}.compound_structures""".stripMargin()
        limit && (readChemblStructuresSql += " LIMIT $limit")
        offset && (readChemblStructuresSql += " OFFSET $offset")
        
        insertConcordanceSql = "INSERT INTO $concordanceTable (structure_id, molregno) VALUES (?, ?)".toString()
        
        createStructureIdIndexSql = "CREATE INDEX idx_con_structure_id on $concordanceTable (structure_id)"
        createMolregnoIndexSql = "CREATE INDEX idx_con_molregno on $concordanceTable (molregno)"
        
        deleteAliasesSql = "DELETE FROM $chemcentralStructureAliasesTable WHERE alias_type = ?"
        
        deleteSourceSql = "DELETE FROM $chemCentralSourcesTable WHERE source_name = ?"
        
        insertAliasesSql = """\
            |INSERT INTO $chemcentralStructureAliasesTable (structure_id, alias_type, alias_value)
            |  SELECT con.structure_id, '$DATASET_NAME', lc.chembl_id
            |    FROM $concordanceTable con
            |    JOIN $chemblIdLookupTable lc ON lc.entity_id = con.molregno
            |      AND lc.entity_type = 'COMPOUND'""".stripMargin()
        
        insertPropertyDefinitionsSql = """\
                |INSERT INTO $chemcentralPropertyDefintionsTable (source_id, property_description, original_id, est_size)
                |  SELECT ?, ass.description, l.chembl_id, sub.est_size FROM
                |    (SELECT assay_id, count(*) est_size 
                |      FROM $chemblActivitiesTable act
                |      GROUP BY assay_id
                |    ) sub
                |  JOIN $chemblAssaysTable ass ON ass.assay_id = sub.assay_id
                |  JOIN $chemblIdLookupTable l ON ass.assay_id = l.entity_id
                |    AND l.entity_type = 'ASSAY'""".stripMargin()
        
        insertStructurePropsSql = """\
                |INSERT INTO $chemcentralStructurePropertiesTable
                |  (structure_id, source_id, batch_id, property_id, property_data)
                |  SELECT con.structure_id, ?, lc.chembl_id, p.property_id, row_to_json(act)::jsonb
                |    FROM $chemblActivitiesTable act
                |    JOIN $chemblIdLookupTable la ON la.entity_id = act.assay_id AND la.entity_type = 'ASSAY'
                |    JOIN $chemblIdLookupTable lc ON lc.entity_id = act.molregno AND lc.entity_type = 'COMPOUND'
                |    JOIN $concordanceTable con ON con.molregno = act.molregno
                |    JOIN $chemcentralPropertyDefintionsTable p ON p.original_id = la.chembl_id""".stripMargin()
    }
    
    void run() {

        DataSource dataSource = Utils.createDataSource(database, chemcentral.user, chemcentral.password)
        Sql db1, db2
        db1 = new Sql(dataSource.connection)
        StructureLoader loader
        
        try {
            createConcordanceTable(db1)
             
            db2 = new Sql(dataSource.connection)
            loader = new StructureLoader(dataSource.connection, chemCentralStructureTable)
            db2.connection.autoCommit = false
            db2.withStatement {
                it.fetchSize = fetchSize
            }
            
            loadData(db2, db1, loader)
            
            createConcordanceIndexes(db1)
            
            deleteSource(db1, DATASET_NAME)
            int chemblSourceId = Utils.createSourceDefinition(dataSource, chemcentral.schema, 1, chembl.name, chembl.description, 'P', chembl.owner, chembl.maintainer, true)
            insertAliases(db1, DATASET_NAME)
            generatePropertyDefinitions(db1, chemblSourceId)
            generatePropertyValues(db1, [chemblSourceId])
            
        } finally {
            loader?.close()
            db2?.close()
            db1?.close()
        }
    }
    
    void createConcordanceTable(Sql db) {
        Utils.executeMayFail(db, 'drop concordance table', 'DROP TABLE ' + concordanceTable)
        Utils.execute(db, 'create concordance table', createConcordanceTableSql)
    }
    
    void loadData(Sql reader, Sql writer, StructureLoader loader) {
        println "Loading data from ${chembl.schema}.compound_structures"
        
        int count = 0
        reader.eachRow(readChemblStructuresSql) { row ->
            int cdid = loader.execute(row['molfile'])
            //println cdid
            writer.executeInsert(insertConcordanceSql, [Math.abs(cdid), row['molregno']])
            count++
            if (count % 10000 == 0) {
                println "Handled $count rows"
            }
        }
        println "Handled $count structures"
        
    }
    
    void createConcordanceIndexes(Sql db) {
        println "Creating concordance indexes"
        db.execute(createStructureIdIndexSql)
        db.execute(createMolregnoIndexSql)
        println "Indexes created"
    } 
    
}

