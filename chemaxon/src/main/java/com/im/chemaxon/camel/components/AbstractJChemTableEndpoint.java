/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.chemaxon.camel.components;

import chemaxon.jchem.db.TableTypeConstants;
import chemaxon.util.ConnectionHandler;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.impl.DefaultEndpoint;

/**
 *
 * @author timbo
 */
public abstract class AbstractJChemTableEndpoint extends DefaultEndpoint {

    protected DataSource ds;
    protected ConnectionHandler conh;

    private String dataSourceRef;
    private String structureTableName;
    private String propertyTableName = ConnectionHandler.DEFAULT_PROPERTY_TABLE;
    private int structureTableType = TableTypeConstants.TABLE_TYPE_DEFAULT;
    protected String extraColumns;

    public AbstractJChemTableEndpoint(String uri, DefaultComponent component) {
        super(uri, component);
    }

    public String getDataSourceRef() {
        return dataSourceRef;
    }

    public void setDataSourceRef(String dataSourceRef) {
        this.dataSourceRef = dataSourceRef;
    }

    public String getStructureTableName() {
        return structureTableName;
    }

    public void setStructureTableName(String structureTableName) {
        this.structureTableName = structureTableName;
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        conh = null;
    }

    public int getStructureTableType() {
        return structureTableType;
    }

    public void setStructureTableType(int structureTableType) {
        this.structureTableType = structureTableType;
    }

    public String getPropertyTableName() {
        return propertyTableName;
    }

    public void setPropertyTableName(String propertyTableName) {
        this.propertyTableName = propertyTableName;
    }

    protected ConnectionHandler createConnectionHandler() {
        conh = new ConnectionHandler();
        conh.setPropertyTable(getPropertyTableName());
        return conh;
    }

    protected void prepareConnectionHandler() throws SQLException {
        // get the "correct" connection so that we handle transactions correctly
        conh.setConnection(ds.getConnection());
    }

    public String getExtraColumns() {
        return extraColumns;
    }

    public void setExtraColumns(String extraColumns) {
        this.extraColumns = extraColumns;
    }

}
