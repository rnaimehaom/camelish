import org.apache.camel.*
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.processor.aggregate.AggregationStrategy

def numbers = 0..99
def letters = A..Z

def aggregator = new AggregationStrategy() {
    Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        return newExchange
    }
}


CamelContext camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            from('direct:start')
            .aggregate(aggregator)
            .log('row: ${body}')
        }
    })
camelContext.start()

ProducerTemplate t = camelContext.createProducerTemplate()
t.sendBody('direct:start', new File('/Users/timbo/data/structures/emolecules/test.smi'))

sleep(60000)
                                           
camelContext.stop()
