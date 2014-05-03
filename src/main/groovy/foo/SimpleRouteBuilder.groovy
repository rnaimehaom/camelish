package foo

import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 13/04/2014.
 */
class SimpleRouteBuilder extends RouteBuilder {

    @Override
    void configure() {
        from('direct:start').to('mock:result')
    }
}
