package roart.component.adviser;

import java.time.LocalDate;

import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;

public class IndicatorMacdHistAdviser extends IndicatorAdviser {

    public IndicatorMacdHistAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param) {
        super(market, investStart, investEnd, param);
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
