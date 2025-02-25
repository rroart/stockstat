package roart.common.pipeline.data;

import java.util.ArrayList;
import java.util.List;

import roart.simulate.model.SimulateStock;

public class SerialListSimulateStock extends SerialObject {
    private List<SimulateStock> list = new ArrayList<>();

    public SerialListSimulateStock() {
        super();
    }

    public SerialListSimulateStock(List<SimulateStock> list) {
        super();
        this.list = list;
    }

    public List<SimulateStock> getList() {
        return list;
    }

    public void setList(List<SimulateStock> list) {
        this.list = list;
    }

    public void add(SimulateStock object) {
        list.add(object);
    }

    public SimulateStock get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

}
