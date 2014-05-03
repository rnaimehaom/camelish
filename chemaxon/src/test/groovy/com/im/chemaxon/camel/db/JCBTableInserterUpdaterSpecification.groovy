package com.im.chemaxon.camel.db

import chemaxon.jchem.db.DatabaseProperties
import chemaxon.jchem.db.StructureTableOptions
import chemaxon.jchem.db.UpdateHandler
import chemaxon.util.ConnectionHandler
import com.im.camel.testsupport.CamelSpecificationBase
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import spock.lang.Shared

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Created by timbo on 14/04/2014.
 */
class JCBTableInserterUpdaterSpecification extends CamelSpecificationBase {


    def resultEndpoint
    def updateHandlerProcessor

    @Shared
    ConnectionHandler conh

    def setupSpec() { // run before the first feature method
        Connection con = DriverManager.getConnection('jdbc:derby:memory:myDB;create=true')
        conh = new ConnectionHandler()
        conh.connection = con

        DatabaseProperties.createPropertyTable(conh)
    }

    def cleanupSpec() { // run after the last feature method
        try {
            DriverManager.getConnection('jdbc:derby:memory:myDB;shutdown=true')
        } catch (SQLException e) {
            //println "shutdown successful"
        }
    }

    def 'simple structure insert'() {

        setup:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(2)

        when:
        template.sendBody('direct:start', 'c1ccccc1')
        template.sendBody('direct:start', 'c1ccncc1')

        then:
        resultEndpoint.assertIsSatisfied()
        updateHandlerProcessor.executionCount == 2
        updateHandlerProcessor.errorCount == 0
    }

    @Override
    RouteBuilder createRouteBuilder() {

        UpdateHandler.createStructureTable(conh, new StructureTableOptions('TEST'))

        updateHandlerProcessor = new JCBTableInserterUpdater(UpdateHandler.INSERT, 'TEST', null) {

            @Override
            protected void setValues(Exchange exchange, UpdateHandler updateHandler) {
                String mol = exchange.in.getBody(String.class)
                updateHandler.setStructure(mol);
            }
        }
        updateHandlerProcessor.connectionHandler = conh

        return new RouteBuilder() {
            public void configure() {
                from('direct:start')
                        .log('Processing data ${body}')
                        .process(updateHandlerProcessor)
                        .to('mock:result')
            }
        }
    }
}
