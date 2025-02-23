package roart.aggregatorindicator.impl;

import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.inmemory.model.Inmemory;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorSTOCHRSI;
import roart.pipeline.Pipeline;

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
    public AbstractIndicator getIndicator(int category, Map<String, AbstractIndicator> newIndicatorMap,
            Map<String, AbstractIndicator> usedIndicatorMap, PipelineData[] datareaders, String catName, Inmemory inmemory) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        AbstractIndicator indicator = new IndicatorSTOCHRSI(conf, null, null, category, datareaders, false, inmemory);
        
        if (newIndicatorMap != null) {
            newIndicatorMap.put(indicator(), indicator);
        }
        return indicator;
    }

}
