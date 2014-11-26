/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.examples.model

/**
 *
 * @author timbo
 */
class QualifiedValue {
    
    enum Qualifier {
        EQUALS('='), 
        LESS_THAN('<'), 
        GREATER_THAN('>')
        
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
    
    Float value
    Qualifier qualifier
	
}

