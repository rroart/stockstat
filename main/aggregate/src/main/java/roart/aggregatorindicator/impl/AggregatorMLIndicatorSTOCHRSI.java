package roart.aggregatorindicator.impl;

import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorSTOCHRSI;
import roart.pipeline.Pipeline;
import roart.model.data.MarketData;

public class AggregatorMLIndicatorSTOCHRSI extends AggregatorMLIndicator {

    public AggregatorMLIndicatorSTOCHRSI(IclijConfig conf) {
        super(conf);
    }

    @Override
    public String indicator() {
        return PipelineConstants.INDICATORSTOCHRSI;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantAggregatorsIndicatorSTOCHRSI();
    }

    // this is duplicated
    @Override
    public AbstractIndicator getIndicator(Map<String, MarketData> marketdatamap, int category,
            Map<String, AbstractIndicator> newIndicatorMap, Map<String, AbstractIndicator> usedIndicatorMap, Pipeline[] datareaders) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        AbstractIndicator indicator = new IndicatorSTOCHRSI(conf, null, null, category, datareaders, false);
        
        if (newIndicatorMap != null) {
            newIndicatorMap.put(indicator(), indicator);
        }
        return indicator;
    }

}
