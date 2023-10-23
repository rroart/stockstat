package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorRSI;

public abstract class RecommendRSI extends Recommend {

    public static AbstractIndicator indicator;
    
    public RecommendRSI(IclijConfig conf) {
        super(conf);
    }

    @Override
    public AbstractIndicator getIndicator(int category, Map<String, AbstractIndicator> newIndicatorMap, Map<String, AbstractIndicator> usedIndicatorMap, PipelineData[] datareaders) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        AbstractIndicator indicator = new IndicatorRSI(conf, null, null, category, datareaders, false);
        
        if (newIndicatorMap != null) {
            newIndicatorMap.put(indicator(), indicator);
        }
       return indicator;
    }
    
    @Override
    public String indicator() {
        return PipelineConstants.INDICATORRSI;
    }
/*
    public static List<Recommend> getClasses(IclijConfig conf) {
        List<Recommend> all = new ArrayList<>();
        all.add(new RSIRecommendSimple(conf));
        all.add(new RSIRecommendComplex(conf));
        List<Recommend> result = new ArrayList<>();
        for (Recommend recommend : all) {
            if (recommend.isEnabled()) {
                result.add(recommend);
            }
        }
        return result;
    }
    */
}

