/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.loaders

import chemaxon.jchem.db.StructureTableOptions
import chemaxon.util.ConnectionHandler
import java.sql.*
import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext

import chemaxon.jchem.db.*
import chemaxon.util.ConnectionHandler

/**
 *
 * @author timbo
 */
class AbstractLoader {
    
    ConfigObject props;
    
    
    AbstractLoader(String config) {
        props = new ConfigSlurper().parse(new File(config).text)
    }
    
    void validate(String[] args, def props) {
        assert props.db.url  != null
        assert props.db.table  != null
    }
    
    void run(String[] args) {
        
        validate(args, props)

        Connection con = createConnection(props)
        try {
            args.each {
                doAction(it, props, con)
            }
        } finally {
            con.close()
        }
    }
    
    boolean doAction(String action, def props, Connection con) {
        if (action == 'createTables') {
            createTables(props, con)
            return true
        } else if (action == 'dropTables') {
            dropTables(props, con)
            return true
        } else if (action == 'loadData') {
            loadData(props, con)
            return true
        }
        return false
    }
    
    Connection createConnection(def props) {
        return DriverManager.getConnection(props.db.url, props.db.user, props.db.password)
    }
    
    void dropTables(def props, Connection con) {
        println "Dropping tables"
        ConnectionHandler conh = new ConnectionHandler(con, 'JCHEMPROPERTIES')
        if (UpdateHandler.isStructureTable(conh, props.db.table)) {
            UpdateHandler.dropStructureTable(conh, props.db.table)
        }
    }
    
    void createTables(def props, Connection con) {
        println "Creating tables"
        String szr = null
        if (props.db.standardizer) {
            szr = new File(props.db.standardizer).text
        }
        ConnectionHandler conh = new ConnectionHandler(con, 'JCHEMPROPERTIES')
        if (!DatabaseProperties.propertyTableExists(conh)) {
            DatabaseProperties.createPropertyTable(conh)    
        }

        StructureTableOptions opts = new StructureTableOptions(props.db.table, props.db.tableType)
        opts.extraColumnDefinitions = props.db.extraColumnDefs.join(',')
        opts.standardizerConfig = szr
        UpdateHandler.createStructureTable(conh, opts)
    }
    
        
    void loadData(def props, Connection con) {
        
        CamelContext context = new DefaultCamelContext()
        createRoutes(context, con)
        context.start()

        executeRoutes(context, con)

        context.stop()
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
	
}

