package roart.recommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.model.StockItem;
import roart.util.StockDao;

public class MACDRecommend extends BuySellRecommend {
    
    // TODO add deltadays?
    
    public  List<String> getBuyList() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAM);
        buyList.add(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAMDELTA);
        buyList.add(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUYMOMENTUM);
        buyList.add(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUGMOMENTUMDELTA);
        return buyList;
    }

    public  List<String> getSellList() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAM);
        sellList.add(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAMDELTA);
        sellList.add(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUM);
        sellList.add(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUMDELTA);
        return sellList;
    }

    public static void addToLists(List<Double> macdLists[], Double[] momentum) throws Exception {
        {
            for (int i = 0; i < macdLists.length; i ++) {
                List<Double> macdList = macdLists[i];
                if (momentum[i] != null) {
                    macdList.add(momentum[i]);
                }
            }
        }
    }

    public void getBuySellRecommendations(Map<String, Double> buyMap, Map<String, Double> sellMap, MyConfig conf, List<Double> macdLists[] /*,List<Double> macdList, List<Double> histList, List<Double> macdDList,
            List<Double> histDList*/, Map<String, Double[]> listMap, Map<String, Double[]> momMap, List<String> buyList, List<String> sellList) {
        int len = macdLists.length;
        Double macdMax[] = new Double[len];
        Double macdMin[] = new Double[len];
        for (int i = 0; i < len; i ++) {
            List<Double> macdList = macdLists[i];
            if (!macdList.isEmpty()) {
                macdMax[i] = Collections.max(macdList);
                macdMin[i] = Collections.min(macdList);
            } else {
                macdMax[i] = 0.0;
                macdMin[i] = 0.0;
            }
        }
        // find recommendations
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            if (momentum == null || momentum[0] == null || momentum[1] == null || momentum[2] == null || momentum[3] == null) {
                continue;
            }
            // this is the histogram 0
            if (momentum[0] >= 0 && buyMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(buyList.get(0)))*(macdMax[0] - momentum[0])/macdMax[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(buyList.get(i)))*(momentum[i])/macdMax[i];
                }
                buyMap.put(id, recommend);
            }
            if (momentum[0] < 0 && sellMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(sellList.get(0)))*(macdMin[0] - momentum[0])/macdMin[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(sellList.get(i)))*(momentum[i])/macdMin[i];
                }
                sellMap.put(id, recommend);
            }
        }
    }
    
    public static void getBuySellRecommendations2(Map<String, Double> buyMap, Map<String, Double> sellMap, MyConfig conf, List<Double> macdLists[] /*,List<Double> macdList, List<Double> histList, List<Double> macdDList,
            List<Double> histDList*/, Map<String, Double[]> listMap, Map<String, Double[]> momMap, List<String> buyList, List<String> sellList) {
        int len = macdLists.length;
        Double macdMax[] = new Double[len];
        Double macdMin[] = new Double[len];
        for (int i = 0; i < len; i ++) {
            List<Double> macdList = macdLists[i];
            if (!macdList.isEmpty()) {
                macdMax[i] = Collections.max(macdList);
                macdMin[i] = Collections.min(macdList);
            } else {
                macdMax[i] = 0.0;
                macdMin[i] = 0.0;
            }
        }
        // find recommendations
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            if (momentum == null || momentum[0] == null || momentum[1] == null || momentum[2] == null || momentum[3] == null) {
                continue;
            }
            // this is the histogram 0
            if (momentum[0] >= 0 && buyMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(buyList.get(0)))*(macdMax[0] - momentum[0])/macdMax[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(buyList.get(i)))*(momentum[i])/macdMax[i];
                }
                buyMap.put(id, recommend);
            }
            if (momentum[0] < 0 && sellMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(sellList.get(0)))*(macdMin[0] - momentum[0])/macdMin[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(sellList.get(i)))*(momentum[i])/macdMin[i];
                }
                sellMap.put(id, recommend);
            }
        }
    }
    
    public static double getQuality(boolean buy, Map<String, Double> buySellMap, Map<String, Double[]> listMap, int curlistidx,
            int newlistidx) {
        double score = 0;
        //System.out.println("buySellMap" + buySellMap.size());
        for (String id : buySellMap.keySet()) {
            Double recommend = buySellMap.get(id);
            Double[] list = listMap.get(id);
            Double newVal = list[newlistidx];
            Double curVal = list[curlistidx];
            //System.out.println("vals " + newVal + "  " + curVal);
            if (newVal != null && curVal != null) {
                double addScore;
                if (buy) {
                    addScore = (newVal / curVal) * recommend / 100;
                    score += addScore;
                } else {
                    addScore = (curVal / newVal) * recommend / 100;
                    score += addScore;                   
                }
                if (false && addScore > 100) {
                    System.out.println("too high " + addScore + " " + newVal + " " + curVal + " " + recommend);
                }
            }
        }
        //System.out.println("score"+score);
        return score;
    }

    @Override
    public void mutate(Map<String, Object> configValueMap, List<String> keys) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getRandom(Map<String, Object> configValueMap, List<String> keys) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void transform(MyConfig newConf, List<String> keys)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void normalize(Map<String, Object> configValueMap, List<String> keys) {
        // TODO Auto-generated method stub
        
    }


}
