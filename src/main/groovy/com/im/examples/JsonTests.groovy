package com.im.examples

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

Expando bean = new Expando()
bean.setProperty('structure', 'c1ccccc1')
bean.setProperty('name', 'benzene')
bean.setProperty('atomCount', 12)



println "bean: $bean"

String json = JsonOutput.toJson(bean)
println "json: $json"

 def slurper = new JsonSlurper()
 def result = slurper.parseText(json)
 
println "result: [${result.getClass().name}] $result"

json = JsonOutput.toJson(result)
println "json: $json"
