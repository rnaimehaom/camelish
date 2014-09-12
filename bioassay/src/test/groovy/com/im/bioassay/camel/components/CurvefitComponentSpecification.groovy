package com.im.bioassay.camel.components

import com.im.bioassay.doseresponse.*
import com.im.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

class CurvefitComponentSpecification extends CamelSpecificationBase {

    def mock

    def 'test component works'() {
        
        given:
        mock = camelContext.getEndpoint('mock:result')

        when:
        template.sendBody('direct:start', DataUtils.createSingle())

        then:
        List exchs = mock.receivedExchanges
        exchs.size() == 1
        def result = exchs[0].in.body
        result.fitModel.inflection != null
        println DoseResponseUtils.fromDoseResponseResult(result)
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from('direct:start').to('curvefit:default?top=100').to('mock:result')
            }
        }
    }


}
