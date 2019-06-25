package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.RecommendConstants;

public class RSIRecommendSimple extends RecommendRSI {
    
    public RSIRecommendSimple(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public  List<Pair<String, String>> getBuyList() {
        List<Pair<String, String>> buyList = new ArrayList<>();
        buyList.add(new Pair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSIBUYWEIGHTRSI, ConfigConstants.INDICATORSRSI));
        buyList.add(new Pair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSIBUYWEIGHTRSIDELTA, ConfigConstants.INDICATORSRSIDELTA));
        return buyList;
    }

    @Override
    public  List<Pair<String, String>> getSellList() {
        List<Pair<String, String>> sellList = new ArrayList<>();
        sellList.add(new Pair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSISELLWEIGHTRSI, ConfigConstants.INDICATORSRSI));
        sellList.add(new Pair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSISELLWEIGHTRSIDELTA, ConfigConstants.INDICATORSRSIDELTA));
        return sellList;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantRecommenderSimpleRSI();
    }

    @Override
    public String complexity() {
        return RecommendConstants.SIMPLE;
    }

    
    @Override
    public int getFutureDays() {
        return conf.getTestIndicatorRecommenderSimpleFutureDays();
    }
    
    @Override
    public int getIntervalDays() {
        return conf.getTestIndicatorRecommenderSimpleIntervalDays();
    }
}

