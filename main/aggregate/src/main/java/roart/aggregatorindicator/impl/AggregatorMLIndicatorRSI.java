package roart.aggregatorindicator.impl;

import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorRSI;

public class AggregatorMLIndicatorRSI extends AggregatorMLIndicator {

    public AggregatorMLIndicatorRSI(IclijConfig conf) {
        super(conf);
    }

    @Override
    public String indicator() {
        return PipelineConstants.INDICATORRSI;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantAggregatorsIndicatorRSI();
    }

    // this is duplicated
    @Override
    public AbstractIndicator getIndicator(int category, Map<String, AbstractIndicator> newIndicatorMap,
            Map<String, AbstractIndicator> usedIndicatorMap, PipelineData[] datareaders) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        AbstractIndicator indicator = new IndicatorRSI(conf, null, null, category, datareaders, false);
        
        if (newIndicatorMap != null) {
            newIndicatorMap.put(indicator(), indicator);
        }
       return indicator;
    }

}

