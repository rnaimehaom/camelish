package com.im.examples.model.types

import spock.lang.Shared
import spock.lang.Specification
import static com.im.examples.model.types.QualifiedValue.Qualifier.*

class QualifiedValueSpecification extends Specification {
    
    def "test equality"() {
        when:
        def a = new QualifiedValue(123.4f)
        def b = new QualifiedValue(123.4f)
        def c = new QualifiedValue(111.11f)
        def d = new QualifiedValue(124.3f, LESS_THAN)
        def e = new QualifiedValue(123.4f, EQUALS)
        
        then:
        a.equals(a)
        a.equals(b) 
        !a.equals(c)
        !a.equals(d)
        a.equals(e)
    }
    
    def "test compare"() {
        when:
        def a = new QualifiedValue(123.4f)
        def b = new QualifiedValue(123.4f)
        def c = new QualifiedValue(111.11f)
        
        then:
        a.compareTo(b) == 0
        a.compareTo(c) > 0
        
    }
    
    def "test plus"() {
        when:
        def a = new QualifiedValue(123.4f)
        def b = new QualifiedValue(111.1f)
        def c = new QualifiedValue(111.1f, LESS_THAN)
        def d = new QualifiedValue(111.1f, GREATER_THAN)
        def ab = a + b
        def ac = a + c
        def cd = c + d
        
        then:
        ab.value == 234.5
        ab.qualifier == EQUALS
        ac.value == 234.5
        ac.qualifier == LESS_THAN
        cd.qualifier == AMBIGUOUS
        
    }
    
    def "test divide"() {
        when:
        def a = new QualifiedValue(444.0f)
        def a4 = a / 4
        def b = new QualifiedValue(444.0f, LESS_THAN)
        def b4 = b / 4
        
        then:
        a4.value == 111
        a4.qualifier == EQUALS
        b4.value == 111
        b4.qualifier == LESS_THAN
    }
    
    
    def "parse float values"() {

        expect:
        QualifiedValue.parse(values, Float.class).equals(results)
        
        where:

        values << [
            '370.1',
            '-2585.9',
            '<38.6',
            '=54',
            ' <38.6',
            ' < 38.6',
            ' <38.6    ',
            ' < -38.6',
            '<=38.6',
            '<0.00000123386'
        ]

        results << [
            new QualifiedValue(370.1f),
            new QualifiedValue(-2585.9f),
            new QualifiedValue(38.6f, LESS_THAN),
            new QualifiedValue(54f),
            new QualifiedValue(38.6f, LESS_THAN),
            new QualifiedValue(38.6f, LESS_THAN),
            new QualifiedValue(38.6f, LESS_THAN),
            new QualifiedValue(-38.6f, LESS_THAN),
            new QualifiedValue(38.6f, LESS_THAN_OR_EQUALS),
            new QualifiedValue(0.00000123386f, LESS_THAN)
        ]
    }
    
    def "parse integer values"() {

        expect:
        QualifiedValue.parse(values, Integer.class).equals(results)
        
        where:

        values << [
            '370',
            '-2585',
            '<38',
            '=54',
            ' <38',
            ' < 38',
            ' <38    ',
            ' < -38',
            '<=38'
        ]

        results << [
            new QualifiedValue(370),
            new QualifiedValue(-2585),
            new QualifiedValue(38, LESS_THAN),
            new QualifiedValue(54),
            new QualifiedValue(38, LESS_THAN),
            new QualifiedValue(38, LESS_THAN),
            new QualifiedValue(38, LESS_THAN),
            new QualifiedValue(-38, LESS_THAN),
            new QualifiedValue(38, LESS_THAN_OR_EQUALS)
        ]
    }
    
    def "parse double values"() {

        expect:
        QualifiedValue.parse(values, Double.class).equals(results)
        
        where:

        values << [
            '370.1',
            '-2585.9',
            '<38.6'
        ]

        results << [
            new QualifiedValue(370.1d),
            new QualifiedValue(-2585.9d),
            new QualifiedValue(38.6d, LESS_THAN)
        ]
    }

}
