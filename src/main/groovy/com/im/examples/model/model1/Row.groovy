package com.im.examples.model.model1

import groovy.json.JsonOutput


/**
 *
 * @author timbo
 */
class Row extends HashMap {
    
    
    Row(Map props) {
        props.each { k, v->
            this[k] = v
        }
    }
    
    RowSet createChildren(String type) {
        def rowset = new RowSet()
        this[type] = rowset
        return rowset
    }
    
    //    RowSet addChild(String type, Row child) {
    //        this[type] << child
    //        return this
    //    }
    
    static void main(String[] args) {
        def structures = new RowSet()
        def batches, batch, ass
        batches = structures.addRow(['id': 1, 's1': 13.2, 's2': 123.3]).createChildren('batches')
        batch = batches.addRow(['id': 1, 'b1': 'red', 'b2': 13.2])
        batch = batches.addRow(['id': 2, 'b1': 'yellow', 'b2': 11.2])
            
        batches = structures.addRow(['id': 2, 's1': 11.2, 's2': 103.3]).createChildren('batches')
        batch = batches.addRow(['id': 1, 'b1': 'green', 'b2': 3.2])
        batch = batches.addRow(['id': 2, 'b1': 'orange', 'b2': 9.2])
            
        structures.addRow(['id': 3, 's1': 14.2, 's2': 113.3]).createChildren('batches')
        batch = batches.addRow(['id': 1, 'b1': 'red', 'b2': 8.2])
        batch = batches.addRow(['id': 2, 'b1': 'blue', 'b2': 5.2])
        ass = batch.createChildren('assay1')
        ass.addRow(['id': 1, 'ic50': 8.2, hill: 0.97])
        ass.addRow(['id': 1, 'ic50': 4.1, hill: 0.93])
        
        structures.aggregate('b2 sum') { p ->
            if (p.batches) {
                float sum = 0f 
                p.batches.each {
                    sum += it.b2
                }
                return sum
            } else {
                return null
            }
        }
        
        println "JSON: ${JsonOutput.prettyPrint(JsonOutput.toJson(structures))}"
    }
    
    
}

