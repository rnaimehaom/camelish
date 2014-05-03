package foo

import com.im.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

class SimpleExternalRouteSpecification extends CamelSpecificationBase {

    def resultEndpoint

    def 'test simple route'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedBodiesReceived('hello')

        when:
        template.sendBody('direct:start', 'hello')

        then:
        resultEndpoint.assertIsSatisfied()
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new SimpleRouteBuilder()
    }


}
