package com.im.examples.model

import com.im.loaders.Utils
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
abstract class Searcher {
    
    ConfigObject database
    DataSource dataSource
    
    Searcher() {
        database = Utils.createConfig('loaders/database.properties')
    }
    
    void createDataSource(String username, String password) {
        dataSource = Utils.createDataSource(database, username, password)
    }
	
}

