package roart.aggregate;

import java.util.Map;

import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.pipeline.PipelineConstants;
import roart.util.MarketData;

public class AggregatorMLIndicatorMACD extends AggregatorMLIndicator {

    public AggregatorMLIndicatorMACD(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public String indicator() {
        return PipelineConstants.INDICATORMACD;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantAggregatorsIndicatorMACD();
    }

    // TODO this is duplicated
    @Override
    public Indicator getIndicator(Map<String, MarketData> marketdatamap, int category,
            Map<String, Indicator> newIndicatorMap, Map<String, Indicator> usedIndicatorMap) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        Indicator indicator = new IndicatorMACD(conf, null, marketdatamap, null, null, null, category);
        
        if (newIndicatorMap != null) {
            newIndicatorMap.put(indicator(), indicator);
        }
        return indicator;
    }

}
