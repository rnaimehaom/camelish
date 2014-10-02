package com.im.chemaxon.camel.components

import com.im.camel.testsupport.CamelSpecificationBase
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.apache.derby.jdbc.EmbeddedDataSource
import com.im.util.DbTestUtils;

class JChemBaseComponentSpecification extends CamelSpecificationBase {

    def mock

    def 'test component works'() {
        
        given:
        mock = camelContext.getEndpoint('mock:result')

        when:
        template.sendBody('direct:start', "c1ccccc1")

        then:
        List exchs = mock.receivedExchanges
        exchs.size() == 1
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from('direct:start')
                .to("jchembase:testonly?mode=insert&structureTableName=TEST123&createTable=always&extraColumns=name VARCHAR(100), age INTEGER&dataSourceRef=mydb")
                .to('mock:result')
            }
        }
    }
    
    CamelContext createCamelContext() {
        
        EmbeddedDataSource ds = DbTestUtils.createDerbyDataSource("memory:JChemBaseComponentSpecification", true)

        SimpleRegistry registry = new SimpleRegistry()
        registry.put('mydb', ds)
        return new DefaultCamelContext(registry)
    }

}
