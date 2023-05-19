package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.RecommendConstants;

public class RSIRecommendSimple extends RecommendRSI {
    
    public RSIRecommendSimple(IclijConfig conf) {
        super(conf);
    }

    @Override
    public  List<Pair<String, String>> getBuyList() {
        List<Pair<String, String>> buyList = new ArrayList<>();
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSIBUYWEIGHTRSI, ConfigConstants.INDICATORSRSI));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSIBUYWEIGHTRSIDELTA, ConfigConstants.INDICATORSRSIDELTA));
        return buyList;
    }

    @Override
    public  List<Pair<String, String>> getSellList() {
        List<Pair<String, String>> sellList = new ArrayList<>();
        sellList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSISELLWEIGHTRSI, ConfigConstants.INDICATORSRSI));
        sellList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSISELLWEIGHTRSIDELTA, ConfigConstants.INDICATORSRSIDELTA));
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

