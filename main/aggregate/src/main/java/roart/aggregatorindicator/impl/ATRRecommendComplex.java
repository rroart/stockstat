package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.RecommendConstants;

public class ATRRecommendComplex extends RecommendATR {
    
    public ATRRecommendComplex(IclijConfig conf) {
        super(conf);
    }

    @Override
    public  List<Pair<String, String>> getBuyList() {
        List<Pair<String, String>> buyList = new ArrayList<>();
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRBUYWEIGHTATRNODE, ConfigConstants.INDICATORSATR));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRBUYWEIGHTATRDELTANODE, ConfigConstants.INDICATORSATRDELTA));
        return buyList;
    }

    @Override
    public  List<Pair<String, String>> getSellList() {
        List<Pair<String, String>> sellList = new ArrayList<>();
        sellList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRSELLWEIGHTATRNODE, ConfigConstants.INDICATORSATR));
        sellList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRSELLWEIGHTATRDELTANODE, ConfigConstants.INDICATORSATRDELTA));
        return sellList;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantRecommenderComplexATR();
    }

    @Override
    public String complexity() {
        return RecommendConstants.COMPLEX;
    }

    
    @Override
    public int getFutureDays() {
        return conf.getTestIndicatorRecommenderComplexFutureDays();
    }
    
    @Override
    public int getIntervalDays() {
        return conf.getTestIndicatorRecommenderComplexIntervalDays();
    }
}

