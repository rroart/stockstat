package roart.aggregatorindicator;

import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.indicator.AbstractIndicator;
import roart.pipeline.Pipeline;
import roart.model.data.MarketData;

public abstract class AggregatorIndicator {
    protected MyMyConfig conf;
    public AggregatorIndicator(MyMyConfig conf) {
        this.conf = conf;
    }

    public abstract String indicator();
    public abstract boolean isEnabled();
    public abstract AbstractIndicator getIndicator(Map<String, MarketData> marketdatamap, int category, Map<String, AbstractIndicator> newIndicatorMap, Map<String, AbstractIndicator> usedIndicatorMap, Pipeline[] datareaders) throws Exception;

}

