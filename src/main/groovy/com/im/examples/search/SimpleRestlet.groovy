package com.im.examples.search

import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.model.rest.RestBindingMode
import org.apache.camel.model.dataformat.JsonLibrary

String HOST = 'localhost'
String PORT = '43256'
String BASE_URL = "$HOST:$PORT/search"

class Person {
    String firstName
    String lastName
}

CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            
            restConfiguration().component("restlet").host(HOST).port(PORT).bindingMode(RestBindingMode.json)
            
            rest("/search")
            .post("/foo").type(Person.class).to("direct:end")
            
            rest("/search")
            .post("/bar").to("direct:end")
            
            from('direct:start')
            .log('BODY: ${body}')
            .marshal().json(JsonLibrary.Jackson)
            .log('JSON: ${body}')
            .to("http4:$BASE_URL/bar")
            
            from('direct:end')
            .log('BODY: ${body}')
            .transform().constant('Welcome') 
            
            from('direct:testmarshal')
            .marshal().json(JsonLibrary.Jackson)
            .log('JSON: ${body}')
        }
    })
camelContext.start()

Person p = new Person(firstName: 'John', lastName: 'Doe')
ProducerTemplate t = camelContext.createProducerTemplate()
//t.sendBody('direct:testmarshal', p)
//def resp = t.requestBody("http4:$BASE_URL/foo", p)

//def resp = t.requestBody("restlet:http://$BASE_URL/foo?restletMethod=POST", p)
def resp = t.requestBody('direct:start', p)
println "resp: $resp"

camelContext.stop()
