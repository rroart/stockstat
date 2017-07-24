package roart.evolution;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.MyMyConfig;
import roart.recommender.BuySellRecommend;
import roart.recommender.MACDRecommend;

public class FitnessBuySellMACD {
    MyMyConfig conf;
    Map<Integer, Map<String, Double[]>> dayMomMap;
    Map<Integer, List<Double>[]> dayMacdListsMap;
    Map<Integer, Map<String, Double[]>> dayRsiMap;
    Map<Integer, List<Double>[]> dayRsiListsMap;
    int macdlen;
    int listlen;
    Map<String, Double[]> listMap;
    BuySellRecommend recommend;
    
    public FitnessBuySellMACD(MyMyConfig conf, Map<Integer, Map<String, Double[]>>dayMomMap, Map<Integer, List<Double>[]> dayMacdListsMap, Map<Integer, Map<String, Double[]>> dayRsiMap, Map<Integer, List<Double>[]> dayRsiListsMap, Map<String, Double[]> listMap, BuySellRecommend recommend) {
        this.conf = conf;
        this.macdlen = conf.getTableDays();
        this.listlen = conf.getTableDays();
        this.dayMacdListsMap = dayMacdListsMap;
        this.dayMomMap = dayMomMap;
        this.dayRsiListsMap = dayRsiListsMap;
        this.dayRsiMap = dayRsiMap;
        this.listMap = listMap;
        this.recommend = recommend;
    }
    
    public double getScores(boolean buy, MyMyConfig testBuySellConfig, List<String> buySellList) throws JsonParseException, JsonMappingException, IOException {
        double testRecommendQualBuySell = 0;
        for (int j = conf.getTestRecommendFutureDays(); j < macdlen; j += conf.getTestRecommendIntervalDays()) {
            List<Double> macdLists[] = dayMacdListsMap.get(j);
            //int newmacdidx = macdlen - 1 - j + conf.getTestRecommendFutureDays();
            //int curmacdidx = macdlen - 1 - j;
            Map<String, Double[]> momMap = dayMomMap.get(j);
            Map<String, Double[]> rsiMap = dayRsiMap.get(j);
            //System.out.println("j"+j);
            Map<String, Double> buysellMap = new HashMap<>();
            if (buy) {
            recommend.getBuySellRecommendations(buysellMap, null, testBuySellConfig, macdLists, listMap, momMap, rsiMap, buySellList, null);
            } else {
            recommend.getBuySellRecommendations(null, buysellMap, testBuySellConfig, macdLists, listMap, momMap, rsiMap, null, buySellList);
            }
            int newlistidx = listlen - 1 - j + conf.getTestRecommendFutureDays();
            int curlistidx = listlen - 1 - j;
            testRecommendQualBuySell += MACDRecommend.getQuality(buy, buysellMap, listMap, curlistidx, newlistidx);
        }
        return testRecommendQualBuySell;
    }

    
}
