package com.im.examples.model.types

import spock.lang.Shared
import spock.lang.Specification
import static com.im.examples.model.types.Qualifier.*

class UnitsSpecification extends Specification {
    
   def "test convert response"() {
        expect:

        p == Units.NumericResponse.PERCENT.convertTo(Units.NumericResponse.FRACTION, f)
        
        where:
        f << [0.234, 0.53]
        p << [23.4, 53.0]
    }
    
    def "test convert molarity"() {
        expect:

        mm == Units.Molarity.MOLAR.convertTo(Units.Molarity.MILLI_MOLAR, m)
        m == Units.Molarity.MILLI_MOLAR.convertTo(Units.Molarity.MOLAR, mm)
        
        where:
        m << [1.234, 5.3, 0.0000563]
        mm << [1234, 5300, 0.0563]
        um << [1234000, 5300000, 56.3]
    }
    
    def "parse molarity values"() {

        expect:
        Units.Molarity.parse(values).equals(results)
        
        
        where:

        values << [
            'M',
            'mM',
            'µM',
            'uM',
            'nM',
            'pM',
            'fM'
        ]

        results << [
            Units.Molarity.MOLAR,
            Units.Molarity.MILLI_MOLAR,
            Units.Molarity.MICRO_MOLAR,
            Units.Molarity.MICRO_MOLAR,
            Units.Molarity.NANO_MOLAR,
            Units.Molarity.PICO_MOLAR,
            Units.Molarity.FEMTO_MOLAR
        ]
    }

}
