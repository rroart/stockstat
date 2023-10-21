package roart.aggregatorindicator.impl;

import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorATR;
import roart.pipeline.Pipeline;

public class AggregatorMLIndicatorATR extends AggregatorMLIndicator {

    public AggregatorMLIndicatorATR(IclijConfig conf) {
        super(conf);
    }

    @Override
    public String indicator() {
        return PipelineConstants.INDICATORATR;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantAggregatorsIndicatorATR();
    }

    // this is duplicated
    @Override
    public AbstractIndicator getIndicator(int category, Map<String, AbstractIndicator> newIndicatorMap,
            Map<String, AbstractIndicator> usedIndicatorMap, Pipeline[] datareaders) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        AbstractIndicator indicator = new IndicatorATR(conf, null, null, category, datareaders, false);
        
        if (newIndicatorMap != null) {
            newIndicatorMap.put(indicator(), indicator);
        }
        return indicator;
    }

}
