package roart.iclij.component.adviser;

import java.time.LocalDate;

import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;

public class IndicatorRsiAdviser extends IndicatorAdviser {

    public IndicatorRsiAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        super(market, investStart, investEnd, param, simulateConfig);
    }

    @Override
    protected int getOffset() {
        return 0;
    }

    @Override
    protected int getOffset2() {
        return 1;
    }

    @Override
    protected String getPipeline() {
        return PipelineConstants.INDICATORRSI;
    }

}
