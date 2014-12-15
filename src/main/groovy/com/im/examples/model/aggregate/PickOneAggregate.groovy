/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.examples.model.aggregate

/**
 *
 * @author timbo
 */
abstract class PickOneAggregate<T> extends SimpleAggregate<T> {
    
    PickOneAggregate(String name) {
        super(name)
    }
	
    void calculate(List<T> values) {
        int c = 0
        T current = null
        values.each {
            if (it != null) {
                c++
                if (current == null) {
                    current = it
                } else {
                    current = pick(current, it)
                }
            }
        }
        result = current
        count = c
    }
    
    abstract T pick(T current, T value)
}

