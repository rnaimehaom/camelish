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
class ChemblCamelETL extends AbstractLoader  {
    
    int offset, limit
    
    ConfigObject chembl
    private String structureTable, concordanceTable

   
    static void main(String[] args) {
        println "Running with $args"
        
        def instance = new ChemblCamelETL('loaders/chemcentral.properties')
        instance.run(args)
       
    }
    
    ChemblCamelETL(String config) {
        super(new File(config).toURL())
        chembl = Utils.createConfig('loaders/chembl.properties')
        this.offset = chembl.offset
        this.limit = chembl.limit
        this.structureTable = props.schema + '.structures'
        this.concordanceTable = chembl.schema + '.concordance'
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
        
        
        println "Loading data from ${chembl.schema}.compound_structures"
        ProducerTemplate t = camelContext.createProducerTemplate()

        // Note: we are using LIMIT without and ORDER BY so if a meaningful limit is
        // specified rows retreived may not be repeatable between runs
        String s = """\
                |SELECT st.molregno, st.molfile
                |  FROM ${chembl.schema}.compound_structures""".stripMargin()
        if (limit) {
            s += " LIMIT $limit"
        }
        if (offset) {
            s += " OFFSET $offset"
        }
        //println "SQL: $s"
        
        t.sendBody('direct:chemblmolquery')
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
                    
                    
                    def numbers = 1..props.processors
                    List<Endpoint> endpoints = []
                    numbers.each {
                        endpoints << endpoint("seda:processor${it}?size=2&blockWhenFull=true")
                    }
                    
                    endpoints.each {
                        from(it)
                        .process(createInserter())
                        .setHeader('sid', simple('${body[cd_id]}'))
                        .setHeader('molregno', simple('${body[molregno]}'))
                        .setBody("INSERT INTO $concordanceTable (structure_id, molregno) VALUES (:?sid, :?molregno)")
                        .to("jdbc:chemcentral")
                        .to('seda:report')  
                    }
                    
                    
                    // this is the entry point
                    from('direct:chemblmolquery')
                    .log('SQL: ${body}')
                    .process(new StreamingSQLProcessor(dataSource, 500, true))
                    .split(body()).streaming()
                    .log(LoggingLevel.DEBUG, 'Procesing molregno ${body[molregno]}')
                    .loadBalance().roundRobin().to(
                        endpoints
                    )            
                    
                    
                    from('direct:errors')
                    .log('Error: ${exception.message}')
                    .log('Error: ${exception.stacktrace}')

                }
            })
    }


}

