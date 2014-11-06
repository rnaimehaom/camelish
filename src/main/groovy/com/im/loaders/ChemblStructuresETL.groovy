package com.im.loaders

import chemaxon.jchem.db.*
import chemaxon.marvin.io.*
import chemaxon.util.ConnectionHandler
import com.im.chemaxon.io.MoleculeIOUtils
import com.im.camel.processor.ChunkBasedReporter
import com.im.chemaxon.camel.db.DefaultJCBInserter
import com.im.chemaxon.camel.db.JCBTableInserterUpdater
import groovy.sql.Sql
import groovy.util.logging.*
import java.util.logging.*
import java.sql.*
import javax.sql.DataSource
import org.apache.camel.*
import org.apache.camel.processor.aggregate.AggregationStrategy
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.postgresql.ds.PGSimpleDataSource

/**
 *
 * @author timbo
 */
@Log
class ChemblStructuresETL extends AbstractLoader  {
    
    int offset, limit
    
    ConfigObject chembl
    private String structureTable

   
    static void main(String[] args) {
        println "Running with $args"
        
        def instance = new ChemblStructuresETL('loaders/chemcentral.properties')
        instance.run(['dropTables','createTables', 'loadData'] as String[])
       
    }
    
    ChemblStructuresETL(String config) {
        super(new File(config).toURL())
        chembl = createConfig('loaders/chembl.properties')
        this.offset = chembl.offset
        this.limit = chembl.limit
        this.structureTable = props.schema + '.structures'
    }
    
    DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource()
        ds.serverName = database.server
        ds.portNumber = database.port
        ds.databaseName = database.database
        ds.user = props.user
        ds.password = props.password
        
        return ds
    }
    

    void dropTables(def props) {
        if (!props.allowRecreate) {
            println "Will not drop tables. Please set allowRecreate property in chemcentral.properties to true to permit this"
        } else {
            
            ConnectionHandler conh = createConnectionHandler()
            Sql db = new Sql(conh.getConnection())
            
            executeMayFail(db, 'drop table structure_props', 'DROP TABLE ' + props.schema + '.structure_props')
            executeMayFail(db, 'drop table sources', 'DROP TABLE ' + props.schema + '.sources')
            executeMayFail(db, 'drop table categories', 'DROP TABLE ' + props.schema + '.categories')
            executeMayFail(db, 'drop table structure_aliases', 'DROP TABLE ' + props.schema + '.structure_aliases')
  
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
                opts.extraColumnDefinitions = ',psa FLOAT, logp FLOAT, hba SMALLINT, hbd SMALLINT, rot_bond_count SMALLINT'
                //        opts.chemTermColsConfig = [
                //            psa: 'psa()', 
                //            logp: 'logp()', 
                //            hba: 'acceptorCount()', 
                //            hbd: 'donorCount()',
                //            rot_bond_count: 'rotatableBondCount()'
                //        ]
                opts.standardizerConfig = new File(props.standardizer).text
                UpdateHandler.createStructureTable(conh, opts)
                
                execute(db, 'create table categories',  'CREATE TABLE ' + props.schema + '''.categories (
  id SERIAL PRIMARY KEY,
  category_name VARCHAR(16),
  CONSTRAINT uq_category_name UNIQUE (category_name)
  )''')
                
                execute(db, 'create table structure_aliases',  'CREATE TABLE ' + props.schema + '''.structure_aliases (
  id SERIAL PRIMARY KEY,
  structure_id INTEGER NOT NULL,
  alias_type VARCHAR(16) NOT NULL,
  alias_value VARCHAR(32) NOT NULL,
  constraint fk_sa2structures FOREIGN KEY (structure_id) references ''' + props.schema + ''' .structures(cd_id) ON DELETE CASCADE

  )''')
                
                execute(db, 'add index idx_sa_structure_id',   'CREATE INDEX idx_sa_structure_id on ' + props.schema + '.structure_aliases(structure_id)')
                execute(db, 'add index idx_sa_alias_type',     'CREATE INDEX idx_sa_structure_alias_type on ' + props.schema + '.structure_aliases(alias_type)')
                execute(db, 'add index idx_sa_alias_value',    'CREATE INDEX idx_sa_structure_alias_value on ' + props.schema + '.structure_aliases(alias_value)')
 
                
                execute(db, 'create table sources',  'create table ' + props.schema + '''.sources (
  id SERIAL PRIMARY KEY,
  category_id integer NOT NULL,
  source_name VARCHAR(16),
  source_description VARCHAR(500),
  type CHAR(1) NOT NULL,
  active BOOLEAN  DEFAULT TRUE,
  CONSTRAINT fk_sources2categories FOREIGN KEY (category_id) references ''' + props.schema + ''' .categories(id),
  CONSTRAINT uq_source_name UNIQUE (source_name)
)''')
  
                execute(db, 'create table structure_props',  'create table ' + props.schema + '''.structure_props (
  id SERIAL PRIMARY KEY,
  source_id INTEGER NOT NULL,
  structure_id INTEGER NOT NULL,
  batch_id VARCHAR(16),
  parent_id INTEGER,
  property_id INTEGER NOT NULL,
  property_data JSONB,
  constraint fk_sp2sources FOREIGN KEY (source_id) references ''' + props.schema + ''' .sources(id) ON DELETE CASCADE,
  constraint fk_sp2structures FOREIGN KEY (structure_id) references ''' + props.schema + ''' .structures(cd_id) ON DELETE CASCADE
);''')
    
                execute(db, 'add index idx_sp_source_id',     'CREATE INDEX idx_sp_source_id on ' + props.schema + '.structure_props(source_id)')
                execute(db, 'add index idx_sp_structure_id',  'CREATE INDEX idx_sp_structure_id on ' + props.schema + '.structure_props(structure_id)')
                execute(db, 'add index idx_sp_batch_id',      'CREATE INDEX idx_sp_batch_id on ' + props.schema + '.structure_props(structure_id)')
                execute(db, 'add index idx_sp_parent_id',     'CREATE INDEX idx_sp_parent_id on ' + props.schema + '.structure_props(parent_id)')
                execute(db, 'add index idx_sp_property_id',   'CREATE INDEX idx_sp_property_id on ' + props.schema + '.structure_props(property_id)')
                execute(db, 'add index idx_sp_property_data', 'CREATE INDEX idx_sp_property_data ON ' + props.schema + '.structure_props USING gin (property_data jsonb_ops)')
    
                execute(db, 'seeding categories', 'insert into ' + props.schema + '''.categories (category_name) values
('ACTIVITY_DATA'),('CALC_PROP'),('PHYSCHEM')''')
    
                execute(db, 'seeding sources', 'insert into ' + props.schema + '''.sources (category_id, source_name, source_description, type, active) 
values (1, 'CHEMBL', 'ChEMBL 19', 'P', 'Y')''')
            }
        }
    }
    
    CamelContext createCamelContext() {
        
        SimpleRegistry registry = new SimpleRegistry()
        registry.put('chemcentral', dataSource)
        
        return new DefaultCamelContext(registry)
    }
   
    
    void executeRoutes(CamelContext camelContext) {
        println "Loading data from ${chembl.schema}.compound_structures"
        ProducerTemplate t = camelContext.createProducerTemplate()

        // Note: we are using LIMIT without and ORDER BY so if a meaningful limit is
        // specified rows retreived may not be repeatable between runs
        String s = """
SELECT st.molregno, st.molfile, cl.chembl_id
FROM ${chembl.schema}.compound_structures st
JOIN ${chembl.schema}.chembl_id_lookup cl ON cl.entity_id = st.molregno AND cl.entity_type = 'COMPOUND'"""
        if (limit) {
            s += " LIMIT $limit"
        }
        if (offset) {
            s += " OFFSET $offset"
        }
        //println "SQL: $s"
        
        t.sendBody('direct:chemblmolquery', s)
    }
    
    JCBTableInserterUpdater createInserter() {
        JCBTableInserterUpdater structuresInserter = new JCBTableInserterUpdater(
            UpdateHandler.INSERT, props.schema + '.structures', null) {
            
            @Override
            protected void configure(UpdateHandler uh) {
                uh.duplicateFiltering = UpdateHandler.DUPLICATE_FILTERING_ON
            }
            
            @Override
            protected void setValues(Exchange exchange, UpdateHandler updateHandler) {
                Map data = exchange.in.getBody(Map.class)
                updateHandler.structure = data['molfile']
            }
            
            @Override
            protected void handleCdId(int cdid, Exchange exchange) {
                Map data = exchange.in.getBody(Map.class)
                data['cd_id'] = Math.abs(cdid)
                log.fine("Structure has CD_ID of $cdid")
                if (cdid < 0) {
                    log.fine("$cdid is duplicate for ${data['molregno']}")
                }
            }
        }
        
        ConnectionHandler conh = new ConnectionHandler(dataSource.getConnection(), props.schema + '.jchemproperties')
        structuresInserter.connectionHandler = conh
        
        return structuresInserter
    }
   
    void createRoutes(CamelContext camelContext) {
        
        camelContext.addRoutes(new RouteBuilder() {
                def void configure() {
                    
                    onException()
                    .handled(true)
                    .to('direct:errors')
                    
                    
                    def numbers = 1..8 //props.processors
                    List<Endpoint> endpoints = []
                    numbers.each {
                        endpoints << endpoint("seda:processor${it}?size=5&blockWhenFull=true")
                    }
                    
                    endpoints.each {
                        from(it)
                        .process(createInserter())
                        .to('direct:chemcentralaliasload')
                        .to('direct:chemcentralpropsload')
                        .to('seda:report')  
                    }
                     
                    // this is the entry point
                    from('direct:chemblmolquery')
                    .to('jdbc:chemcentral?outputType=StreamList')
                    .split(body()).streaming()
                    .log(LoggingLevel.DEBUG, 'Procesing molregno ${body[molregno]}')
                    .loadBalance().roundRobin().to(
                        endpoints
                    )      

                    
                    from('direct:chemcentralpropsload')
                    .setHeader('sid', simple('${body[cd_id]}'))
                    .setHeader('molregno', simple('${body[molregno]}'))
                    .setHeader('cid', simple('${body[chembl_id]}'))
                    .setBody(constant("""insert into ${props.schema}.structure_props
                        (structure_id, source_id, batch_id, property_id, property_data)
                        select :?sid, 1, :?cid, assay_id, row_to_json(activities)::jsonb
                        from ${chembl.schema}.activities where molregno = :?molregno"""))
                    //.log('SQL: ${body}')
                    .to('jdbc:chemcentral?useHeadersAsParameters=true')
                    
                    
                    from('direct:chemcentralaliasload')
                    .setHeader('sid', simple('${body[cd_id]}'))
                    .setHeader('cid', simple('${body[chembl_id]}'))
                    .setBody(constant("""insert into ${props.schema}.structure_aliases
                        (structure_id, alias_type, alias_value) values (?sid, 'chembl', :?cid)"""))
                    //.log('SQL: ${body}')
                    .to('jdbc:chemcentral?useHeadersAsParameters=true')
                    
                    from('seda:report')
                    .process(new ChunkBasedReporter(props.reportingChunk))
                    
                    
                    from('direct:errors')
                    .log('Error: ${exception.message}')
                    //.log('Error: ${exception.stacktrace}')
                }
            })
    }
	
}

