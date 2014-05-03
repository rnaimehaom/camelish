package com.im.bioassay.camel.converters;

import com.im.bioassay.doseresponse.DoseResponseResult;
import com.im.bioassay.doseresponse.DoseResponseUtils;
import org.apache.camel.*;


/**
 * Created by timbo on 17/04/2014.
 */
@Converter
public class DoseResponseResultTypeConverter {

    @Converter
    public static DoseResponseResult toDoseResponseResult(String input) {
        return DoseResponseUtils.toDoseResponseResult(input);
    }


}
