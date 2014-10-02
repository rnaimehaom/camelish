package com.im.chemaxon.examples.jchemsearch

import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext

CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            from("direct:one")
            .log('direct one: ${body}')
            
            from("direct:two?block=true")
            .log('direct two: ${body}')
            
            
  
 
        }
    })

camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
t.sendBody('direct:one', 'hello one')
t.sendBody('direct:two', 'hello two')

sleep(1000)
camelContext.stop()