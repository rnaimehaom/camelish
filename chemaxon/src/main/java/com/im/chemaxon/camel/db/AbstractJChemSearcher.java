package com.im.chemaxon.camel.db;

import chemaxon.jchem.db.JChemSearch;
import chemaxon.sss.SearchConstants;
import chemaxon.sss.search.JChemSearchOptions;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 26/04/2014.
 */
public abstract class AbstractJChemSearcher extends ConnectionHandlerService implements Processor {

    private static final Logger LOG = Logger.getLogger(AbstractJChemSearcher.class.getName());

    private JChemSearch jcs;
    private String structureTable;
    private String searchOptions;

    public AbstractJChemSearcher() {
    }

    public AbstractJChemSearcher(String structureTable, String opts) {
        this.structureTable = structureTable;
        this.searchOptions = opts;
    }

    public String getStructureTable() {
        return structureTable;
    }

    public void setStructureTable(String structureTable) {
        this.structureTable = structureTable;
    }

    public String getSearchOptions() {
        return searchOptions;
    }

    public void setSearchOptions(String searchOptions) {
        this.searchOptions = searchOptions;
    }

    @Override
    protected void doStart() throws Exception {
        LOG.log(Level.FINE, "Starting JChemSearcher {0}", this.toString());
        super.doStart();
        jcs = createJChemSearch();
    }

    private JChemSearch createJChemSearch() throws SQLException {
        LOG.log(Level.FINE, "Creating JChemSearch for table %s", structureTable);
        JChemSearch j = new JChemSearch();
        j.setStructureTable(structureTable);
        j.setConnectionHandler(getConnectionHandler());
        JChemSearchOptions opts = new JChemSearchOptions(SearchConstants.SUBSTRUCTURE);
        opts.setOptions(searchOptions);
        j.setSearchOptions(opts);
        configureJChemSearch(j);
        return j;
    }

    protected void configureJChemSearch(JChemSearch jcs) {
        // noop
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        handleSearchParams(exchange, jcs);
        jcs.setRunning(true);
        handleSearchResults(exchange, jcs);
    }

    protected abstract void handleSearchParams(Exchange exchange, JChemSearch jcs);

    protected abstract void handleSearchResults(Exchange exchange, JChemSearch jcs) throws Exception;
}