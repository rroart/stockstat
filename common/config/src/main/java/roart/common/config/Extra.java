package roart.common.config;

import java.util.List;

public class Extra {
    private List<MarketStockExpression> complex;

    private List<MarketStock> simple;

    public Extra() {
        super();
    }

    public List<MarketStockExpression> getComplex() {
        return complex;
    }

    public void setComplex(List<MarketStockExpression> complex) {
        this.complex = complex;
    }

    public List<MarketStock> getSimple() {
        return simple;
    }

    public void setSimple(List<MarketStock> simple) {
        this.simple = simple;
    }
}
