package roart.aggregate;

import java.util.Map;

import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.indicator.IndicatorRSI;
import roart.pipeline.PipelineConstants;
import roart.util.MarketData;

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
    public Indicator getIndicator(Map<String, MarketData> marketdatamap, int category,
            Map<String, Indicator> newIndicatorMap, Map<String, Indicator> usedIndicatorMap) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        Indicator indicator = new IndicatorRSI(conf, null, marketdatamap, null, null, null, category);
        
        if (newIndicatorMap != null) {
            newIndicatorMap.put(indicator(), indicator);
        }
       return indicator;
    }

}

