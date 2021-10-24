package roart.iclij.component.adviser;

import java.time.LocalDate;

import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;

public class IndicatorMacdHistAdviser extends IndicatorAdviser {

    public IndicatorMacdHistAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
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

}
