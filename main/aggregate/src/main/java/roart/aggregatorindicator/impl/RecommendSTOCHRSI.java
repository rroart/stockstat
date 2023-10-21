package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.pipeline.Pipeline;
import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorRSI;
import roart.indicator.impl.IndicatorSTOCHRSI;

public abstract class RecommendSTOCHRSI extends Recommend {

    public static AbstractIndicator indicator;
    
    public RecommendSTOCHRSI(IclijConfig conf) {
        super(conf);
    }

    @Override
    public AbstractIndicator getIndicator(int category, Map<String, AbstractIndicator> newIndicatorMap, Map<String, AbstractIndicator> usedIndicatorMap, Pipeline[] datareaders) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        AbstractIndicator indicator = new IndicatorSTOCHRSI(conf, null, null, category, datareaders, false);
        
        if (newIndicatorMap != null) {
            newIndicatorMap.put(indicator(), indicator);
        }
       return indicator;
    }
    
    @Override
    public String indicator() {
        return PipelineConstants.INDICATORSTOCHRSI;
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

