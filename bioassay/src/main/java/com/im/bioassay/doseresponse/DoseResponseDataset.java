package com.im.bioassay.doseresponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by timbo on 19/04/2014.
 */
public class DoseResponseDataset {

    List<DoseResponseResult> results = new ArrayList<DoseResponseResult>();

    public List<DoseResponseResult> getResults() {
        return results;
    }

    public void setResults(List<DoseResponseResult> results) {
        this.results = results;
    }


}
