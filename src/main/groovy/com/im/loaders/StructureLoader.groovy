package com.im.loaders

import chemaxon.util.ConnectionHandler
import java.sql.Connection

import chemaxon.jchem.db.UpdateHandler

/**
 *
 * @author timbo
 */
class StructureLoader {
    
    ConnectionHandler conh
    UpdateHandler uh
    String structureTable
    Connection connection
	
    StructureLoader(Connection connection, String structureTable) {
        this.structureTable = structureTable
        this.connection = connection
        init()
    }
 
    private void init() {
    
        conh = new ConnectionHandler();
        conh.setConnection(connection);
        uh = new UpdateHandler(conh, UpdateHandler.INSERT, structureTable, null);
        uh.duplicateFiltering = UpdateHandler.DUPLICATE_FILTERING_ON
        
    }
    
    void close() {
        uh?.close()
    }
    
    int execute(def mol) {
        uh.structure = mol
        uh.execute(true)
    }

}