package com.im.bioassay.doseresponse

import com.im.bioassay.curvefit.FourPLModel
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
x=1 10 100 1000 10000 100000
y=5 9 25 70 90 95
''')
        converted instanceof DoseResponseResult
        converted.getXValues().size() == 6
        converted.getYValues().size() == 1
        converted.getYValues().getAt(0).size() == 6
        converted.id == 'test1'
    }

    def "Convert multiple spaces"() {

        expect:
        DoseResponseResult converted = DoseResponseUtils.toDoseResponseResult(
                '''
id = test2
x=1 10  100 1000 10000      100000
y=5 9 25  70 90   95
''')
        converted instanceof DoseResponseResult
        converted.getXValues().size() == 6
        converted.getYValues().size() == 1
        converted.getYValues().getAt(0).size() == 6
        converted.id == 'test2'
    }

    def "Convert tabs"() {

        expect:
        DoseResponseResult converted = DoseResponseUtils.toDoseResponseResult(
                '''
id = test3
x=1 10  100 1000 10000      100000
y=5 9   25    70    90  95
''')
        converted instanceof DoseResponseResult
        converted.getXValues().size() == 6
        converted.getYValues().size() == 1
        converted.getYValues().getAt(0).size() == 6
        converted.id == 'test3'
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
x=1 10 100 1000 10000 100000
y=5 9 25 70 90 95
''' , '''
x=1 10 100 1000 10000 100000
y=5 9 25 70 90 95
#END
x=1 10 100 1000 10000 100000
y=5 9 25 70 90 95
''' , '''
x=1 10 100 1000 10000 100000
y=5 9 25 70 90 95
#END
x=1 10 100 1000 10000 100000
y=5 9 25 70 90 95
#END
'''
        ]

        counts << [1, 2, 2]
    }

    def "Test writing to String"() {
        when:
        DoseResponseResult result = DataUtils.createSingle()
        def FourPLModel ic50 = new FourPLModel(1.1d, 99.9d, 0.99d, '>', 55.55d)
        result.setFitModel(ic50)

        String s = DoseResponseUtils.fromDoseResponseResult(result)
        println s

        then:
        s != null
    }

}
