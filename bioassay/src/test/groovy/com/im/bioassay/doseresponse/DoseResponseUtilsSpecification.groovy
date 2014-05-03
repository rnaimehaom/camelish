package com.im.bioassay.doseresponse

import spock.lang.Specification

/**
 * Created by timbo on 19/04/2014.
 */
class DoseResponseUtilsSpecification extends Specification {

    def "Simple convert"() {

        expect:
        DoseResponseResult converted = DoseResponseUtils.toDoseResponseResult(
                '''
id = test1
x=1, 10, 100, 1000, 10000, 100000
y=5, 9, 25, 70, 90, 95
''')
        converted instanceof DoseResponseResult
        converted.getXValues().size() == 6
        converted.getYValues().size() == 1
        converted.getYValues().getAt(0).size() == 6
        converted.id == 'test1'

    }


    def "Read multiple records"() {

        setup:
        DoseResponseUtils drutils = new DoseResponseUtils()

        when:
        StringReader reader = new StringReader(lines)
        Iterator<DoseResponseResult> iter = drutils.doseResponseIterator(new BufferedReader(reader))
        List items = iter.collect { it }

        then:
        items.size() == counts

        where:
        lines << [
                '''
x=1, 10, 100, 1000, 10000, 100000
y=5, 9, 25, 70, 90, 95
''' , '''
x=1, 10, 100, 1000, 10000, 100000
y=5, 9, 25, 70, 90, 95
#END
x=1, 10, 100, 1000, 10000, 100000
y=5, 9, 25, 70, 90, 95
''' , '''
x=1, 10, 100, 1000, 10000, 100000
y=5, 9, 25, 70, 90, 95
#END
x=1, 10, 100, 1000, 10000, 100000
y=5, 9, 25, 70, 90, 95
#END
'''
        ]

        counts << [1, 2, 2]
    }

    def "Test writing to String"() {
        when:
        DoseResponseResult result = DataUtils.createSingle()
        def IC50 ic50 = new IC50();
        ic50.setConc(55.55d);
        ic50.setHill(0.99d);
        ic50.setBottom(1.1d);
        ic50.setTop(99.9d);
        ic50.setIC50Modifier(">");
        ic50.setSumSquares(12.34d);
        result.setIC50(ic50);

        String s = DoseResponseUtils.fromDoseResponseResult(result)
        println s

        then:
        s != null


    }


}
