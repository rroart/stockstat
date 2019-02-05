package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.List;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.RecommendConstants;

public class RSIRecommendComplex extends RecommendRSI {
    
    // TODO add deltadays?
    
    public RSIRecommendComplex(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public  List<String> getBuyList() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSINODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSIDELTANODE);
        return buyList;
    }

    @Override
    public  List<String> getSellList() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSINODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSIDELTANODE);
        return sellList;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantRecommenderComplexRSI();
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

