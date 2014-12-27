package com.im.examples.model.types

/** Defintion of types of units, also, optionally allowing inter-conversion where
 * possible.
 * 
 * TODO - should the enums be replaced with classes so that this can be extended
 * with new types more easily?
 *
 * @author timbo
 */
interface Units {
    
    interface Concentration extends Units { }
    interface Response extends Units { }
    interface Time extends Units { }
    
    enum Duration implements Time {
        
        // TODO - handle conversions
        DAYS,
        HOURS,
        MINUTES,
        SECONDS,
        MILLI_SECONDS,
        MICRO_SECONDS,
        NANO_SECONDS,
        PICO_SECONDS
    }
    
    enum NumericResponse implements Response {
        
        PERCENT(100.0), // percentage expected to be between 0 and 100
        FRACTION(1.0),  // number expected to be between 0 and 1
        NUMBER(null)    // any number, inter-conversion not possible
        
        def scale
        
        NumericResponse(def scale) {
            this.scale = scale
        }
        
        static Number convertTo(Response from, Response to, Number value) {
            if (from.scale == null || to.scale == null) {
                throw new IllegalStateException("Cannot convert values")
            }
            return (value * from.scale / to.scale)
        }
        
        Number convertTo(Response to, Number value) {
            return convertTo(this, to, value)
        }
        
    }
    
    enum Molarity implements Concentration {
        
        MOLAR('M', 1.0),
        MILLI_MOLAR('mM', 0.001),
        MICRO_MOLAR('µM', ['uM'], 0.000001),
        NANO_MOLAR('nM', 0.000000001),
        PICO_MOLAR('pM', 0.000000000001),
        FEMTO_MOLAR('fM', 0.000000000000001)
    
        String symbol
        List<String> otherSymbols = []
        def scale
        
        Molarity(String symbol, def scale) {
            this.symbol = symbol
            this.scale = scale
        }
        
        Molarity(String symbol, List<String> otherSymbols, def scale) {
            this.symbol = symbol
            this.otherSymbols = otherSymbols
            this.scale = scale
        }
        
        static Number convertTo(Molarity from, Molarity to, Number value) {
            return (value * from.scale / to.scale)
        }
        
        Number convertTo(Molarity to, Number value) {
            return convertTo(this, to, value)
        }
        
        /** Generate the correct enum from text representation. Representation is 
         * quite restrictive. M, mM, uM, µM, nM, pM fM with no spaces, case being important.
         * 
         * @param symbol The text to parse
         * @return The correct Molarity enum
         * @throws IllegalArgumentException if no valid emum found
         */
        static Molarity parse(String symbol) {
            Molarity result = Molarity.values().find {
                symbol == it.symbol || it.otherSymbols.find { it == symbol }
            }
            if (result == null) {
                throw new IllegalArgumentException("Unsuppported type of Molarity: $symbol")
            }
            return result
        }
	
    }
    
    enum MassPerVolume implements Concentration {
        
        MG_ML('mg/ml', 1.0),
        G_L('g/l', 1.0)
        // TODO - others such as ug/ml, ng/ml
    
        String symbol
        def scale
        
        MassPerVolume(String symbol, def scale) {
            this.symbol = symbol
            this.scale = scale
        }
    }

}
          