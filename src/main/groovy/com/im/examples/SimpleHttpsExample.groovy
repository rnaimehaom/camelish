package com.im.examples

import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.component.http4.*
import org.apache.camel.util.jsse.*


CamelContext camelContext = new DefaultCamelContext()
//Utils.applySslContextToHttpComponent(camelContext)

//KeyStoreParameters ksp = new KeyStoreParameters();
//ksp.setResource("data/cert/keystore.jks");
//ksp.setPassword("secret");
// 
//KeyManagersParameters kmp = new KeyManagersParameters();
//kmp.setKeyStore(ksp);
//kmp.setKeyPassword("secret");
// 
//SSLContextParameters scp = new SSLContextParameters();
//scp.setKeyManagers(kmp);
// 
//HttpComponent httpComponent = camelContext.getComponent("https4", HttpComponent.class);
//httpComponent.setSslContextParameters(scp);


camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
            
            from('direct:simple')
            //.to('https4:www.google.co.uk/search?q=foo')
            //.to('https4:www.ebi.ac.uk/chemblws/compounds/CHEMBL1.json')
            .to('https4:www.ebi.ac.uk/chemblws/compounds/substructure/CN(CCCN)c1cccc2ccccc12')
            .log('got ${body}')
            
        }
    })

camelContext.start()



ProducerTemplate t = camelContext.createProducerTemplate()
t.sendBody('direct:simple', null)
//t.sendBody('direct:simple', 'Cc1cc(ccc1C(=O)c2ccccc2Cl)N3N=CC(=O)NC3=O')
println "finished"

camelContext.stop()