package com.im.examples.model.types

/**
 *
 * @author timbo
 */
class XC50Curve {
   
    QualifiedNumber xc50
    Number hill
    Number top
    Number bottom
    Number rsquared

    
    XC50Curve(QualifiedNumber xc50) {
        this.xc50 = xc50
    }
    
    XC50Curve(QualifiedNumber xc50, Number hill, Number top, Number bottom) {
        this(xc50)
        this.hill = hill
        this.top = top
        this.bottom = bottom
    }
    
    XC50Curve(QualifiedNumber xc50, Number hill, Number top, Number, bottom, Number rsquared) {
        this(xc50, hill, top, bottom)
        this.rsquared = rsquared
    }
}

