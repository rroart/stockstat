package roart.evaluation;

import java.util.ArrayList;
import java.util.List;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;

public abstract class RSIRecommend extends RecommendRSI {
    
    // TODO add deltadays?
    
    public RSIRecommend(MyMyConfig conf) {
        super(conf);
    }

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

    @Override
    public boolean isEnabled() {
        return conf.wantRecommenderRSI();
    }
}

