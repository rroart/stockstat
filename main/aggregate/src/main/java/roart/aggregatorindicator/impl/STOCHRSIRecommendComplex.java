package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.RecommendConstants;

public class STOCHRSIRecommendComplex extends RecommendSTOCHRSI {
    
    public STOCHRSIRecommendComplex(IclijConfig conf) {
        super(conf);
    }

    @Override
    public  List<Pair<String, String>> getBuyList() {
        List<Pair<String, String>> buyList = new ArrayList<>();
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSIBUYWEIGHTSTOCHRSINODE, ConfigConstants.INDICATORSSTOCHRSI));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSIBUYWEIGHTSTOCHRSIDELTANODE, ConfigConstants.INDICATORSSTOCHRSIDELTA));
        return buyList;
    }

    @Override
    public  List<Pair<String, String>> getSellList() {
        List<Pair<String, String>> sellList = new ArrayList<>();
        sellList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSISELLWEIGHTSTOCHRSINODE, ConfigConstants.INDICATORSSTOCHRSI));
        sellList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSISELLWEIGHTSTOCHRSIDELTANODE, ConfigConstants.INDICATORSSTOCHRSIDELTA));
        return sellList;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantRecommenderComplexSTOCHRSI();
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

