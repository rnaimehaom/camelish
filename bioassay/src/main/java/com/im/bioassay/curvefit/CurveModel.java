/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.bioassay.curvefit;

/**
 *
 * @author timbo
 */
public abstract class CurveModel {
    
    private long time;
    private int iterations;
    protected String lastParamChanged;
    protected boolean lastParamIncreased;
    private Double sumSquares;

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @return the iterations
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @param iterations the iterations to set
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /**
     * @return the sumSquares
     */
    public Double getSumSquares() {
        return sumSquares;
    }
    
}
