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
        configureJChemSearch(j);
        return j;
    }

    protected void configureJChemSearch(JChemSearch jcs) {
        // noop
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        handleSearchParams(exchange, jcs);
        handleQueryStructure(exchange, jcs);
        String opts = jcs.getSearchOptions().toString();
        LOG.log(Level.INFO, "Executing search using options: {0}", opts);
        startSearch(jcs);
        handleSearchResults(exchange, jcs);
    }

    protected void startSearch(JChemSearch jcs) throws Exception {
        jcs.setRunMode(JChemSearch.RUN_MODE_SYNCH_COMPLETE);
        jcs.setRunning(true);
        LOG.info("Search complete");
    }

    protected void handleQueryStructure(Exchange exchange, JChemSearch jcs) {
        Object body = exchange.getIn().getBody();
        if (body == null) {
            throw new IllegalArgumentException("Query structure must be specified as body");
        }
        if (body instanceof chemaxon.struc.Molecule) {
            jcs.setQueryStructure((chemaxon.struc.Molecule) body);
        } else {
            String query;
            if (body instanceof String) {
                query = (String) body;
            } else {
                query = exchange.getContext().getTypeConverter().convertTo(String.class, body);
            }
            jcs.setQueryStructure(query);
        }
    }

    protected void handleSearchParams(Exchange exchange, JChemSearch jcs) {
        JChemSearchOptions opts = new JChemSearchOptions(SearchConstants.SUBSTRUCTURE);
        if (searchOptions != null) {
            LOG.log(Level.INFO, "Setting default search options to {0}", searchOptions);
            opts.setOptions(searchOptions);
        }
        jcs.setSearchOptions(opts);
    }

    protected abstract void handleSearchResults(Exchange exchange, JChemSearch jcs) throws Exception;
}
