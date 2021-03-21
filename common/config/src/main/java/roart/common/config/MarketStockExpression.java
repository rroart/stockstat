package roart.common.config;

import java.util.List;

public class MarketStockExpression {

    // Inner list:
    // ( market, id, (eventual) field )
    private List<MarketStock> items;
       
    private String expression;

    public MarketStockExpression() {
        super();
    }

    public List<MarketStock> getItems() {
        return items;
    }

    public void setItems(List<MarketStock> items) {
        this.items = items;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
   
}
