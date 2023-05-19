package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.List;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;

public abstract class RSIRecommend extends RecommendRSI {
    
    public RSIRecommend(IclijConfig conf) {
        super(conf);
    }

    /*
    @Override
    public  List<String> getBuyList() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.INDICATORSRSIRECOMMENDBUYWEIGHT);
        buyList.add(ConfigConstants.INDICATORSRSIRECOMMENDBUYWEIGHTDELTA);
        return buyList;
    }

    @Override
    public  List<String> getSellList() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.INDICATORSRSIRECOMMENDSELLWEIGHT);
        sellList.add(ConfigConstants.INDICATORSRSIRECOMMENDSELLWEIGHTDELTA);
        return sellList;
    }
*/
    
    @Override
    public boolean isEnabled() {
        return conf.wantRecommenderRSI();
    }
}

