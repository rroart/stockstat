package roart.aggregatorindicator;

import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.indicator.AbstractIndicator;
import roart.pipeline.Pipeline;

public abstract class AggregatorIndicator {
    protected IclijConfig conf;
    public AggregatorIndicator(IclijConfig conf) {
        this.conf = conf;
    }

    public abstract String indicator();
    public abstract boolean isEnabled();
    public abstract AbstractIndicator getIndicator(int category, Map<String, AbstractIndicator> newIndicatorMap, Map<String, AbstractIndicator> usedIndicatorMap, Pipeline[] datareaders) throws Exception;

}

