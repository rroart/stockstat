package roart.common.pipeline.data;

import roart.simulate.model.StockHistory;

public class SerialStockHistory extends SerialObject {
    private StockHistory stockHistory;

    public SerialStockHistory() {
        super();
    }

    public SerialStockHistory(StockHistory stockHistory) {
        super();
        this.stockHistory = stockHistory;
    }

    public StockHistory getStockHistory() {
        return stockHistory;
    }

    public void setStockHistory(StockHistory stockHistory) {
        this.stockHistory = stockHistory;
    }
}
