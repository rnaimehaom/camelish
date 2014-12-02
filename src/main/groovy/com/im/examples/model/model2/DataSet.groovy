package com.im.examples.model.model2

import groovy.transform.Canonical

/**
 *
 * @author timbo
 */
class DataSet extends RowSet {
    
    Object listId
    
    DataSet(Object id) {
        super(id)
    }
	
    Row createRow(Object id, Map data) {
        super.createRow(id, data)
    }
    
    Row addRow(Row row) {
        super.createRow(row)
    }
    
    List<Row> getRows(Object filterId) {
        Filter filter = null
        if (filterId != null) {
            filter = filters[filterId]
        }
        List rows = []
        if (filter == null) {
            rows.addAll(members.values())
        } else {
            filter.ids.each {
                rows << members[it]
            }
        }
        return rows
    }
    
    static void main(String[] args) {
        int id = 1
        DataSet structures = new DataSet('structures')
        RowSet batches = structures.createChildRowSet('batches')
        RowSet samples = batches.createChildRowSet('samples')
        RowSet atoms = samples.createChildRowSet('atoms')
        Row s1 = structures.createRow(id++, [id: 's1'])
        println "s1 $s1"
        Row s2 = structures.createRow(id++, [id: 's2'])
        println "s2 $s2"
        
        Row b1_1 = batches.createRow(id++, [id: 'b1_1'], s1)
        println "b1_1 $b1_1"
        Row b1_2 = batches.createRow(id++, [id: 'b1_2'], s1)
        println "b1_2 $b1_2"
        Row b2_1 = batches.createRow(id++, [id: 'b2_1'], s2)
        println "b2_1 $b2_1"
        
        Row m1_1_1 = samples.createRow(id++, [id: 'm1_1_1'], b1_1)
        println "m1_1_1 $m1_1_1"
        Row m1_2_1 = samples.createRow(id++, [id: 'm1_2_1'], b1_2)
        println "m1_2_1 $m1_2_1"
        Row m1_2_2 = samples.createRow(id++, [id: 'm1_2_2'], b1_2)
        println "m1_2_2 $m1_2_2"
        Row m2_1_1 = samples.createRow(id++, [id: 'm2_1_1'], b2_1)
        println "m2_1_1 $m2_1_1"
        
        Row a1_1_1 = atoms.createRow(id++, [id: 'a1_1_1'], m1_1_1)
        println "a1_1_1 $a1_1_1"
        
        println "s1 kids: ${s1.getChildren(batches)}"
        println "b1_1 kids: ${b1_1.getChildren(samples)}"
        
        println "structures members: ${structures.members.values()}"
        println "batches members: ${batches.members.values()}"
        println "samples members: ${samples.members.values()}"
        println "atoms members: ${atoms.members.values()}"
    }
}

