package com.im.examples.model.aggregate

class Max<T> extends PickOneAggregate<T> {
    
    Max(String name) {
        super(name)
    }
    
    Max() {
        super('Max')
    }
    
    T pick(T current, T value) {
        (value.compareTo(current) > 0) ? value : current 
    }
        
}