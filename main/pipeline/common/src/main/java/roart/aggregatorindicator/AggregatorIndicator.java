package roart.aggregatorindicator;

import java.util.Map;

import roart.common.pipeline.data.PipelineData;
import roart.iclij.config.IclijConfig;
import roart.indicator.AbstractIndicator;
import roart.common.inmemory.model.Inmemory;

public abstract class AggregatorIndicator {
    protected IclijConfig conf;
    public AggregatorIndicator(IclijConfig conf) {
        this.conf = conf;
    }

    public abstract String indicator();
    public abstract boolean isEnabled();
    public abstract AbstractIndicator getIndicator(int category, Map<String, AbstractIndicator> newIndicatorMap, Map<String, AbstractIndicator> usedIndicatorMap, PipelineData[] datareaders, String catName, Inmemory inmemory) throws Exception;

}

