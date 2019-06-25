package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.RecommendConstants;

public class ATRRecommendComplex extends RecommendATR {
    
    public ATRRecommendComplex(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public  List<Pair<String, String>> getBuyList() {
        List<Pair<String, String>> buyList = new ArrayList<>();
        buyList.add(new Pair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRBUYWEIGHTATRNODE, ConfigConstants.INDICATORSATR));
        buyList.add(new Pair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRBUYWEIGHTATRDELTANODE, ConfigConstants.INDICATORSATRDELTA));
        return buyList;
    }

    @Override
    public  List<Pair<String, String>> getSellList() {
        List<Pair<String, String>> sellList = new ArrayList<>();
        sellList.add(new Pair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRSELLWEIGHTATRNODE, ConfigConstants.INDICATORSATR));
        sellList.add(new Pair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRSELLWEIGHTATRDELTANODE, ConfigConstants.INDICATORSATRDELTA));
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
