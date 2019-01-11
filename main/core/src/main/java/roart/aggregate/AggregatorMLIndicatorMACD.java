package roart.aggregate;

import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.pipeline.Pipeline;
import roart.model.data.MarketData;

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
            Map<String, Indicator> newIndicatorMap, Map<String, Indicator> usedIndicatorMap, Pipeline[] datareaders) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        Indicator indicator = new IndicatorMACD(conf, null, marketdatamap, null, null, null, category, datareaders, false);
        
        if (newIndicatorMap != null) {
            newIndicatorMap.put(indicator(), indicator);
        }
        return indicator;
    }

}
