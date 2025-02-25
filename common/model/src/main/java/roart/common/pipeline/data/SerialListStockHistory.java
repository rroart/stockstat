package roart.common.pipeline.data;

import java.util.ArrayList;
import java.util.List;

import roart.simulate.model.StockHistory;

public class SerialListStockHistory extends SerialObject {
    private List<StockHistory> list = new ArrayList<>();

    public SerialListStockHistory() {
        super();
    }

    public SerialListStockHistory(List<StockHistory> list) {
        super();
        this.list = list;
    }

    public List<StockHistory> getList() {
        return list;
    }

    public void setList(List<StockHistory> list) {
        this.list = list;
    }

    public void add(StockHistory object) {
        list.add(object);
    }

    public StockHistory get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

}
