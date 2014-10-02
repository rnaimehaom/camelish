/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.chemaxon.camel.components;

import chemaxon.jchem.db.DatabaseProperties;
import chemaxon.jchem.db.StructureTableOptions;
import chemaxon.jchem.db.UpdateHandler;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;

/**
 *
 * @author timbo
 */
@UriEndpoint(scheme = "jchembase")
public class JChemBaseEndpoint extends AbstractJChemTableEndpoint {

    private static Logger LOG = Logger.getLogger(JChemBaseEndpoint.class.getName());

    /**
     * @return the createTable
     */
    public CreateTable getCreateTable() {
        return createTable;
    }

    /**
     * @param createTable the createTable to set
     */
    public void setCreateTable(CreateTable createTable) {
        this.createTable = createTable;
    }


    public enum Mode {

        insert
    }

    private Mode mode;

    public enum CreateTable {

        always, never, ifAbsent
    }

    private CreateTable createTable = CreateTable.never;

    protected final JChemBaseComponent component;
    protected UpdateHandler inserter, updater;

    public JChemBaseEndpoint(String uri, JChemBaseComponent component) {
        super(uri, component);
        this.component = component;
    }

    @Override
    public Producer createProducer() throws Exception {
        validate();
        return new JChemBaseProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("from: not supported - You can't read messages from this endpoint");
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * @return the mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    void validate() throws Exception {
        if (mode == null) {
            throw new IllegalStateException("Mode must be specified");
        } else if (mode == null) {
            throw new IllegalStateException("Mode: " + mode + " not supported");
        }

    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        ds = component.getCamelContext().getRegistry().lookupByNameAndType(getDataSourceRef(), DataSource.class);
        conh = createConnectionHandler();

        conh.setConnection(ds.getConnection());
        if (createTable != CreateTable.never) {
            createStructureTable();
        }
        if (mode == Mode.insert) {
            inserter = new UpdateHandler(conh, UpdateHandler.INSERT, getStructureTableName(), "");
        }
        conh.setConnection(null);
    }

    private void createStructureTable() throws SQLException {

        // first create the property table if does't exist
        if (!DatabaseProperties.propertyTableExists(conh)) {
            DatabaseProperties.createPropertyTable(conh);
            LOG.info("Property table created");
        }

        boolean dropStructureTable = false;
        boolean createStructureTable = false;

        DatabaseProperties dbp;
        switch (createTable) {
            case always:
                createStructureTable = true;

                dbp = new DatabaseProperties(conh, false);
                dropStructureTable = dbp.getStructureTableNames().contains(getStructureTableName()); // TODO - handle case sensitivity?
                break;
            case ifAbsent:
                dbp = new DatabaseProperties(conh, false);
                createStructureTable = !dbp.getStructureTableNames().contains(getStructureTableName()); // TODO - handle case sensitivity?
                break;
        }

        if (dropStructureTable) {
            UpdateHandler.dropStructureTable(conh, getStructureTableName());
            LOG.log(Level.INFO, "Structure table {0} dropped", getStructureTableName());
        }

        if (createStructureTable) {
            StructureTableOptions opts = new StructureTableOptions(getStructureTableName(), getStructureTableType());
            opts.setExtraColumnDefinitions(extraColumns);
            UpdateHandler.createStructureTable(conh, opts);
            LOG.log(Level.INFO, "Structure table {0} created", getStructureTableName());
        }
    }

    @Override
    protected void doStop() throws Exception {

        if (inserter != null) {
            inserter.close();
            inserter = null;
        }
        if (updater != null) {
            updater.close();
            updater = null;
        }

        super.doStop();

    }

}
