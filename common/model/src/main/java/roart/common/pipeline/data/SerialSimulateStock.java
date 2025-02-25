package roart.common.pipeline.data;

import roart.simulate.model.SimulateStock;

public class SerialSimulateStock extends SerialObject {
    private SimulateStock simulateStock;

    public SerialSimulateStock() {
        super();
    }

    public SerialSimulateStock(SimulateStock simulateStock) {
        super();
        this.simulateStock = simulateStock;
    }

    public SimulateStock getSimulateStock() {
        return simulateStock;
    }

    public void setSimulateStock(SimulateStock simulateStock) {
        this.simulateStock = simulateStock;
    }
    
}
