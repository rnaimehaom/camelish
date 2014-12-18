package com.im.examples.model.aggregate

import spock.lang.Specification
import com.im.examples.model.types.QualifiedNumber

class MinSpecification extends Specification {
    
    
    def "simple min values"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.values = [370.1, 500.3, 12.1]
        agg.createAggregate(new Min<Float>('Minimum'))
        
        
        then:
        agg.values.size() == 3
        agg.aggregates.size() == 1
        agg.aggregates[0].result == 12.1
        agg.aggregates[0].count == 3
        agg.aggregates[0].name == 'Minimum'
        
    }
    
    def "set values after aggregator"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.createAggregate(new Min<Float>())
        agg.values = [370.1, 500.3, 12.1]
        
        
        then:
        agg.values.size() == 3
        agg.aggregates.size() == 1
        agg.aggregates[0].result == 12.1
        agg.aggregates[0].count == 3
        
    }
    
    def "handle null values"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.values = [370.1, null, 12.1, null]
        agg.createAggregate(new Min<Float>())
        
        
        then:
        agg.values.size() == 4
        agg.aggregates.size() == 1
        agg.aggregates[0].result == 12.1
        agg.aggregates[0].count == 2
        agg.aggregates[0].name == 'Min'
        
    }
    
    def "handle text values"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.values = ['a', 'd', 'foo', 'bar']
        agg.createAggregate(new Min<String>())
        
        
        then:
        agg.values.size() == 4
        agg.aggregates.size() == 1
        agg.aggregates[0].result == 'a'
        agg.aggregates[0].count == 4
        
    }
    
    def "handle qualified values"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.values = [
            new QualifiedNumber(370.1f),
            new QualifiedNumber(500.3f),
            new QualifiedNumber(12.1f),
        ]
        agg.createAggregate(new Min<QualifiedNumber>())
        
        
        then:
        agg.values.size() == 3
        agg.aggregates.size() == 1
        agg.aggregates[0].result.value == 12.1f
        agg.aggregates[0].count == 3
        
    }

}
