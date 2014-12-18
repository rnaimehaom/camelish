package com.im.examples.model.types

import spock.lang.Shared
import spock.lang.Specification
import static com.im.examples.model.types.Qualifier.*

class QualifiedNumberSpecification extends Specification {
    
    def "test equality"() {
        when:
        def a = new QualifiedNumber(123.4f)
        def b = new QualifiedNumber(123.4f)
        def c = new QualifiedNumber(111.11f)
        def d = new QualifiedNumber(124.3f, LESS_THAN)
        def e = new QualifiedNumber(123.4f, EQUALS)
        
        then:
        a.equals(a)
        a.equals(b) 
        !a.equals(c)
        !a.equals(d)
        a.equals(e)
    }
    
    def "test compare"() {
        when:
        def a = new QualifiedNumber(123.4f)
        def b = new QualifiedNumber(123.4f)
        def c = new QualifiedNumber(111.11f)
        
        then:
        a.compareTo(b) == 0
        a.compareTo(c) > 0
        
    }
    
    def "test plus"() {
        when:
        def a = new QualifiedNumber(123.4f)
        def b = new QualifiedNumber(111.1f)
        def c = new QualifiedNumber(111.1f, LESS_THAN)
        def d = new QualifiedNumber(111.1f, GREATER_THAN)
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
        def a = new QualifiedNumber(444.0)
        def a4 = a / 4
        def b = new QualifiedNumber(444.0, LESS_THAN)
        def b4 = b / 4
        
        then:
        a4.value == 111
        a4.qualifier == EQUALS
        b4.value == 111
        b4.qualifier == LESS_THAN
    }
    
    
    def "parse float values"() {

        expect:
        QualifiedNumber.parse(values, Float.class).equals(results)
        QualifiedNumber.parse(values, Float.class).value instanceof Float
        
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
            new QualifiedNumber(370.1f),
            new QualifiedNumber(-2585.9f),
            new QualifiedNumber(38.6f, LESS_THAN),
            new QualifiedNumber(54f),
            new QualifiedNumber(38.6f, LESS_THAN),
            new QualifiedNumber(38.6f, LESS_THAN),
            new QualifiedNumber(38.6f, LESS_THAN),
            new QualifiedNumber(-38.6f, LESS_THAN),
            new QualifiedNumber(38.6f, LESS_THAN_OR_EQUALS),
            new QualifiedNumber(0.00000123386f, LESS_THAN)
        ]
    }
    
    def "parse integer values"() {

        expect:
        QualifiedNumber.parse(values, Integer.class).equals(results)
        QualifiedNumber.parse(values, Integer.class).value instanceof Integer
        
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
            new QualifiedNumber(370),
            new QualifiedNumber(-2585),
            new QualifiedNumber(38, LESS_THAN),
            new QualifiedNumber(54),
            new QualifiedNumber(38, LESS_THAN),
            new QualifiedNumber(38, LESS_THAN),
            new QualifiedNumber(38, LESS_THAN),
            new QualifiedNumber(-38, LESS_THAN),
            new QualifiedNumber(38, LESS_THAN_OR_EQUALS)
        ]
    }
    
    def "parse double values"() {

        expect:
        QualifiedNumber.parse(values, Double.class).equals(results)
        QualifiedNumber.parse(values, Double.class).value instanceof Double
        
        where:

        values << [
            '370.1',
            '-2585.9',
            '<38.6'
        ]

        results << [
            new QualifiedNumber(370.1d),
            new QualifiedNumber(-2585.9d),
            new QualifiedNumber(38.6d, LESS_THAN)
        ]
    }
    
    def "parse big decimal values"() {

        expect:
        QualifiedNumber.parse(values, BigDecimal.class).equals(results)
        QualifiedNumber.parse(values, BigDecimal.class).value instanceof BigDecimal
        
        where:

        values << [
            '370.1',
            '-2585.9',
            '<38.6'
        ]

        results << [
            new QualifiedNumber(370.1G),
            new QualifiedNumber(-2585.9G),
            new QualifiedNumber(38.6G, LESS_THAN)
        ]
    }
    
    def "parse long values"() {

        expect:
        QualifiedNumber.parse(values, Long.class).equals(results)
        QualifiedNumber.parse(values, Long.class).value instanceof Long
        
        where:

        values << [
            '370',
            '-2585',
            '<38'
        ]

        results << [
            new QualifiedNumber(370L),
            new QualifiedNumber(-2585L),
            new QualifiedNumber(38L, LESS_THAN)
        ]
    }
    

}
