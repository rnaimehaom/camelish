package com.im.chemaxon.camel.db;

import chemaxon.jchem.db.JChemSearch;
import chemaxon.sss.search.JChemSearchOptions;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 26/04/2014.
 */
public abstract class JChemSearcher extends ConnectionHandlerService implements Processor {

    private static final Logger log = Logger.getLogger(JChemSearcher.class.getName());

    private JChemSearch jcs;
    private String structureTable;
    JChemSearchOptions opts;

    public JChemSearcher(String structureTable, JChemSearchOptions opts) {
        this.structureTable = structureTable;
        this.opts = opts;
    }

    @Override
    protected void doStart() throws Exception {
        log.fine("Starting JChemSearcher " + this.toString());
        super.doStart();
        jcs = createJChemSearch();
    }

    private JChemSearch createJChemSearch() throws SQLException {
        log.log(Level.FINE, "Creating JChemSearch for table %s", structureTable);
        JChemSearch j = new JChemSearch();
        j.setStructureTable(structureTable);
        j.setConnectionHandler(getConnectionHandler());
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
        jcs.run();
        handleSearchResults(exchange, jcs);
    }

    protected abstract void handleSearchParams(Exchange exchange, JChemSearch jcs);

    protected abstract void handleSearchResults(Exchange exchange, JChemSearch jcs);
}
