package com.im.examples.model.aggregate

abstract class AggregateImpl {
        
    String name
    
    AggregateImpl(String name) {
        this.name = name
    }
        
    abstract protected void calculate(List values)
}
