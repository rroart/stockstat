package roart.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.pipeline.PipelineConstants;
import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.indicator.IndicatorRSI;
import roart.util.MarketData;

public abstract class RecommendRSI extends Recommend {

    public static Indicator indicator;
    
    public RecommendRSI(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public Indicator getIndicator(Map<String, MarketData> marketdatamap, int category, Map<String, Indicator> newIndicatorMap, Map<String, Indicator> usedIndicatorMap) throws Exception {
        if (usedIndicatorMap != null && usedIndicatorMap.containsKey(indicator())) {
            return usedIndicatorMap.get(indicator());
        }

        Indicator indicator = new IndicatorRSI(conf, null, marketdatamap, null, null, null, category);
        
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
    public static List<Recommend> getClasses(MyMyConfig conf) {
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

