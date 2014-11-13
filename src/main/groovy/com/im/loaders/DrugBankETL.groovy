package com.im.loaders

import chemaxon.jchem.db.*
import chemaxon.marvin.io.*
import chemaxon.util.ConnectionHandler
import com.im.chemaxon.io.MoleculeIOUtils
import com.im.camel.processor.ChunkBasedReporter
import com.im.camel.processor.StreamingSQLProcessor
import com.im.chemaxon.camel.db.DefaultJCBInserter
import com.im.chemaxon.camel.db.JCBTableInserterUpdater
import groovy.sql.Sql
import groovy.util.logging.*
import java.util.logging.*
import java.sql.*
import javax.sql.DataSource
import org.apache.camel.*
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry

/**
 *
 * @author timbo
 */
@Log
class DrugBankETL extends AbstractLoader  {
    
    int offset, limit
    
    ConfigObject drugbank
    private String structureTable

   
    static void main(String[] args) {
        println "Running with $args"
        
        def instance = new DrugBankETL('loaders/chemcentral.properties')
        instance.run(args)
       
    }
    
    DrugBankETL(String config) {
        super(new File(config).toURL())
        drugbank = Utils.createConfig('loaders/drugbank.properties')
        this.offset = drugbank.offset
        this.limit = drugbank.limit
        this.structureTable = props.schema + '.structures'
    }
    
    void dropTables(def props) {
        
    }
    
    void createTables(def props) { 
        
    }
    
    CamelContext createCamelContext() {
        
        SimpleRegistry registry = new SimpleRegistry()
        registry.put('chemcentral', dataSource)
        
        return new DefaultCamelContext(registry)
    }
   
    
    void executeRoutes(CamelContext camelContext) {
        
        int sourceId = Utils.createSourceDefinition(dataSource, props.schema, 1, drugbank.name, drugbank.description, 'P', drugbank.owner, drugbank.maintainer, true)
        
        if (!sourceId) {
            println "Failed to generate source ID for DrugBank"
        } else {
            int propertyId = Utils.createPropertyDefinition(dataSource, props.schema, sourceId,
                'Data from DrugBank database', null, null, null)
            
            if (!propertyId) {
                println "Failed to generate property ID for DrugBank"
            } else {
        
                println "Loading data from ${drugbank.schema}.${drugbank.table}"
                ProducerTemplate t = camelContext.createProducerTemplate()

                // Note: we are using LIMIT without and ORDER BY so if a meaningful limit is
                // specified rows retreived may not be repeatable between runs
                String s = "SELECT cd_structure, drugbank_id FROM ${drugbank.schema}.${drugbank.table}"
                if (limit) {
                    s += " LIMIT $limit"
                }
                if (offset) {
                    s += " OFFSET $offset"
                }
        
                t.sendBodyAndHeaders('direct:drugbankquery', s, [sourceId: sourceId, propertyId: propertyId])
            }
        }
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
                def mol = data['cd_structure']
                updateHandler.structure = new String(mol)
            }
            
            @Override
            protected void handleCdId(int cdid, Exchange exchange) {
                Map data = exchange.in.getBody(Map.class)
                data['cd_id'] = Math.abs(cdid)
                log.fine("Structure has CD_ID of $cdid")
                if (cdid < 0) {
                    log.fine("$cdid is duplicate for ${data['drugbank_id']}")
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
                    
                    
                    Endpoint endpoint = endpoint("seda:processor1?size=2&blockWhenFull=true")
                    
                    from(endpoint)
                    .process(createInserter())
                    .setHeader('strid', simple('${body[cd_id]}'))
                    .setHeader('drugbank_id', simple('${body[drugbank_id]}'))
                    .to('direct:chemcentralaliasload')
                    .to('direct:chemcentralpropsload')
                    .to('seda:report')  
                    
                     
                    // this is the entry point
                    from('direct:drugbankquery')
                    .log('SQL: ${body}')
                    .process(new StreamingSQLProcessor(dataSource, 500, true))
                    .split(body()).streaming()
                    //.log('Procesing drugbank_id ${body[drugbank_id]}')
                    .to(endpoint)        

                    
                    from('direct:chemcentralpropsload')
                    .setBody(constant("""\
                        |insert into ${props.schema}.structure_props
                        |  (structure_id, source_id, property_id, property_data)
                        |  select :?strid, :?sourceId, :?propertyId, row_to_json(i.*)::jsonb
                        |    from (
                        |      select drugbank_id, drug_groups, generic_name, brands
                        |      from ${drugbank.schema}.${drugbank.table}
                        |      where drugbank_id = :?drugbank_id\n\
                        |    ) i""".stripMargin()))
                    //.log('SQL: ${body}')
                    .to('jdbc:chemcentral?useHeadersAsParameters=true')
                    
                    
                    from('direct:chemcentralaliasload')
                    .setBody(constant("""insert into ${props.schema}.structure_aliases
                        (structure_id, alias_type, alias_value) values (:?strid, 'DRUGBANK', :?drugbank_id)"""))
                    //.log('SQL: ${body}')
                    .to('jdbc:chemcentral?useHeadersAsParameters=true')
                    
                    from('seda:report')
                    .process(new ChunkBasedReporter(1000))
                    
                    
                    from('direct:errors')
                    .log('Error: ${exception.message}')
                    .log('Error: ${exception.stacktrace}')

                }
            })
    }
	
}

