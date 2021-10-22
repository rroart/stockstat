package roart.iclij.component.adviser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;

public class IndicatorMacdHistZeroAdviser extends IndicatorMacdHistAdviser {

    public IndicatorMacdHistZeroAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        super(market, investStart, investEnd, param, simulateConfig);
    }

    @Override
    protected void sort(List<Pair<String, Double>> valueList) {
        if (valueList.isEmpty()) {
            return;
        }
        int i = 0;
        for (Pair<String, Double> pair : new ArrayList<>(valueList)) {
            Pair<String, Double> aPair = new ImmutablePair(pair.getKey(), Math.abs(pair.getValue()));
            valueList.set(i, aPair);
            i++;
        }
        valueList.sort(Comparator.comparing(Pair::getValue));
        if (indicatorreverse) {
            Collections.reverse(valueList);
        }
    }

}
