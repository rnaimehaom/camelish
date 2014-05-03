package com.im.bioassay.doseresponse

/**
 * Created by timbo on 20/04/2014.
 */
class DataUtils {

    static DoseResponseResult createSingle() {
        DoseResponseResult result = new DoseResponseResult()
        result.id = 'test1'
        result.XValues = [
                new Double(1),
                new Double(10),
                new Double(100),
                new Double(1000),
                new Double(10000),
                new Double(100000)
        ]
        result.YValues = [
                [
                        new Double(5),
                        new Double(9),
                        new Double(25),
                        new Double(70),
                        new Double(90),
                        new Double(95)
                ]
        ]
        return result
    }
}
