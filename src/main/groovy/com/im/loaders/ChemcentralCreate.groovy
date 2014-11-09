package com.im.loaders

import chemaxon.jchem.db.*
import chemaxon.util.ConnectionHandler
import groovy.sql.Sql
import groovy.util.logging.*
import java.util.logging.*
import java.sql.*
import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource

/**
 *
 * @author timbo
 */
@Log
class ChemcentralCreate  {
    
    ConfigObject props, database
    private String structureTable
    DataSource dataSource

   
    static void main(String[] args) {
        println "Running with $args"
        
        def instance = new ChemcentralCreate('loaders/chemcentral.properties')
        instance.run(args)
       
    }
    
    ChemcentralCreate(String config) {
        props = new ConfigSlurper().parse(new File(config).toURL()) 
        database = Utils.createConfig('loaders/database.properties')
        this.structureTable = props.schema + '.structures'
        dataSource = Utils.createDataSource(database, props.user, props.password)
    }
    
    void run(String[] args) {
        args.each { action ->
            if (action == 'createTables') {
                createTables(props)
            } else if (action == 'dropTables') {
                dropTables(props)
            } else {
                println "Unrecognised action: $action"
            }
        }
    }
    
    ConnectionHandler createConnectionHandler() {
        new ConnectionHandler(dataSource.getConnection(), props.schema + '.jchemproperties')
    }

    void dropTables(def props) {
        if (!props.allowRecreate) {
            println "Will not drop tables. Please set allowRecreate property in chemcentral.properties to true to permit this"
        } else {
            
            ConnectionHandler conh = createConnectionHandler()
            Sql db = new Sql(conh.getConnection())
            
            Utils.executeMayFail(db, 'drop table structure_props', 'DROP TABLE ' + props.schema + '.structure_props')
            Utils.executeMayFail(db, 'drop table property_definitions', 'DROP TABLE ' + props.schema + '.property_definitions')
            Utils.executeMayFail(db, 'drop table sources', 'DROP TABLE ' + props.schema + '.sources')
            Utils.executeMayFail(db, 'drop table categories', 'DROP TABLE ' + props.schema + '.categories')
            Utils.executeMayFail(db, 'drop table structure_aliases', 'DROP TABLE ' + props.schema + '.structure_aliases')
  
            if (UpdateHandler.isStructureTable(conh, structureTable)) {
                println "dropping structure table $structureTable"
                UpdateHandler.dropStructureTable(conh, structureTable)
            }
                      
        }
    }
    
    void createTables(def props) { 
        if (!props.allowRecreate) {
            println "Will not create tables. Please set allowRecreate property in chemcentral.properties to true to permit this"
        } else {
            ConnectionHandler conh = createConnectionHandler()
            Sql db = new Sql(conh.getConnection())
 
            if (!DatabaseProperties.propertyTableExists(conh)) {
                println "creating property table"
                DatabaseProperties.createPropertyTable(conh)    
            }
            
            if (!UpdateHandler.isStructureTable(conh, structureTable)) {
                println "creating structure table $structureTable"
                StructureTableOptions opts = new StructureTableOptions(structureTable, TableTypeConstants.TABLE_TYPE_MOLECULES)
//                opts.extraColumnDefinitions = ',psa FLOAT, logp FLOAT, hba SMALLINT, hbd SMALLINT, rot_bond_count SMALLINT'
//                        opts.chemTermColsConfig = [
//                            psa: 'psa()', 
//                            logp: 'logp()', 
//                            hba: 'acceptorCount()', 
//                            hbd: 'donorCount()',
//                            rot_bond_count: 'rotatableBondCount()'
//                        ]
                opts.standardizerConfig = new File(props.standardizer).text
                UpdateHandler.createStructureTable(conh, opts)
                
                Utils.execute(db, 'create table categories',  'CREATE TABLE ' + props.schema + '''.categories (
  id SERIAL PRIMARY KEY,
  category_name VARCHAR(16),
  CONSTRAINT uq_category_name UNIQUE (category_name)
  )''')
                
                Utils.execute(db, 'create table structure_aliases',  'CREATE TABLE ' + props.schema + '''.structure_aliases (
  id SERIAL PRIMARY KEY,
  structure_id INTEGER NOT NULL,
  alias_type VARCHAR(16) NOT NULL,
  alias_value VARCHAR(32) NOT NULL,
  CONSTRAINT fk_sa2structures FOREIGN KEY (structure_id) references ''' + props.schema + ''' .structures(cd_id) ON DELETE CASCADE

  )''')
                
                Utils.execute(db, 'add index idx_sa_structure_id',   'CREATE INDEX idx_sa_structure_id on ' + props.schema + '.structure_aliases(structure_id)')
                Utils.execute(db, 'add index idx_sa_alias_type',     'CREATE INDEX idx_sa_structure_alias_type on ' + props.schema + '.structure_aliases(alias_type)')
                Utils.execute(db, 'add index idx_sa_alias_value',    'CREATE INDEX idx_sa_structure_alias_value on ' + props.schema + '.structure_aliases(alias_value)')
 
                
                Utils.execute(db, 'create table sources',  'create table ' + props.schema + '''.sources (
  id SERIAL PRIMARY KEY,
  category_id integer NOT NULL,
  source_name VARCHAR(16),
  source_description VARCHAR(500),
  type CHAR(1) NOT NULL,
  owner VARCHAR(50) NOT NULL,
  maintainer VARCHAR(50) NOT NULL,
  active BOOLEAN  DEFAULT TRUE,
  CONSTRAINT fk_sources2categories FOREIGN KEY (category_id) references ''' + props.schema + ''' .categories(id),
  CONSTRAINT uq_source_name UNIQUE (source_name)
)''')
                
                Utils.execute(db, 'create table property_definitions',  'create table ' + props.schema + '''.property_definitions (
  property_id SERIAL PRIMARY KEY,
  source_id integer NOT NULL,
  property_name VARCHAR(32) NOT NULL,
  property_description VARCHAR(500),
  original_id VARCHAR(32),
  definition TEXT,
  example TEXT,
  CONSTRAINT fk_sp2sources FOREIGN KEY (source_id) references ''' + props.schema + ''' .sources(id) ON DELETE CASCADE,
  CONSTRAINT uq_properties UNIQUE (source_id, property_id)
)''')
  
                Utils.execute(db, 'create table structure_props',  'create table ' + props.schema + '''.structure_props (
  id SERIAL PRIMARY KEY,
  source_id INTEGER NOT NULL,
  structure_id INTEGER NOT NULL,
  batch_id VARCHAR(16),
  parent_id INTEGER,
  property_id INTEGER NOT NULL,
  property_data JSONB,
  CONSTRAINT fk_sp2sources FOREIGN KEY (source_id) references ''' + props.schema + ''' .sources(id) ON DELETE CASCADE,
  CONSTRAINT fk_sp2structures FOREIGN KEY (structure_id) references ''' + props.schema + ''' .structures(cd_id) ON DELETE CASCADE
)''')
    
                Utils.execute(db, 'add index idx_sp_source_id',     'CREATE INDEX idx_sp_source_id on ' + props.schema + '.structure_props(source_id)')
                Utils.execute(db, 'add index idx_sp_structure_id',  'CREATE INDEX idx_sp_structure_id on ' + props.schema + '.structure_props(structure_id)')
                Utils.execute(db, 'add index idx_sp_batch_id',      'CREATE INDEX idx_sp_batch_id on ' + props.schema + '.structure_props(structure_id)')
                Utils.execute(db, 'add index idx_sp_parent_id',     'CREATE INDEX idx_sp_parent_id on ' + props.schema + '.structure_props(parent_id)')
                Utils.execute(db, 'add index idx_sp_property_id',   'CREATE INDEX idx_sp_property_id on ' + props.schema + '.structure_props(property_id)')
                //Utils.execute(db, 'add index idx_sp_property_data', 'CREATE INDEX idx_sp_property_data ON ' + props.schema + '.structure_props USING gin (property_data jsonb_ops)')
    
                println 'seeding categories'
                db.executeInsert('insert into ' + props.schema + '''.categories (category_name) values
('ACTIVITY_DATA'),('CALC_PROP'),('PHYSCHEM')''')
    
            }
        }
    }
    
}

