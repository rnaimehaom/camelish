package com.im.examples.model

import groovy.json.JsonOutput


def structures = []
(0..4).each {
    def data = ['id': it, 'structure': 'c1ccccc1']
    structures << data
    def batches = []
    data['batches'] = batches
    
    batches << ['batchId': 'B0001', 'props': ['propID1': [['propVal1': 1]]]]

}

String json = JsonOutput.toJson(structures)
println "json: $json"
//println "json: ${JsonOutput.prettyPrint(json)}"