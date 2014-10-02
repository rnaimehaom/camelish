/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.util

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class BatchHandlerTest extends Specification {
	
    void "check sized based"() {
        println "check sized based()"
        
        given:
        def list = [1,2,3,4,5,6,7,8,9,0]
        def handler1 = new CountingBatchHandler()
        handler1.size = 2
        def handler2 = new CountingBatchHandler()
        handler2.size = 3
        def handler3 = new CountingBatchHandler()
        
        when:
        handler1.process(list)
        handler2.process(list)
        handler3.process(list)
        
        then:
        handler1.count == 5
        handler2.count == 4
        handler3.count == 1
    }
    
    void "check time based"() {
        println "check time based()"
        
        given:
        def list = [1,2,3,4,5,6,7,8,9,0]
        def handler1 = new CountingBatchHandler()
        handler1.time = 10
        handler1.size = 0
        handler1.sleep = 50
        
        def handler2 = new CountingBatchHandler()
        handler2.time = 25
        handler2.size = 0
        handler2.sleep = 10
        
        when:
        handler1.process(list)
        handler2.process(list)
        
        then:
        handler1.count == 10
        handler2.count > 1
        handler2.count < 10
        
    }
    
    void "check size and time based"() {
        println "check size and time based()"
        
        given:
        def list = [1,2,3,4,5,6,7,8,9,0]
        def handler1 = new CountingBatchHandler()
        handler1.time = 40
        handler1.size = 3
        handler1.sleep = 10
        
        def handler2 = new CountingBatchHandler()
        handler2.time = 0
        handler2.size = 0
        
        when:
        handler1.process(list)
        handler2.process(list)
        
        then:
        handler1.count > 3
        handler1.count < 10
        handler2.count == 1
        
    }
    
    
    class CountingBatchHandler<Integer> extends BatchHandler {
        int count = 0
        int sleep = 0
        void handle(Iterator<Integer> it) {
            println it
            count++
        } 
        Integer handleItem(Integer item) {
            if (sleep > 0) {
                println "sleeping $sleep"
                Thread.sleep(sleep)
            }
            return item
        }
    }
    
}

