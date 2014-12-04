package com.im.examples.model.model2.client

import com.im.examples.model.QualifiedValue
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
    
    println "------------ DrugBank ---------------"
    
    // This simulates generating an intial dataset based on a really stupid query
    SubsetInfo listInfo1 = execute("Generating list for query") {
        SubsetInfo li = server.queryForFirstStructuresAsList('tim', 'test 1', 2000)
        println "  generated results for list $li.id"
        return li
    }
            
    // this simulates restoring a persisted dataset and making it available for use
    String drugbankDataset = 'drugbank'
    execute("Restoring dataset $listInfo1.id as $drugbankDataset") {
        server.restoreDataSet(drugbankDataset, listInfo1.id)
        println "  dataset restored"
    }
    
    // run a simple filter
    SubsetInfo filterInfo = execute("Simple filtering") {
        /* This is stop-gap measure to provide filtering just by passing a closure 
         * that does the filtering.
         * TODO - Need to establish how this would best be implemented.
         */
        SubsetInfo filterInfo = server.filter(drugbankDataset, 'tim') { dataset -> 
            // simple case of taking the first 100
            def hits = []
            def keys = dataset.members.keySet()
            (0..99).each {
                hits << keys[it]
            }
            return hits
        }
        println "  filter $filterInfo.id generated with $filterInfo.size results"
        return filterInfo
    }
            
    execute("Merging properties for DrugBank") {
        server.mergeProperties(drugbankDataset, 'DrugBank', 'drugbank') { json ->
            def props = [:]
            props['DrugBankID'] = json.drugbank_id
            props['Brands'] = json.brands
            props['Generic name'] = json.generic_name
                    
            return props
        }
        println "  merge complete"
    }
    
    execute("Getting data") {
        def data = server.fetchData(drugbankDataset, filterInfo.id)
        println "  fetched ${data.size()} rows"
    }
    
    println "------------ ChEMBL ---------------"
    
     // This one a slightly more sensible query
    SubsetInfo listInfo2 = execute("Generating list for CHEMBL1613886") {
        SubsetInfo li = server.queryForStructuresForPropertyDef('tim', 'test 2', 'CHEMBL1613886')
        println "  generated results for list $li.id"
        return li
    }
  
    String chemblDataset = 'CHEMBL1613886'
    execute("Restoring dataset $listInfo2.id as $chemblDataset") {
        server.restoreDataSet(chemblDataset, listInfo2.id)
        println "  dataset restored"
    }
    
    [
        CHEMBL1613886: 'Cyp3A4', 
        CHEMBL1613777: 'Cyp2C19', 
        CHEMBL1614110: 'Cyp2D6', 
        CHEMBL1614027: 'Cyp2C9'].each { chemblid, propname ->
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
            
    execute("Getting data") {
        def data = server.fetchData(chemblDataset, filterInfo.id)
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
    //println "  memory before: $m0 $f0 = ${m0 - f0}"
    long t0 = System.currentTimeMillis()
    def result = closure()
    long t1 = System.currentTimeMillis()
    rt.gc()
    long f1 = rt.freeMemory()
    long m1 = rt.totalMemory()
    println "  took ${t1-t0}ms"
    //println "  memory after: $m1 $f1 = ${m1 - f1}"
    println "  memory used: ${(m1-f1) - (m0-f0)}"
    return result
}  
