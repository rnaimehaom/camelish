/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.chemaxon.camel.db;

import chemaxon.enumeration.supergraph.SupergraphException;
import chemaxon.formats.MolExporter;
import chemaxon.jchem.db.DatabaseSearchException;
import chemaxon.jchem.db.JChemSearch;
import chemaxon.sss.search.JChemSearchOptions;
import chemaxon.sss.search.SearchException;
import chemaxon.struc.Molecule;
import com.im.util.CollectionUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;

/**
 * Default implementation that assumes the body contains the query structure as
 * a String, and the results are set to the body as an array of CD_ID values
 *
 * @author timbo
 */
public class DefaultJChemSearcher extends AbstractJChemSearcher {

    private final static Logger LOG = Logger.getLogger(DefaultJChemSearcher.class.getName());

    public static final String HEADER_SEARCH_OPTIONS = "JChemSearchOptions";

    /**
     * The different types of output that can be generated.
     * <br>
     * RAW generates the raw int[] arrray returned by JChemSearch
     * <br>
     * CD_IDS generates an Iterable<Integer> containing the CD_ID values
     * <br>
     * MAPS generates an Iterable<Map<String,Object>> of the values for the
     * columns specified by the outputColumns field
     * <br>
     * MOLECULES generates an Iterable<Molecule> with the values specified by
     * the outputColumns field. The keys are the column names, converted to
     * UPPER CASE, and the CD_STRUCTURE column MUST be specified in the column
     * list (if not you get empty molecules)
     * <br>
     * TEXT generates a String containing the structures (as if generated using
     * the MOLECULES options converted to a text in the format specified by the
     * outputFormat field. NOTE: this builds the entire String in memory so is
     * only suitable for small result sets.
     * <br>
     * STREAM generates an OutputStream and writes the structures as text to
     * that stream. This is similar in nature to the TEXT option but suitable
     * for large numbers of structures.
     *
     */
    public enum OutputMode {

        RAW,
        CD_IDS,
        MAPS,
        MOLECULES,
        TEXT,
        STREAM
    }

    /**
     * The type of output that is generated. One of the values of the OutputMode
     * enum. Default is RAW as this is the cheapest in terms of processing time,
     * but leaves you with the most work to do.
     *
     */
    private OutputMode outputMode = OutputMode.RAW;

    public OutputMode getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(OutputMode outputMode) {
        this.outputMode = outputMode;
    }

    /**
     * The list of columns to retrieve
     *
     */
    private List<String> outputColumns = Collections.EMPTY_LIST;

    public List<String> getOutputColumns() {
        return outputColumns;
    }

    public void setOutputColumns(List<String> outputColumns) {
        this.outputColumns = outputColumns;
    }

    /**
     * Specifies the file format when using TEXT as the output mode. Default is
     * "sdf"
     *
     */
    private String outputFormat = "sdf";

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

//    /**
//     * The interval at which the JChemSearch instance is polled for results
//     *
//     */
//    private int pollTime = 100;
//
//    public int getPollTime() {
//        return pollTime;
//    }
//
//    public void setPollTime(int pollTime) {
//        this.pollTime = pollTime;
//    
    /**
     * Sets the search options based on what is specified in the
     * HEADER_SEARCH_OPTIONS if present, or the defaults provided by the
     * searchOptions if not.
     *
     * @param exchange
     * @param jcs
     */
    @Override
    protected void handleSearchParams(Exchange exchange, JChemSearch jcs) {

        String headerOpts = exchange.getIn().getHeader(HEADER_SEARCH_OPTIONS, String.class);
        if (headerOpts != null) {
            LOG.log(Level.INFO, "Using search options from header: {0}", headerOpts);
            JChemSearchOptions opts = new JChemSearchOptions(JChemSearch.SUBSTRUCTURE);
            opts.setOptions(headerOpts);
            jcs.setSearchOptions(opts);
        } else {
            super.handleSearchParams(exchange, jcs);
        }
    }

    @Override
    protected void startSearch(JChemSearch jcs) throws Exception {

        switch (outputMode) {
            case STREAM:
                jcs.setOrder(JChemSearch.NO_ORDERING);
                jcs.setRunMode(JChemSearch.RUN_MODE_ASYNCH_PROGRESSIVE);
                jcs.setRunning(true);
                break;
            default:
                super.startSearch(jcs);
        }
    }

    @Override
    protected void handleSearchResults(Exchange exchange, JChemSearch jcs) throws Exception {
        // TOOD - work out how to best stream the results - various complications here, 
        // such as similarity search not supporting asynch mode 
        switch (outputMode) {
            case RAW:
                int[] hits = jcs.getResults();
                exchange.getOut().setBody(hits);
                break;
            case CD_IDS:
                exchange.getOut().setBody(getHitsAsList(jcs));
                break;
            case MOLECULES:
                handleAsMolecules(exchange, jcs);
                break;
            case TEXT:
                handleAsText(exchange, jcs);
                break;
            case STREAM:
                handleAsStream(exchange, jcs);
                break;
            default:
                throw new UnsupportedOperationException("Mode " + outputMode + " not yet supported");
        }
    }

    private List<Integer> getHitsAsList(JChemSearch jcs) {
        return CollectionUtils.asIntegerList(jcs.getResults());
    }

    /**
     * Create an Iterable of Molecules
     *
     * @param exchange
     * @param jcs
     * @throws SQLException
     * @throws IOException
     * @throws SearchException
     * @throws SupergraphException
     * @throws PropertyNotSetException
     * @throws DatabaseSearchException
     */
    void handleAsMolecules(Exchange exchange, JChemSearch jcs)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {
        Molecule[] mols = jcs.getHitsAsMolecules(jcs.getResults(), null, outputColumns, null);
        exchange.getOut().setBody(Arrays.asList(mols));
    }

    /**
     * Create the molecules as text in the format specified by the outputFormat
     * property. Note: this is only suitable for relatively small numbers of
     * molecules. Use handleAsStream for large sets.
     *
     * @param exchange
     * @param jcs
     * @throws SQLException
     * @throws IOException
     * @throws SearchException
     * @throws SupergraphException
     * @throws PropertyNotSetException
     * @throws DatabaseSearchException
     */
    private void handleAsText(final Exchange exchange, final JChemSearch jcs)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {
        final Molecule[] mols = jcs.getHitsAsMolecules(jcs.getResults(), null, outputColumns, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final MolExporter exporter = new MolExporter(out, outputFormat);
        try {
            writeMoleculesToMolExporter(exporter, mols);
            exchange.getOut().setBody(out.toString());
        } finally {
            exporter.close();
        }
    }

    private void handleAsStream(final Exchange exchange, final JChemSearch jcs)
            throws SQLException, IOException, SearchException, SupergraphException, DatabaseSearchException {

        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(pis);
        final MolExporter exporter = new MolExporter(out, outputFormat);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (jcs.getRunMode() == JChemSearch.RUN_MODE_ASYNCH_PROGRESSIVE) {
                                // async mode - this will be the norm
                                while (jcs.hasMoreHits()) {
                                    int[] hits = jcs.getAvailableNewHits(1);
                                    LOG.log(Level.FINER, "Processing {0} hits", hits.length);
                                    Molecule[] mols = jcs.getHitsAsMolecules(hits, null, outputColumns, null);
                                    writeMoleculesToMolExporter(exporter, mols);
                                }
                            } else {
                                // just in case we also handle sync mode
                                Molecule[] mols = jcs.getHitsAsMolecules(jcs.getResults(), null, outputColumns, null);
                                LOG.log(Level.FINER, "Processing {0} hits", mols.length);
                                writeMoleculesToMolExporter(exporter, mols);
                            }
                        } catch (InterruptedException | DatabaseSearchException | SQLException | IOException | SearchException | SupergraphException e) {
                            LOG.log(Level.SEVERE, "Error writing molecules", e);
                        } finally {

                            try {
                                exporter.close();
                            } catch (IOException ex) {
                                LOG.log(Level.SEVERE, "Error closing MolExporter", ex);
                            }
                        }
                    }

                }).start();

        exchange.getOut().setBody(pis);
    }

    private void writeMoleculesToMolExporter(final MolExporter exporter, final Molecule[] mols) throws IOException {
        for (Molecule mol : mols) {
            exporter.write(mol);
        }
    }
}
