package com.im.examples.model.types

import java.util.regex.Pattern

/** Represents a numberic value which can have a qualifier (modifier).
 * Examples would be 17, <22, >44, 5.
 * A critical aspect of this is how to sort and filter. If the above examples are 
 * sorted in ascending order are the results 5, 17, <22, >45, or <22, 5, 17, >45.
 * The answer is a little subjective (and so may need to be configurable), but 
 * generally speaking the first answer is probably the right one.
 * 
 * TODO - assess whether this should be changed to Java, but the dynamic aspects 
 * are pretty useful when it comes to aggregation. 
 */
class QualifiedValue<T extends Number> implements Comparable<QualifiedValue> {
    
    enum Qualifier {
        EQUALS('='), 
        APPROX_EQUALS('~'), 
        LESS_THAN('<'), 
        GREATER_THAN('>'),
        LESS_THAN_OR_EQUALS('<='), 
        GREATER_THAN_OR_EQUALS('>='),
        AMBIGUOUS('#') // for use in aggregations where the state is inconsistent
        
        String symbol
        
        Qualifier(String symbol) {
            this.symbol = symbol
        }
        
        String getSymbol() {
            return symbol
        }
        
        static Qualifier create(String symbol) {
            return Qualifier.values().find {
                it.symbol == symbol
            }
        }
    }
     
    T value
    Qualifier qualifier
    
    QualifiedValue(T v) {
        this.value = v
        this.qualifier = Qualifier.EQUALS
    }
    
    QualifiedValue(T v, Qualifier q) {
        this.value = v
        this.qualifier = q
    }
    
    QualifiedValue(T v, String q) {
        this.value = v
        this.qualifier = Qualifier.create(q)
    }
    
    QualifiedValue(QualifiedValue qv) {
        this.value = qv.value
        this.qualifier = qq.qualifier
    }
    /** Parse from a text represetnations. Values like this are supported:
     * 123.4, <123.4, < 123.4, <-123.4, >=15.2, 1234, -0.00001234
     * Integer, Float and Double types are supported. If your format does not match
     * this format then parse it yourself and use one of the constructors.
     * 
     * @param s The text to parse
     * @param cls the type to parse the number part to
     * @return The value
     */
    static QualifiedValue<T> parse(String s, Class<T> cls) {
        def matcher = s =~ /\s*(=|<|>|~|<=|>=)?\s*(\-?[0-9,\.]+)\s*/
        if (matcher.matches()) {
            def q = matcher[0][1]
            def v = matcher[0][2]
            if (v != null) {
                Number num = null
                switch (cls) {
                case Float:
                    num = new Float(v)
                    break
                case Integer:
                    num = new Integer(v)
                    break
                case Double:
                    num = new Double(v)
                    break
                default:
                    throw new IllegalArgumentException("Type $cls.name not supported")
                }
                Qualifier qual = Qualifier.EQUALS
                if (q != null) {
                    qual = Qualifier.create(q)
                }
                return new QualifiedValue(num, qual) 
            }
        }
        throw new IllegalArgumentException("Format $s not supported")
    }
    
    @Override
    boolean equals(Object o) {
        if (o == null) { return false }
        if (!(o instanceof QualifiedValue)) { return false }
        return value.equals(o.value) && qualifier.equals(o.qualifier)
    }
    
    @Override
    int compareTo(QualifiedValue o) {
        return value.compareTo(o.value)
    }
    
    @Override
    String toString() {
       "${qualifier.symbol}$value"
    }
    
    QualifiedValue plus(QualifiedValue other) {
        T sum = value + other.value
        return new QualifiedValue(sum, resolveQualifier(qualifier, other.qualifier))
    } 
    
    QualifiedValue minus(QualifiedValue other) {
        T sum = value - other.value
        return new QualifiedValue(sum, resolveQualifier(qualifier, other.qualifier))
    } 
  
    private static Qualifier resolveQualifier(Qualifier a, Qualifier b) {
        if (a == b) {
            return a
        } else if (a == Qualifier.EQUALS) {
            return b
        } else if (b == Qualifier.EQUALS) {
            return a
        } else {
            return Qualifier.AMBIGUOUS
        }
    }
    
    QualifiedValue multiply(Number num) {
        def result = value * num
        return new QualifiedValue(result, qualifier)
    }
    
    QualifiedValue div(Number num) {
        def result = value / num
        return new QualifiedValue(result, qualifier)
    }
    
}

