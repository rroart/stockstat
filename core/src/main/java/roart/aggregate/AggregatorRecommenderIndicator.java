package roart.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import roart.calculate.CalcMACDNode;
import roart.calculate.CalcNode;
import roart.category.Category;
import roart.category.CategoryConstants;
import roart.config.MyMyConfig;
import roart.evaluation.IndicatorEvaluation;
import roart.evaluation.Recommend;
import roart.indicator.Indicator;
import roart.indicator.IndicatorUtils;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.TaUtil;

public class AggregatorRecommenderIndicator extends Aggregator {

    Map<String, Object[]> macdMap;
    Map<String, Double[]> listMap;
    Map<String, Object[]> objectMap;
    Map<String, Double[]> resMap;
    
    public AggregatorRecommenderIndicator(MyMyConfig conf, String index, List<StockItem> stocks, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, Category[] categories) throws Exception {
        super(conf, index, 0);
       String wanted = CategoryConstants.PRICE;
        Category cat = null;
        for (Category category : categories) {
            if (category.getTitle().equals(wanted)) {
                cat = category;
                break;
            }
        }
        Object macd = cat.getResultMap().get("MACD");
        Map<String, Double[]> list0 = (Map<String, Double[]>) cat.getResultMap().get("LIST");
        Object object = cat.getResultMap().get("OBJECT");
        Object rsi = cat.getResultMap().get("RSI");
        macdMap = (Map<String, Object[]>) macd;
        Map<String, Object[]> rsiMap = (Map<String, Object[]>) rsi;

        Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
        Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders);
        Map<String, Indicator> indicatorMap = new HashMap<>();
        int category = Constants.PRICECOLUMN;
        Set<String> ids = new HashSet<>();
        ids.addAll(list0.keySet());
        List<String> keys = new ArrayList<>();
        for (String type : usedRecommenders.keySet()) {
            List<Recommend> list = usedRecommenders.get(type);
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getResultMap().get(indicator);
                ids.retainAll(aResult.keySet());
            }
        }
        Map<String, Object[]> result = new HashMap<>();
        for (String id : ids) {
            Object[] arrayResult = new Object[0];
            for (String type : usedRecommenders.keySet()) {
                List<Recommend> list = usedRecommenders.get(type);
                for (Recommend recommend : list) {
                    String indicator = recommend.indicator();
                    Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getResultMap().get(indicator);
                    arrayResult = (Object[]) ArrayUtils.addAll(arrayResult, aResult.get(id));
                }
            }
            result.put(id, arrayResult);
        }
        TaUtil tu = new TaUtil();
        for (String recommender : usedRecommenders.keySet()) {
        //for (String type : indicatorMap.keySet()) {
            List<Indicator> indicators = Recommend.getIndicators(recommender, usedRecommenders, indicatorMap);
            //indicators.add(indicatorMap.get(type));
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators);
            Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
            //Map<String, Double[]> indicatorMap = dayIndicatorMap.get(j);
            // find recommendations
            int macdlen = conf.getTableDays();
            int listlen = conf.getTableDays();
            double recommend = 0;
            //transform(conf, buyList);
            Map<String, Double[]> resultMap = new HashMap<>();
            for (String id : ids) {
                Double[] aResult = new Double[2]; 
                Double[] mergedResult = (Double[]) result.get(id);
                for (int i = 0; i < keys.size(); i++) {
                    String key = keys.get(i);
                    // TODO temp fix
                    CalcNode node = (CalcNode) conf.configValueMap.get(key);
                    //node.setDoBuy(useMax);
                    double value = mergedResult[i];
                    recommend += node.calc(value, 0);
                }
                aResult[0] = recommend;
                resultMap.put(id, aResult);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return conf.wantMACDRSIRecommender();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        return macdMap.get(stock.getId());
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        headrow.add("Ag"+title);
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] obj = macdMap.get(stock.getId());
        Object val = null;
        if (obj != null) {
            val = obj[0];
        }
        row.add(val);
    }

}
