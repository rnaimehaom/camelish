package com.im.examples.model.types

/** Represents a set of dose response data, along with an options XC50 curve.
 * The results are represented as Map of dose and repsonse values, which have
 * corresponding units (dose being of type Units.Concentration, response being of type
 * Units.Response.
 * 
 * Possibly this it too restictive as a set of dose response data could potentially 
 * have multiple curves fitted, and curves can be fitted to other types of data 
 * (e.g. time series), but probably this is sufficient for now.
 *
 * @author timbo
 */
class DoseResponseResults {
    
    Map<Number,Number> data
    Units.Concentration doseUnits
    Units.Response responseUnits
    XC50Curve curveFit
    
    DoseResponseResults(Map<Number,Number> data, Units.Concentration doseUnits, Units.Response responseUnits) {
        this.data = data
        this.doseUnits = doseUnits
        this.responseUnits = responseUnits
    }
    
    DoseResponseResults(Map<Number,Number> data, Units.Molarity doseUnits, Units.Response responseUnits, XC50Curve curveFit) {
        this.data = data
        this.doseUnits = doseUnits
        this.responseUnits = responseUnits
        this.curveFit = curveFit
    }
	
}

