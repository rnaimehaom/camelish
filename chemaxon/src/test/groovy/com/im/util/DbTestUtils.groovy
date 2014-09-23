/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.util

import org.apache.derby.jdbc.EmbeddedDataSource

/**
 *
 * @author timbo
 */
class DbTestUtils {
    
    static EmbeddedDataSource createDerbyDataSource(String name, boolean create) {
        EmbeddedDataSource ds = new EmbeddedDataSource() 
	ds.setDatabaseName(name)
        create && ds.setCreateDatabase("create")
        return ds
    }
	
}

