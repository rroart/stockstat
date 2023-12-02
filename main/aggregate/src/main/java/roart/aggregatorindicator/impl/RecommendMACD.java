package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorMACD;

public abstract class RecommendMACD extends Recommend {

    public RecommendMACD(IclijConfig conf) {
        super(conf);
    }

    @Override
    public AbstractIndicator getIndicator(int category, Map<String, AbstractIndicator> newIndicatorMap, Map<String, AbstractIndicator> usedIndicatorMap, PipelineData[] datareaders, String catName) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        AbstractIndicator indicator = new IndicatorMACD(conf, null, catName, category, datareaders, false);
        
        if (newIndicatorMap != null) {
            newIndicatorMap.put(indicator(), indicator);
        }
        return indicator;
    }
 
    @Override
    public String indicator() {
        return PipelineConstants.INDICATORMACD;
    }
    /*
    public static List<Recommend> getClasses(IclijConfig conf) {
        List<Recommend> all = new ArrayList<>();
        all.add(new MACDRecommendSimple(conf));
        all.add(new MACDRecommendComplex(conf));
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
