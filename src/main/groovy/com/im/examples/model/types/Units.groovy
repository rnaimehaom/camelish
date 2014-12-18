package com.im.examples.model.types

/**
 *
 * @author timbo
 */
interface Units {
    
    enum Molarity implements Units {
        
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

}
          