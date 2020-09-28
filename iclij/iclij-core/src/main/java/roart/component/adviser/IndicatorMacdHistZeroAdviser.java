package roart.component.adviser;

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

public class IndicatorMacdHistZeroAdviser extends IndicatorAdviser {

    public IndicatorMacdHistZeroAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        super(market, investStart, investEnd, param, simulateConfig);
    }

    @Override
    protected int getOffset() {
        return 0;
    }

    @Override
    protected int getOffset2() {
        return 3;
    }

    @Override
    protected String getPipeline() {
        return PipelineConstants.INDICATORMACD;
    }

    @Override
    protected void sort(List<Pair<String, Double>> valueList) {
        if (valueList.isEmpty()) {
            return;
        }
        List<Double> values = valueList.stream().map(Pair::getValue).collect(Collectors.toList());
        Double min = Collections.min(values);
        Double max = Collections.max(values);
        Double absmax = Math.max(Math.abs(min), Math.abs(max));
        int i = 0;
        for (Pair<String, Double> pair : new ArrayList<>(valueList)) {
            Pair aPair = new ImmutablePair(pair.getKey(), absmax - Math.abs(pair.getValue()));
            valueList.set(i, aPair);
            i++;
        }
        valueList.sort(Comparator.comparing(Pair::getValue));
        if (indicatorreverse) {
            Collections.reverse(valueList);
        }
    }

}
