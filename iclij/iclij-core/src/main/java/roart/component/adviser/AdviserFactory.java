package roart.component.adviser;

import java.time.LocalDate;

import roart.component.model.ComponentData;
import roart.iclij.config.Market;

public class AdviserFactory {

    public static Adviser get(int i, Market market, LocalDate investStart, LocalDate investEnd, ComponentData param) {
        switch (i) {
        case 0:
            return new AboveBelowAdviser(market, investStart, investEnd, param);
        case 1:
            return new PeriodAdviser(market, investStart, investEnd, param);
        case 2:
            return new IndicatorMacdMacdAdviser(market, investStart, investEnd, param);
        case 3:
            return new IndicatorMacdSignalAdviser(market, investStart, investEnd, param);
        case 4:
            return new IndicatorMacdHistAdviser(market, investStart, investEnd, param);
        case 5:
            return new IndicatorRsiAdviser(market, investStart, investEnd, param);
        default:
            return null;
        }
    }
}
