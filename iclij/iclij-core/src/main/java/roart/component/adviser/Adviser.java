package roart.component.adviser;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;

public abstract class Adviser {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected Market market;
    protected LocalDate investStart;
    protected LocalDate investEnd;
    protected ComponentData param;

    public Adviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param) {
        this.market = market;
        this.investStart = investStart;
        this.investEnd = investEnd;
        this.param = param;
    }

    public abstract List<IncDecItem> getIncs(String aParameter, int buytop,
            LocalDate date, int indexOffset, List<String> stockDates, List<String> excludes);

    public abstract List<String> getParameters();

    public abstract double getReliability(LocalDate date, Boolean above);
}
