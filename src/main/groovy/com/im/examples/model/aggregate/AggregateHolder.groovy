package com.im.examples.model.aggregate

/** Handles one or more aggregations for a set of values.
 * Typical usage:
 * <code>
 * AggregateHolder agg = new AggregateHolder("Assay XYZ IC50")
 * agg.setValues([1.23f, 3.45f, 5.32f, 2.67f])
 * agg.createAggregate(new Min<Float>())
 * agg.createAggregate(new Max<Float>())
 * println agg
 * </code>
 *
 * @author timbo
 */
class AggregateHolder {
    
    String name
    List values = []
    List aggregates = []
    
    AggregateHolder(String name) {
        this.name = name
    }
    
    void createAggregate(AggregateImpl inst) {
        inst.calculate(values)
        aggregates << inst
    }
    
    void setValues(List values) {
        aggregates.each {
            it.calculate(values)
        }
        this.values = values
    }
    
    @Override
    String toString() {
        StringBuilder b = new StringBuilder(name)
        aggregates.each {
            b.append('\n  ')
            .append(it.toString())
        }
        return b.toString()
    }
        
    
    public static void main(String[] args) {
        AggregateHolder agg = new AggregateHolder("Assay XYZ IC50")
        agg.setValues([1.23f, 3.45f, 5.32f, 2.67f])
        agg.createAggregate(new Min<Float>())
        agg.createAggregate(new Max<Float>())
        println agg
        
    }
	
}

