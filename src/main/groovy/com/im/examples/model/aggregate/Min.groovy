package com.im.examples.model.aggregate

class Min<T> extends PickOneAggregate<T> {
    
    Min(String name) {
        super(name)
    }
    
    Min() {
        super('Min')
    }
    
    T pick(T current, T value) {
        (value.compareTo(current) < 0) ? value : current 
    }
        
}