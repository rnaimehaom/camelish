/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/** Take an Iterable and breaks it into chunks that can be processed.
 * Override the process() method to handle each chunk.
 * Can be configure to use chunk size or a time delay or both. If these are set to
 * a positive value the a new chunk is fired for processing when either the maximum
 * size is achieved or the amount of time (im millis) has accrued since processing
 * the chunk commenced. If either of these parameters is set to zero then the parameter
 * is ignored.
 * By default the size is set to 25 and the time to 0.
 *
 * @author timbo
 */
public abstract class BatchHandler<T> {

    private static final Logger LOG = Logger.getLogger(BatchHandler.class.getName());

    /**
     * The batch size to use. Default value is 25
     *
     */
    private int size = 25;

    /**
     * The max time (in millis) to wait before sending. Default value is 0 (meaning time is
     * ignored).
     *
     */
    private int time = 0;

    public BatchHandler() {

    }

    public BatchHandler(int size, int time) {
        this.size = size;
        this.time = time;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void process(Iterable iterable) {
        List<T> tmp = new ArrayList<T>();
        int j = 0;
        Iterator<T> it = iterable.iterator();
        long start = System.currentTimeMillis();
        while (it.hasNext()) {
            tmp.add(handleItem(it.next()));
            j++;
            if ((size > 0 && j == size)
                    || (time > 0 && (System.currentTimeMillis() - start > time))) {
                handle(tmp.iterator());
                tmp = new ArrayList<T>();
                j = 0;
                start = System.currentTimeMillis();
            }
        }
        if (!tmp.isEmpty()) {
            LOG.info("Finished up firing");
            handle(tmp.iterator());
        }
    }
    
    /** Callback to allow each item to be processed before being added to the chunk.
     * Default does nothing.
     * 
     * @param item
     * @return 
     */
    protected T handleItem(T item) {
        return item;
    }

    /** Callback to handle the chunks of items.
     * Override this method to process the chunks
     * 
     * @param data 
     */
    protected abstract void handle(Iterator<T> data);

}
