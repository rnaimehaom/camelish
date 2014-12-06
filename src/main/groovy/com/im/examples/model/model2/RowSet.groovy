package com.im.examples.model.model2

import groovy.transform.Canonical

class RowSet {
	
    final Object id
    final RowSet parent
    /* Might need the innnermost part to be a Map not a Set if we need to look up 
     * rows by their IDs in a fast fashion?
     */
    Map<RowSet, Map<Row, Set<Row>>> children = [:]
    /* can this somehow be combined with children? */
    Map<Object,Row> members = [:]
    
    /* Filters on the rows. All are kept so that user can step back and use a previous 
     * filter. Currently only used at top level (DataSet) but potntially can alos be applied
     * to child RowSets
     */
    Map<Object, Filter> filters = [:]
    
    /* A set of DataExtractors that extract source data and set values to the Rows
     * in this RowSet from the contents of the source data. When new Rows are added
     * existing DataExtractors should be executed to extract values, but (TODO) there
     * is currently no mechanism to know where to get the source data from.
     * Probably needs to use some sort of REST API so that the DataExtractor can 
     * be created with a URL template like https://somewhere.com/property123/{id}
     * so that the data corresponding to the Row ID can be retrieved (doing this in
     * bulk also needs consideration)
     */
    Map<Object, DataExtractor> dataExtractors = [:]
    
    RowSet(Object id) { this.id = id }
    
    RowSet(Object id, RowSet parent) { 
        this.id = id
        this.parent = parent
    }
    
    RowSet getChildRowSet(Object id) {
        children.keySet().find { it.id == id }
    }
    
    RowSet createChildRowSet(Object id) {
        RowSet rs = new RowSet(id, this)
        children[rs] = [:]
        return rs
    }
            
    protected Row createRow(Object id, Map data) {
        Row row = new Row(id, data)
        members[id] = row
        return row
    }
    
    protected Row addRow(Row row) {
        members[row.id] = row
        return row
    }
    
    Row createRow(Object id, Map data, Row parentRow) {
        Map<Row, Set> rowData = parent.children[this]
        
        if (!parent.members.keySet().contains(parentRow.id)) {
            throw new IllegalStateException("Invalid parent row")
        }
        Set kids = rowData[parent]
        if (kids == null) {
            kids = [] as HashSet
            rowData[parent] = kids
        }

        Row child = createRow(id, data)
        kids << child
        return child
    }
    
    Row createChildRow(RowSet childRowSet, Row parent, Map data) {
        Map<Row, Set> rowData = children[childRowSet]
        if (rowData == null) {
            throw new IllegalStateException("RowSet is not a child of this RowSet")
        }
        if (!members.keySet().contains(parent.id)) {
            throw new IllegalStateException("Parent is not part of this RowSet")
        }
        Set kids = rowData[parent]
        if (kids == null) {
            kids = [] as HashSet
            rowData[parent] = kids
        }

        Row child = childRowSet.createRow(data)
        // TODO handle data extraction
        kids << child
        return child
    }
    
    /** Optimised for bulk additions, primarily to allow bulk retrieval/extraction
     * of derived properties
     */
    Row createChildRows(RowSet childRowSet, Map<Row, Map> data) {
        data.each { k,v ->
            createChildRows(childRowSet, k, v)
        }
    }
    
    Set getChildren(RowSet childRowSet, Row row) {
        children[childRowSet][row]
    }
    
    DataExtractor createDataExtractor(Object propertyDefId, Closure closure) {
        DataExtractor extractor = new DataExtractor(this, propertyDefId, closure)
        dataExtractors[propertyDefId] = extractor
        return extractor
    }
    
    @Canonical
    class DataExtractor {
        RowSet rowset
        Object propertyDefId
        Closure closure
        
        void extract(Object parentId, Object propertyId, def json) {
            Map data = closure(json)
            def parentRow = rowset.parent.members[parentId]
            if (parentRow) {
                rowset.createRow(propertyId, data, parentRow)
            }
        }
    }
    
    class Row {   
        Object id
        Map data
        Row(Object id, Map data) {
            this.id = id
            this.data = (data ?: [:])
        }
 
        Set getChildren(RowSet childRowSet) {
            getChildren(childRowSet, this)
        }
        
        @Override
        public String toString() {
            return "Row [parent: ${RowSet.this.id} id: $id ${data.size()} data items]"
        }
    }
    
    Filter createFilter(String owner, closure) {
        List<Object> ids = closure(this)
        Filter f = new Filter(UUID.randomUUID().toString(), new Date(), owner, ids, closure)
        filters[f.uuid] = f
        return f
    }
   
}