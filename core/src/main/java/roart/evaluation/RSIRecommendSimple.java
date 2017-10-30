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

public class RSIRecommendSimple extends RecommendRSI {
    
    // TODO add deltadays?
    
    public RSIRecommendSimple(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public  List<String> getBuyList() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSIBUYWEIGHTRSI);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSIBUYWEIGHTRSIDELTA);
        return buyList;
    }

    @Override
    public  List<String> getSellList() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSISELLWEIGHTRSI);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSISELLWEIGHTRSIDELTA);
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

