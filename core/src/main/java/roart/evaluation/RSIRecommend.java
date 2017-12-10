package roart.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.config.MyMyConfig;

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
        //buyList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSBUY);
        //buyList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSBUYDELTA);
        return buyList;
    }

    @Override
    public  List<String> getSellList() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.INDICATORSRSIRECOMMENDSELLWEIGHT);
        sellList.add(ConfigConstants.INDICATORSRSIRECOMMENDSELLWEIGHTDELTA);
        //sellList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSSELL);
        //sellList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSSELLDELTA);
        return sellList;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantRecommenderRSI();
    }
}

