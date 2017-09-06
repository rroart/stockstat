package roart.aggregate;

import java.util.Map;

import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.pipeline.Pipeline;
import roart.util.MarketData;

public abstract class AggregatorIndicator {
    protected MyMyConfig conf;
    public AggregatorIndicator(MyMyConfig conf) {
        this.conf = conf;
    }

    public abstract String indicator();
    public abstract boolean isEnabled();
    public abstract Indicator getIndicator(Map<String, MarketData> marketdatamap, int category, Map<String, Indicator> newIndicatorMap, Map<String, Indicator> usedIndicatorMap, Pipeline[] datareaders) throws Exception;

}

