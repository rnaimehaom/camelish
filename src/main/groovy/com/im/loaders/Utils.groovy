package com.im.loaders

import groovy.sql.Sql
import java.sql.SQLException
import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource

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
    
    static DataSource createDataSource(ConfigObject database, String user, String password) {
        PGSimpleDataSource ds = new PGSimpleDataSource()
        ds.serverName = database.server
        ds.portNumber = database.port
        ds.databaseName = database.database
        ds.user = user
        ds.password = password
        
        return ds
    }
    
    static int createSourceDefinition(DataSource dataSource, String schema, int categoryId, String name, String desc, String type, String owner, String maintainer, boolean active) {
   
        Sql db = new Sql(dataSource)
        int id = 0 
        try {
            def rows = db.executeUpdate("""DELETE FROM ${Sql.expand(schema)}.sources WHERE
                category_id = $categoryId and source_name = $name""")
             if (rows) {
                 println "Deleted old $name"
             }
            
            def keys = db.executeInsert("""INSERT INTO ${Sql.expand(schema)}.sources
(category_id, source_name, source_description, type, owner, maintainer, active) 
values ($categoryId, $name, $desc, $type, $owner, $maintainer, $active)""")
            id = keys[0][0]
            println "Source ID is $id"
        
        } finally {
            db.close()
        }
        return id        
    }
    
     static int createPropertyDefinition(
         DataSource dataSource, 
         String schema, 
         int sourceId, 
         String name, 
         String desc, 
         String originalId, 
         String definition, 
         String example) {
        
        Sql db = new Sql(dataSource)
        int id = 0 
        try {
            
            def keys = db.executeInsert("""INSERT INTO ${Sql.expand(schema)}.property_definitions
(source_id, property_name, property_description, original_id, definition, example) 
values ($sourceId, $name, $desc, $originalId, $definition, $example)""")
            id = keys[0][0]
            println "Property ID is $id"
        
        } finally {
            db.close()
        }
        return id        
    }
	
}

