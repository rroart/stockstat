package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.aggregatorindicator.AggregatorIndicator;
import roart.iclij.config.IclijConfig;

public abstract class AggregatorMLIndicator extends AggregatorIndicator {

    private static final String ANYTHING = "anything";
    
    public AggregatorMLIndicator(IclijConfig conf) {
        super(conf);
    }

    public static Map<String, List<AggregatorMLIndicator>> getUsedAggregatorMLIndicators(IclijConfig conf) {
        List<AggregatorMLIndicator> all = new ArrayList<>();
        all.add(new AggregatorMLIndicatorMACD(conf));
        all.add(new AggregatorMLIndicatorRSI (conf));
        all.add(new AggregatorMLIndicatorATR (conf));
        all.add(new AggregatorMLIndicatorCCI (conf));
        all.add(new AggregatorMLIndicatorSTOCH (conf));
        all.add(new AggregatorMLIndicatorSTOCHRSI (conf));
        Map<String, List<AggregatorMLIndicator>> result = new HashMap<>();
        for (AggregatorMLIndicator recommend : all) {
            if (recommend.isEnabled()) {
                List<AggregatorMLIndicator> recommendList = result.get(ANYTHING);
                if (recommendList == null) {
                    recommendList = new ArrayList<>();
                }
                recommendList.add(recommend);
                result.put(ANYTHING, recommendList);
            }
        }
        return result;
    }
    
}
