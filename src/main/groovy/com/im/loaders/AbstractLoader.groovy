/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.loaders

import chemaxon.jchem.db.StructureTableOptions
import chemaxon.util.ConnectionHandler
import java.sql.*
import javax.sql.DataSource
import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext

import chemaxon.jchem.db.*
import chemaxon.util.ConnectionHandler
import groovy.sql.Sql

/**
 *
 * @author timbo
 */
class AbstractLoader {
    
    ConfigObject database;
    ConfigObject props;
  
    DataSource dataSource
    String tableName
    
    AbstractLoader(URL config) {
        
        database = createConfig('loaders/database.properties')
        props = createConfig(config)
        validate()
        dataSource = createDataSource()
        tableName = props.schema + '.' + props.table
    }
    
    static ConfigObject createConfig(String path) {
        return createConfig(new File(path).toURL())
    }
    
    static ConfigObject createConfig(URL url) {
        return new ConfigSlurper().parse(url)
    }
    
    void validate() {
        assert props.schema != null
        assert props.table != null
    }
    
    void run(String[] args) {
        
        println "Validating ..."


        args.each {
            doAction(it, props)
        }
    }
    
    boolean doAction(String action, def props) {
        if (action == 'createTables') {
            createTables(props)
            return true
        } else if (action == 'dropTables') {
            dropTables(props)
            return true
        } else if (action == 'loadData') {
            loadData(props)
            return true
        }
        return false
    }

    ConnectionHandler createConnectionHandler() {
        return new ConnectionHandler(dataSource.getConnection(), props.schema + '.jchemproperties')
    }
    
    
    void dropTables(def props) {
        println "Dropping tables"
        ConnectionHandler conh = createConnectionHandler()
        if (UpdateHandler.isStructureTable(conh, tableName)) {
            UpdateHandler.dropStructureTable(conh, tableName)
        }
    }
    
    void createTables(def props) {
        println "Creating tables"
        String szr = null
        if (props.standardizer) {
            szr = new File(props.standardizer).text
        }
        ConnectionHandler conh = createConnectionHandler()
        if (!DatabaseProperties.propertyTableExists(conh)) {
            DatabaseProperties.createPropertyTable(conh)    
        }

        StructureTableOptions opts = new StructureTableOptions(tableName, props.tableType)
        opts.extraColumnDefinitions = props.extraColumnDefs.join(',')
        opts.standardizerConfig = szr
        UpdateHandler.createStructureTable(conh, opts)
    }
    
        
    void loadData(def props) {
        
        CamelContext context = createCamelContext()
        createRoutes(context)
        context.start()

        executeRoutes(context)

        context.stop()
    }
    
    CamelContext createCamelContext() {
        return new DefaultCamelContext()
    }
    
    List<String> getColumnNamesFromColumnDefs(List defs) {
        def result = []
        defs.each {
            String col = it.trim()
            int pos = col.indexOf(' ')
            result << col.substring(0, pos)
        }
        return result
    }
	

    void executeMayFail(Sql db, String desc, String sql) {
        println desc
        try {
            db.execute(sql)
        } catch (SQLException ex) {
            println "Execution failed: ${ex.message}"
        }
    }

    void execute(Sql db, String desc, String sql) {
        println desc
        db.execute(sql)
    }

}