package com.im.examples.model.model2.client

import com.im.examples.model.types.QualifiedValue
import com.im.examples.model.model2.*
import com.im.examples.model.model2.server.DemoServer

/** this simulates a client of the server that creates a dataset by querying chemcentral
 * to generate an initial dataset and then filters it.
 * 
 */

def server = new DemoServer()
try {
   
    // clean out the lists etc.
    execute("Cleaning...") {
        server.clean()
        println "  clean complete"
    }
    
    println "------------ ChEMBL ---------------"
    // 22s, 506MB
    
    // get all structures that have adata for a particular assay
    SubsetInfo listInfo = execute("Generating list for CHEMBL1614459") {
        SubsetInfo li = server.queryForStructuresForPropertyDef('tim', 'test 2', 'CHEMBL1614459')
        println "  generated results for list $li.id"
        return li
    }
  
    String chemblDataset = 'CHEMBL1614459'
    execute("Restoring dataset $listInfo.id as $chemblDataset") {
        server.restoreDataSet(chemblDataset, listInfo.id)
        println "  dataset restored"
    }
    
    [
        CHEMBL1614459: 'Lipid Storage Modulators',
        CHEMBL1794375: 'Inhibitors of binding or entry into Marburg Virus'
    ].each { chemblid, propname ->
        execute("Merging properties for $chemblid") {
            server.mergeProperties(chemblDataset, chemblid, propname) { json ->
                def props = [:]
                String v_str = json.standard_value
                String q_str = json.standard_relation
                props['value'] = new QualifiedValue(new Float(v_str), q_str)
                    
                return props
            }
            println "  merge complete"
        }
    }
    
    // run a simple filter
    SubsetInfo filter = execute("Simple filtering") {
        SubsetInfo f = server.filter(chemblDataset, 'Tim') { dataset -> 
            // simple case of taking the first 100
            def hits = []
            def keys = dataset.members.keySet()
            (0..99).each {
                hits << keys[it]
            }
            return hits
        }
        println "  filter $f.id contains ${f.size} items"
        return f
    }
            
    execute("Getting data") {
        def data = server.fetchData(chemblDataset, filter)
        println "  fetched ${data.size()} rows"
    }
            
    execute("Cleaning...") {
        server.clean()
        println "  clean complete"
    }
} finally {
    server.close()
}


static def execute(String desc, Closure closure) {
    println desc
    Runtime rt = Runtime.getRuntime()
    rt.gc()
    long f0 = rt.freeMemory()
    long m0 = rt.totalMemory()
    long t0 = System.currentTimeMillis()
    def result = closure()
    long t1 = System.currentTimeMillis()
    rt.gc()
    long f1 = rt.freeMemory()
    long m1 = rt.totalMemory()
    println "  took ${t1-t0}ms"
    println "  memory before: ${m0 - f0}"
    println "  memory after: ${m1 - f1}"
    println "  memory diff: ${(m1-f1) - (m0-f0)}"
    return result
}  
