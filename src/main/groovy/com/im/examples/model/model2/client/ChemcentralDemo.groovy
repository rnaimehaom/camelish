package com.im.examples.model.model2.client

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
    
    // This simulates generating an intial dataset based on a query
    SubsetInfo listInfo = execute("Generating list for query") {
        SubsetInfo listInfo = server.queryForStructuresAsList('tim', 'test 1', null)
        println "  generated results for list $listInfo.id"
        return listInfo
    }
            
    // this simulates restoring a persisted dataset and making it available for use
    String datasetName = 'tmpdata'
    execute("Restoring dataset $listInfo.id as $datasetName") {
        server.restoreDataSet(datasetName, listInfo.id)
        println "  dataset restored"
    }
    
    // run a simple filter
    SubsetInfo filterInfo = execute("Simple filtering") {
        /* This is stop-gap measure to provide filtering just by passing a closure 
         * that does the filtering.
         * TODO - Need to establish how this would best be implemented.
         */
        SubsetInfo filterInfo = server.filter(datasetName, 'tim') { dataset -> 
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
            
    execute("Merging properties") {
        server.mergeProperties(datasetName, 'DrugBank', 'drugbank') { json ->
            def props = [:]
            props['DrugBankID'] = json.drugbank_id
            props['Brands'] = json.brands
            props['Generic name'] = json.generic_name
                    
            return props
        }
        println "  merge complete"
    }
            
    execute("Getting data") {
        def data = server.fetchData(datasetName, filterInfo.id)
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
    long t0 = System.currentTimeMillis()
    def result = closure()
    long t1 = System.currentTimeMillis()
    println "  took ${t1-t0}ms"
    return result
}  
