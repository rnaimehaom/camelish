package com.im.bioassay.doseresponse

import com.im.bioassay.camel.converters.DoseResponseResultTypeConverter
import spock.lang.Specification


class DoseResponseResultSpecification extends Specification {


    def "simple parsing"() {

        expect:
        def result = new DoseResponseResultTypeConverter().toDoseResponseResult(lines)

        DoseResponseUtils.validate(result) != valid

        where:
        lines << [
'''
x=1 10 100 1000 10000 100000
y=5 9 25 70 90 95
''',
'''
X=1 10 100 1000 10000 100000
Y=5 9 25 70 90 95
'''  // case shouldn't matter
,'''
x=1 10 100 1000 10000 100000
y=5 9 25 70 90 95
y=5 9 25 70 90 96
'''   // OK - multiple Y values OK, even encouraged!
,'''
x=1 10 100 1000 10000 100000
y=5 9 25 70 90
'''  // invalid - not enough Y values
,'''
x=1 10 100 1000 10000 100000
''' // invalid - no Y values
,'''
x=1 10 100 1000 10000 100000
y=5 9 25 70 90 95
m=some rubbish
''' // OK - strange lines ignored
,'''
x=1 10 100 1000 10000 100000
y=5 9 25 70 95
''' // fail - missing value
        ]

        valid = [true, true, true, false, false, true, false]

    }
}
