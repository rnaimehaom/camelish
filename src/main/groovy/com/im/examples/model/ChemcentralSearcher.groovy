package com.im.examples.model

import javax.sql.DataSource
import com.im.loaders.Utils
import groovy.transform.Canonical
import groovy.sql.Sql
import groovy.json.JsonSlurper

/**
 *
 * @author timbo
 */
class ChemcentralSearcher extends Searcher {
    
    ConfigObject chemcentral
    
    ChemcentralSearcher() {

        chemcentral = Utils.createConfig('loaders/chemcentral.properties')
        
        createDataSource(chemcentral.user, chemcentral.password)
    }
    
    Expando createModel() {
        Expando model = new Expando()
        model.setProperty('structures', [:])
        return model
    }
    
    static Expando findOrCreateStructure(Expando model, Integer id) {
        Expando structure = model.structures[id]
        if (!structure) {
            structure = new Expando()
            structure.setProperty('props', [:])
            structure.setProperty('aggregates', [:])
            model.structures[id] = structure
        }
        return structure
    }
    static Expando findOrCreateStructureProperty(Expando structure, Integer id) {
        Expando prop = structure.props[id]
        if (!prop) {
            prop = new Expando()
            prop.setProperty('data', [:])
            structure.props[id] = prop
        }
        return prop
    }
    
    static Aggregate findOrCreateAggregate(Expando structure, Integer id) {
        Aggregate agg = structure.aggregates[id]
        if (!agg) {
            agg = new Aggregate()
            structure.aggregates[id] = agg
        }
        return agg
    }
    
    void fetchCompoundProperties(Expando model, List<Integer> propertyIds) {
        QueryByPropertyId query = new QueryByPropertyId(propertyIds)
        Sql db = new Sql(dataSource)
        try {
            query.execute(db, model)
        } finally {
            db.close()
        }
    }
    
    
    static class QueryByPropertyId {
        
        List<Integer> propertyIds
        def slurper = new JsonSlurper()
        
        QueryByPropertyId(List<Integer> propertyIds) {
            this.propertyIds= propertyIds
        }
        
        void execute(Sql db, def model) {
            println "Executing query"
            
            db.eachRow("""
                    SELECT id, structure_id, batch_id, property_id, property_data
                      FROM chemcentral.structure_props
                      WHERE property_id IN (${Sql.expand(propertyIds.join(','))})""") { row ->
                
                def structure = findOrCreateStructure(model, row['structure_id'])
                def prop = findOrCreateStructureProperty(structure, row['property_id'])
                def json = slurper.parseText(row['property_data'] as String)
                prop.data[row['id']] = json
            }
            println "Finished processing query"
            
        }
    }
    
    
    static void main(String[] args) {
        ChemcentralSearcher searcher = new ChemcentralSearcher()
        Expando model = searcher.createModel()
        Runtime rt = Runtime.getRuntime()
        rt.gc()
        long f0 = rt.freeMemory()
        long m0 = rt.totalMemory()
        println "Before: $m0 $f0 = ${m0 - f0}"
        long t0 = System.currentTimeMillis()
        //searcher.fetchCompoundProperties(model, new Integer(1749612))
        //searcher.fetchCompoundProperties(model, new Integer(1639512)) // 4610
        //searcher.fetchCompoundProperties(model, [1687792]) // 1687792
        searcher.fetchCompoundProperties(model, [1639840,1639810,1639678,1639871]) // 17148
        

        long t1 = System.currentTimeMillis()

        model.structures.each { s ->
            s.value.props.each { p ->
                Aggregate agg = findOrCreateAggregate(s.value, p.key)
                p.value.data.each { d ->
                    def value = d.value.standard_value
                    agg.values << value
                    def q = QualifiedValue.Qualifier.create(d.value.standard_relation)
                }
                agg.createAggregate(Aggregate.ArithmeticMean.class, "Assay $p.key")
            }
        }

        rt.gc()
        long f1 = rt.freeMemory()
        long m1 = rt.totalMemory()
        println "After: $m1 $f1 = ${m1 - f1}"
        println "fetched ${model.structures.size()} structures in ${t1-t0}ms"
        println "memory: ${(m1-f1) - (m0-f0)}"
        
        
        //        model.structures.each { s ->
        //            println "Struct ${s.key} ="
        //            s.value.aggregates.each { a ->
        //                a.value.each {
        //                    println " ${a.key} name=${it.aggregates[0].name} mean=${it.aggregates[0].mean} n=${it.aggregates[0].count} values=${it.values}"
        //                }
        //            }
        //        }

        long t2 = System.currentTimeMillis()
        def filtered = []
        model.structures.each { s ->
            if (filter(s.value.aggregates[1639840], 1000f) && filter(s.value.aggregates[1639810], 1000f)) {
                filtered << s
            }
        }
        long t3 = System.currentTimeMillis()
        println "Filtering took ${t3 - t2}ms and found ${filtered.size()} records"
       
    }
    
    static boolean filter(def agg, float threshold) {
        if (agg && agg.aggregates.size() > 0) {
            return agg.aggregates[0].mean < threshold
        }
        return false
    }
        
} 


