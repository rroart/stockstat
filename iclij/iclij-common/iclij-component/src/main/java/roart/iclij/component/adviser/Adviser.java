package roart.iclij.component.adviser;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;

public abstract class Adviser {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected static final boolean VERIFYCACHE = false;
    
    protected Market market;
    
    protected LocalDate investStart;
    
    protected LocalDate investEnd;
    
    protected ComponentData param;

    protected SimulateInvestConfig simulateConfig;

    protected Map<Integer, List<Pair<String, Double>>> valueMap;
    
    protected Object object;
    
    public Adviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        this.market = market;
        this.investStart = investStart;
        this.investEnd = investEnd;
        this.param = param;
        this.simulateConfig = simulateConfig;
    }

    public abstract List<String> getIncs(String aParameter, int buytop,
            int indexOffset, List<String> stockDates, List<String> excludes);

    public abstract List<String> getParameters();

    public abstract void getValueMap(List<String> stockDates, int firstidx2, int lastidx2,
            Map<String, List<List<Double>>> categoryValueMap);

    public void setExtra(Object object) {
        this.object = object;
    }
}
