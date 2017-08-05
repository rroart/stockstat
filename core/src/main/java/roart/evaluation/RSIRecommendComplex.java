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

public class RSIRecommendComplex extends RecommendRSI {
    
    // TODO add deltadays?
    
    public RSIRecommendComplex(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public  List<String> getBuyList() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIWEIGHTSBUYRSINODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIWEIGHTSBUYRSIDELTANODE);
        return buyList;
    }

    @Override
    public  List<String> getSellList() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIWEIGHTSSELLRSINODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIWEIGHTSSELLRSIDELTANODE);
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

}

