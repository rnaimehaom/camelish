package com.im.examples.model

/**
 *
 * @author timbo
 */
class Aggregate {
    
    List values = []
    List aggregates = []
    
    void createAggregate(Class cls, String name) {
        def inst = cls.newInstance()
        inst.name = name
        inst.calculate(values)
        aggregates << inst
    }
    
    class AggregateImpl {
        
        String name
    }
    
    class ArithmeticMean extends AggregateImpl {
        
        Float mean
        int count
        
        void calculate(List values) {
            int c = 0
            float sum = 0
            values.each {
                if (it != null) {
                    c++
                    sum += it
                }
            }
            if (c) {
                mean = sum / c
            }
            count = c
        }
        
    }
    
    public static void main(String[] args) {
        Aggregate agg = new Aggregate()
        agg.values = [2f, 4f, 5f, null, 6f]
        agg.createAggregate(ArithmeticMean.class, 'Test')
        println "Aggregates: ${agg.aggregates.size()}"
        println "Average for ${agg.aggregates[0].name}: ${agg.aggregates[0].mean} (n=${agg.aggregates[0].count})"  
    }
	
}

