package roart.component.adviser;

import java.time.LocalDate;

import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;

public class AdviserFactory {

    public static Adviser get(int i, Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        switch (i) {
        case 0:
            return new AboveBelowAdviser(market, investStart, investEnd, param, simulateConfig);
        case 1:
            return new PeriodAdviser(market, investStart, investEnd, param, simulateConfig);
        case 2:
            return new IndicatorMacdMacdAdviser(market, investStart, investEnd, param, simulateConfig);
        case 3:
            return new IndicatorMacdSignalAdviser(market, investStart, investEnd, param, simulateConfig);
        case 4:
            return new IndicatorMacdHistAdviser(market, investStart, investEnd, param, simulateConfig);
        case 5:
            return new IndicatorRsiAdviser(market, investStart, investEnd, param, simulateConfig);
        case 6:
            return new DayAdviser(market, investStart, investEnd, param, simulateConfig);
        default:
            return null;
        }
    }
}
