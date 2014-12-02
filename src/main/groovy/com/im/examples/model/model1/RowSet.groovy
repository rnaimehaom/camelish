package com.im.examples.model.model1

/**
 *
 * @author timbo
 */
class RowSet extends HashSet {
   
    Row addRow(Row row) {
        this << row
        return row
    }
    
    Row addRow(Map data) {
        Row row = new Row(data)
        this << row
        return row
    }
    
    /** Aggregate data from the child level as a new property at this level
     * 
     * @param propName The name of the new aggregated property
     * @param func A closure that does the averaging. Is passed in the the parent
     */
    void aggregate(String propName, Closure func) {
        this.each { p ->
            p[propName] = func(p)
        }
    }
	
}

