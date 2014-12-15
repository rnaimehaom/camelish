package com.im.examples.model.aggregate

import spock.lang.Specification

class MaxSpecification extends Specification {
    
    
    def "simple max values"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.values = [370.1, 500.3, 12.1]
        agg.createAggregate(new Max<Float>())
        
        
        then:
        agg.values.size() == 3
        agg.aggregates.size() == 1
        agg.aggregates[0].result == 500.3
        agg.aggregates[0].count == 3
        
    }
    
    def "handle null values"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.values = [370.1, null, 12.1, null]
        agg.createAggregate(new Max<Float>())
        
        
        then:
        agg.values.size() == 4
        agg.aggregates.size() == 1
        agg.aggregates[0].result == 370.1
        agg.aggregates[0].count == 2
        
    }
    
    def "handle text values"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.values = ['a', 'd', 'foo', 'bar']
        agg.createAggregate(new Max<String>())
        
        
        then:
        agg.values.size() == 4
        agg.aggregates.size() == 1
        agg.aggregates[0].result == 'foo'
        agg.aggregates[0].count == 4
        
    }

}
