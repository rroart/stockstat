package roart.recommender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.model.StockItem;
import roart.util.MarketData;
import roart.util.StockDao;

public class MACDRecommend {
    
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

    public static void addToLists(Map<String, MarketData> marketdatamap, int category, List<Double> macdLists[] /*List<Double> macdList,
            List<Double> histList, List<Double> macdDList, List<Double> histDList*/, String market, Double[] momentum) throws Exception {
        {
            for (int i = 0; i < macdLists.length; i ++) {
                List<Double> macdList = macdLists[i];
                if (momentum[i] != null) {
                    macdList.add(momentum[i]);
                }
            }
            /*
            if (momentum[0] != null) {
                histList.add(momentum[0]);
            }
            if (momentum[1] != null) {
                histDList.add(momentum[1]);
            }
            if (momentum[2] != null) {
                macdList.add(momentum[2]);
            }
            if (momentum[3] != null) {
                macdDList.add(momentum[3]);
            }
            */
            //System.out.println("outout2 " + Arrays.toString(sig));
        }
    }

    public static void getBuySellRecommendations(Map<String, Double> buyMap, Map<String, Double> sellMap, MyConfig conf, List<Double> macdLists[] /*,List<Double> macdList, List<Double> histList, List<Double> macdDList,
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
        /*
        Double maxhist = 0.0;
        Double maxdhist = 0.0;
        Double maxmacd = 0.0;
        Double maxdmacd = 0.0;
        Double minhist = 0.0;
        Double mindhist = 0.0;
        Double minmacd = 0.0;
        Double mindmacd = 0.0;
        log.info("listsize" + histList.size());
        log.info("listsize" + histDList.size());
        log.info("listsize" + macdList.size());
        log.info("listsize" + macdDList.size());
        if (!histList.isEmpty()) {
            maxhist = Collections.max(histList);
        }
        if (!histDList.isEmpty()) {
            maxdhist = Collections.max(histDList);
        }
        if (!macdList.isEmpty()) {
            maxmacd = Collections.max(macdList);
        }
        if (!macdDList.isEmpty()) {
            maxdmacd = Collections.max(macdDList);
        }
        if (!histList.isEmpty()) {
            minhist = Collections.min(histList);
        }
        if (!histDList.isEmpty()) {
            mindhist = Collections.min(histDList);
        }
        if (!macdList.isEmpty()) {
            minmacd = Collections.min(macdList);
        }
        if (!macdDList.isEmpty()) {
            mindmacd = Collections.min(macdDList);
        }
        */
        // find recommendations
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            if (momentum == null || momentum[0] == null || momentum[1] == null || momentum[2] == null || momentum[3] == null) {
                continue;
            }
            double hist = momentum[0];
            double histd = momentum[1];
            double macd = momentum[2];
            double macdd = momentum[3];
            // this is the histogram 0
            if (momentum[0] >= 0 && buyMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(buyList.get(0)))*(macdMax[0] - momentum[0])/macdMax[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(buyList.get(i)))*(momentum[i])/macdMax[i];
                }
                /*
                if (histd >= 0) {
                    recommend += conf.weightBuyHistDelta()*(histd)/maxdhist;
                }
                if (macd >= 0) {
                    recommend += conf.weightBuyMacd()*(macd)/maxmacd;
                }
                if (macdd >= 0) {
                    recommend += conf.weightBuyMacdDelta()*(macdd)/maxdmacd;
                }
                */
                buyMap.put(id, recommend);
            }
            if (momentum[0] < 0 && sellMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(sellList.get(0)))*(macdMin[0] - momentum[0])/macdMin[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(sellList.get(i)))*(momentum[i])/macdMin[i];
                }
                /*
                double recommend = conf.weightSellHist()*(minhist - hist)/minhist;
                if (histd < 0) {
                    recommend += conf.weightSellHistDelta()*(histd)/mindhist;
                }
                if (macd < 0) {
                    recommend += conf.weightSellMacd()*(macd)/minmacd;
                }
                if (macdd < 0) {
                    recommend += conf.weightSellMacdDelta()*(macdd)/mindmacd;
                }
                */
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
                if (buy) {
                    score += (newVal / curVal - 1) / recommend;
                } else {
                    score += (1 - newVal / curVal) / recommend;
                }
            }
        }
        //System.out.println("score"+score);
        return score;
    }


}
