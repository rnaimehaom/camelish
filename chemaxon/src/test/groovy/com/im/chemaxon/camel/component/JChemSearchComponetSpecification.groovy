package com.im.chemaxon.camel.components

import chemaxon.jchem.db.*
import chemaxon.util.ConnectionHandler
import com.im.camel.testsupport.CamelSpecificationBase
import java.sql.Connection
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.apache.derby.jdbc.EmbeddedDataSource
import com.im.util.DbTestUtils;

class JChemSearchComponentSpecification extends CamelSpecificationBase {


    def 'test component works'() {
        
        println "running test"
        
        given:
        def mock = camelContext.getEndpoint('mock:result')
        
        

        when:
        template.sendBody('direct:molportsearch', "c1ccccc1")

        then:
        List exchs = mock.receivedExchanges
        exchs.size() == 1
    }
    
    CamelContext createCamelContext() {
        println "Creating camel context"
        EmbeddedDataSource ds = DbTestUtils.createDerbyDataSource("memory:JChemSearchComponentSpecification", true)
        
        createMolsTable(ds.connection)
        SimpleRegistry registry = new SimpleRegistry()
        registry.put('mydb', ds)
        return new DefaultCamelContext(registry)
    }
    
    void createMolsTable(Connection con) {
        println "Creating mols table"
        ConnectionHandler conh = new ConnectionHandler(con, ConnectionHandler.DEFAULT_PROPERTY_TABLE)
        DatabaseProperties.createPropertyTable(conh)
        println "property table created"
        def opts = new StructureTableOptions('MOLECULES', TableTypeConstants.TABLE_TYPE_MOLECULES)
        UpdateHandler.createStructureTable(conh, opts)
        println "MOLECULES table created"
        UpdateHandler uh = new UpdateHandler(conh, UpdateHandler.INSERT, 'MOLECULES', '')
        ['c1ccccc1', 'c1ccncc1', 'CC', 'CCC'].each {
            uh.structure = it
            uh.execute()
            println "inserted $it"
        }
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from('direct:molportsearch')
                .to('jchemsearch:test?structureTableName=molecules&searchOptions=t:s&dataSourceRef=mydb')
                .log('jchemsearch completed')
                .to('mock:result')
            }
        }
    }


}
