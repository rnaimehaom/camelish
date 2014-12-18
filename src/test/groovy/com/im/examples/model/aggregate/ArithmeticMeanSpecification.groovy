package com.im.examples.model.aggregate

import spock.lang.Specification
import com.im.examples.model.types.QualifiedNumber

class ArithmeticMeanSpecification extends Specification {
    
    
    def "simple min values"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.values = [100.0, 200.0, 300.0]
        agg.createAggregate(new ArithmeticMean('ArithmeticMean'))
        
        
        then:
        agg.values.size() == 3
        agg.aggregates.size() == 1
        agg.aggregates[0].result == 200.0
        agg.aggregates[0].count == 3
        
    }
    
    def "handle null values"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.values = [100.0, null, 300.0, null]
        agg.createAggregate(new ArithmeticMean('ArithmeticMean'))
        
        
        then:
        agg.values.size() == 4
        agg.aggregates.size() == 1
        agg.aggregates[0].result == 200.0
        agg.aggregates[0].count == 2
        
    }
    
    
    def "handle qualified values"() {

        when:
        AggregateHolder agg = new AggregateHolder()
        agg.values = [new QualifiedNumber(100.0), new QualifiedNumber(200.0), new QualifiedNumber(300.0)]
        agg.createAggregate(new ArithmeticMean())
        
        
        then:
        agg.values.size() == 3
        agg.aggregates.size() == 1
        agg.aggregates[0].result.value == 200.0
        agg.aggregates[0].count == 3
        
    }
    
}
