package com.im.examples.model.aggregate

class ArithmeticMean<T> extends SimpleAggregate<T> {
    
    ArithmeticMean(String name) {
        super(name)
    }
    
    ArithmeticMean() {
        super('ArithmeticMean')
    }
        
    void calculate(List<T> values) {
        int c = 0
        T sum = null
        values.each {
            if (it != null) {
                c++
                if (sum == null) {
                    sum = it
                } else { 
                    sum += it
                }
            }
        }
        if (c) {
            result = sum / c
        } else {
            result = null
        }
        count = c
    }
        
}