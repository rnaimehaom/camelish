package com.im.examples.model.aggregate

/** Simple aggregate that geenrates a value and a count 
 *
 * @author timbo
 */
abstract class SimpleAggregate<T> extends AggregateImpl {
        
    T result
    int count
    
    SimpleAggregate(String name) {
        super(name)
    }
    
    @Override
    String toString() {
        return "$name=$result (n=$count)"
    }
	
}

