package com.im.examples.model.types;

/**
 *
 * @author timbo
 */
public enum Qualifier {

    EQUALS("="),
    APPROX_EQUALS("~"),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    LESS_THAN_OR_EQUALS("<="),
    GREATER_THAN_OR_EQUALS(">="),
    AMBIGUOUS("#"); // for use in aggregations where the state is inconsistent

    private final String symbol;

    Qualifier(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    static Qualifier create(String symbol) {
        Qualifier result = null;
        for (Qualifier q : Qualifier.values()) {
            if (q.symbol.equals(symbol)) {
                result = q;
                break;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("Invalid symbol " + symbol);
        }
        return result;
    }
}
