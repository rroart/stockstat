package roart.aggregatorindicator.impl;

import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorRSI;
import roart.pipeline.Pipeline;
import roart.model.data.MarketData;

public class AggregatorMLIndicatorRSI extends AggregatorMLIndicator {

    public AggregatorMLIndicatorRSI(MyMyConfig conf) {
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

    // TODO this is duplicated
    @Override
    public AbstractIndicator getIndicator(Map<String, MarketData> marketdatamap, int category,
            Map<String, AbstractIndicator> newIndicatorMap, Map<String, AbstractIndicator> usedIndicatorMap, Pipeline[] datareaders) throws Exception {
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

