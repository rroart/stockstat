package roart.component.adviser;

import java.time.LocalDate;

import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;

public class IndicatorMacdSignalAdviser extends IndicatorAdviser {

    public IndicatorMacdSignalAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        super(market, investStart, investEnd, param, simulateConfig);
    }

    @Override
    protected int getOffset() {
        return 2;
    }

    @Override
    protected int getOffset2() {
        return 3;
    }

    @Override
    protected String getPipeline() {
        return PipelineConstants.INDICATORMACD;
    }

}
