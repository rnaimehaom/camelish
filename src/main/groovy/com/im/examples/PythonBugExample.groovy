package com.im.examples

import org.apache.camel.*
import org.apache.camel.impl.*
import org.apache.camel.builder.*

String script = URLEncoder.encode('"Hello world!"', "UTF-8") // this works - script returns "Hello world!"
//String script = URLEncoder.encode('bar = "baz"; "Hello world!"', "UTF-8") // this fails - script returns null

CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            from("direct:python")
            .to("language:python:" + script)
        }
    }
)

camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
def result = t.requestBody('direct:python', 'foo')
println result

camelContext.stop()