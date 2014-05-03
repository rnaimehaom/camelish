package com.im.chemaxon.io;

import chemaxon.formats.MFileFormatUtil;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.marvin.io.MRecordReader;
import chemaxon.marvin.io.MRecordParseException;
import chemaxon.struc.MProp;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 14/04/2014.
 */
public class MoleculeIOUtils {

    public static final String STRUCTURE_FIELD_NAME = "MOLECULE_AS_STRING";

    static Logger log = Logger.getLogger(MoleculeIOUtils.class.getName());

    /** Creates an Iterator of MRecords from the InputStream
     * Designed for splitting a file or stream into individual records without generating a molecule instance.
     * Can be used as a Camel splitter.
     * <code>split().method(MoleculeIOUtils.class, "mrecordIterator")</code>
     *
     * @param is The input molecules in any format that Marvin recognises
     * @return Iterator or records
     * @throws IOException
     */
    public Iterator<MRecord> mrecordIterator(final InputStream is) throws IOException {

        log.log(Level.FINE, "Creating Iterator<MRecord> for %s", is.toString());

        final MRecordReader recordReader = MFileFormatUtil.createRecordReader(is, null, null, null);

        return new Iterator<MRecord>() {

            private MRecord nextRecord;
            int count = 0;

            /** Public access in case direct access is needed during operation.
             * Use with care.
             *
             * @return The instance doing the parsing
             */
            public MRecordReader getRecordReader() {
                return recordReader;
            }

            public boolean hasNext() {
                try {
                    return read();
                } catch (Exception e) {
                    throw new RuntimeException("Error reading record " + count, e);
                }
            }

            public boolean read() throws IOException, MRecordParseException {
                log.finer("Reading next ...");
                count++;
                MRecord rec = recordReader.nextRecord();

                if (rec != null) {
                    nextRecord = rec;
                    return true;
                } else {
                    log.fine("Stream seems completed");
                    nextRecord = null;
                    close(recordReader);
                    return false;
                }
            }

            public MRecord next() {
                if (nextRecord == null) {

                    boolean success;
                    try {
                        success = read();
                    } catch (Exception e) {
                        throw new RuntimeException("Error reading record " + count, e);
                    }

                    if (!success) {
                        close(recordReader);
                        throw new NoSuchElementException("No more records");
                    }
                }
                return nextRecord;
            }

            public void remove() {
                throw new UnsupportedOperationException("Remove not supported");
            }

            @Override
            public void finalize() {
                // ensure always closed. Whole file may not be read.
                if (recordReader != null) {
                    log.info("Reader not closed. Doing this in finalize() instead.");
                    close(recordReader);
                }
            }

            private void close(MRecordReader reader) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ioe) {
                        throw new RuntimeException("IOException closing MRecordReader", ioe);
                    }
                }
                reader = null;
            }

        };
    }

    public static Map<String, String> mrecordToMap(MRecord record) {
        Map<String, String> vals = new HashMap<String, String>();
        vals.put(STRUCTURE_FIELD_NAME, record.getString());
        String[] fields = record.getPropertyContainer().getKeys();
        List<MProp> values = record.getPropertyContainer().getPropList();
        for (int x = 0; x < fields.length; x++) {
            vals.put(fields[x], MPropHandler.convertToString(values.get(x), null));
        }
        return vals;
    }

}
