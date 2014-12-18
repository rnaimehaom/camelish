package com.im.examples.model.types

/**
 *
 * @author timbo
 */
class QualifiedNumberWithUnits<T extends Number> extends QualifiedNumber<T> {
    
    Units units
    
    QualifiedNumberWithUnits(T v, Qualifier q, Units units) {
        super(v, q)
        this.units = units
    }
    
    QualifiedNumberWithUnits convertTo(Units to) {
        if (to == this.units) {
            return this
        } else {
            T converted = this.units.convertTo(to, this.value)
            return new QualifiedNumberWithUnits(converted, this.qualifier, to)
        }
    }
    
    // TODO - implement parsing from string
    
    @Override
    boolean equals(Object o) {
        if (o == null) { return false }
        if (!(o instanceof QualifiedNumberWithUnits)) { return false }
        QualifiedNumberWithUnits other = o.convertTo(units)
        return value.equals(other.value) && qualifier.equals(other.qualifier)
    }
    
    @Override
    int compareTo(QualifiedNumber o) {
        QualifiedNumberWithUnits other = o.convertTo(units)
        return value.compareTo(other.value)
    }
    
    @Override
    String toString() {
       "${qualifier.symbol}$value${units.symbol}"
    }
	
}

