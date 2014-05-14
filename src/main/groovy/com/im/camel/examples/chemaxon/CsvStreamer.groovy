package com.im.camel.examples.chemaxon

import org.apache.camel.*
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.model.dataformat.CsvDataFormat

CsvDataFormat csv = new CsvDataFormat()
csv.setDelimiter(' ')
csv.setSkipFirstLine(false)
csv.setLazyLoad(true)

CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            from('direct:start')
            .unmarshal(csv)
            .split(body()).streaming()
            .log('row: ${body}')
        }
    })
camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
t.sendBody('direct:start', new File('/Users/timbo/data/structures/emolecules/test.smi'))

sleep(60000)
                                           
camelContext.stop()
