package com.im.loaders

import groovy.sql.Sql
import java.sql.SQLException

/**
 *
 * @author timbo
 */
class Utils {
    
    static ConfigObject createConfig(String path) {
        return createConfig(new File(path).toURL())
    }
    
    static ConfigObject createConfig(URL url) {
        return new ConfigSlurper().parse(url)
    }
    
    static void executeMayFail(Sql db, String desc, String sql) {
        println desc
        try {
            db.execute(sql)
        } catch (SQLException ex) {
            println "Execution failed: ${ex.message}"
        }
    }

    static void execute(Sql db, String desc, String sql) {
        println desc
        db.execute(sql)
    }
	
}

