package com.im.chemaxon.camel.io

import com.im.camel.testsupport.CamelSpecificationBase
import com.im.chemaxon.io.MoleculeIOUtils
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class FileToMoleculeSplitterSpecification extends CamelSpecificationBase {


    def resultEndpoint

    def 'simple smiles splitter'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(5)

        when:
        template.sendBody('direct:start', 'c1ccccc1\nc1ccncc1\nC\nCC\nCCC\n')

        then:
        resultEndpoint.assertIsSatisfied()
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from('direct:start')
                        .log('Processing data ${body}')
                        .split().method(MoleculeIOUtils.class, "mrecordIterator")
                        .to('mock:result')
            }
        }
    }
}
