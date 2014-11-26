package com.im.examples

import org.apache.camel.CamelContext
import org.apache.camel.component.http4.*
import org.apache.camel.util.jsse.*

/**
 *
 * @author timbo
 */
class Utils {
    
    
    
    static void applySslContextToHttpComponent(CamelContext camelContext) {
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource("data/cert/keystore.jks");
        ksp.setPassword("secret");
 
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyStore(ksp);
        kmp.setKeyPassword("secret");
 
        SSLContextParameters scp = new SSLContextParameters();
        scp.setKeyManagers(kmp);
 
        HttpComponent httpComponent = camelContext.getComponent("https4", HttpComponent.class);
        httpComponent.setSslContextParameters(scp);
    }
	
}

