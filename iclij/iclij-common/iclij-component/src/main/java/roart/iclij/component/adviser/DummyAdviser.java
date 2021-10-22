package roart.iclij.component.adviser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;

public class DummyAdviser extends Adviser {

    public DummyAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param,
            SimulateInvestConfig simulateConfig) {
        super(market, investStart, investEnd, param, simulateConfig);
    }

    @Override
    public List<String> getIncs(String aParameter, int buytop, int indexOffset, List<String> stockDates,
            List<String> excludes) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getParameters() {
        return null;
    }

    @Override
    public void getValueMap(List<String> stockDates, int firstidx2, int lastidx2,
            Map<String, List<List<Double>>> categoryValueMap) {
    }

}
