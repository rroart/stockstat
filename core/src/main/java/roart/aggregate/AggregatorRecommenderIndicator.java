package roart.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import roart.calculate.CalcComplexNode;
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
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.TaUtil;

public class AggregatorRecommenderIndicator extends Aggregator {

    //Map<String, Object[]> macdMap;
    Map<String, Double[]> listMap;
    Map<String, Object[]> objectMap;
    Map<String, Double[]> resultMap;
    
    public AggregatorRecommenderIndicator(MyMyConfig conf, String index, List<StockItem> stocks, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, Category[] categories, Pipeline[] datareaders) throws Exception {
        super(conf, index, 0);
       Category cat = IndicatorUtils.getWantedCategory(categories);
        Map<String, Indicator> usedIndicatorMap = cat.getIndicatorMap();
        Map<String, Map<String, Object>> localResultMap = cat.getIndicatorLocalResultMap();
        Map<String, Double[]> list0 = (Map<String, Double[]>) localResultMap.get(localResultMap.keySet().iterator().next()).get(PipelineConstants.LIST);
 
        Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
        Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders);
        Map<String, Indicator> indicatorMap = new HashMap<>();
        int category = Constants.PRICECOLUMN;
        Set<String> ids = new HashSet<>();
        ids.addAll(list0.keySet());
        List<String> keys = new ArrayList<>();
        Map<String, Indicator> newIndicatorMap = new HashMap<>();

        for (String type : usedRecommenders.keySet()) {
            List<Recommend> list = usedRecommenders.get(type);
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                if (indicator != null) {
                    Indicator newIndicator = recommend.getIndicator(marketdatamap, category, newIndicatorMap, usedIndicatorMap, datareaders);
                    if (newIndicator != null) {
                        indicatorMap.put(indicator, newIndicator);
                    }
                }
                // TODO fix
                Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getIndicatorLocalResultMap().get(indicator).get(PipelineConstants.LIST);
                ids.retainAll(aResult.keySet());
            }
        }
        Map<String, Double[]> result = new HashMap<>();
        for (String id : ids) {
            Double[] arrayResult = new Double[0];
            for (String type : usedRecommenders.keySet()) {
                List<Recommend> list = usedRecommenders.get(type);
                for (Recommend recommend : list) {
                    String indicator = recommend.indicator();
                    Map<String, Double[][]> listMap = (Map<String, Double[][]>) cat.getIndicatorLocalResultMap().get(indicator).get(PipelineConstants.LIST);
                    Double[][] aResult = listMap.get(id);
                    arrayResult = (Double[]) ArrayUtils.addAll(arrayResult, aResult[0]);
                }
            }
            result.put(id, arrayResult);
        }
        TaUtil tu = new TaUtil();
        for (String recommender : usedRecommenders.keySet()) {
        //for (String type : indicatorMap.keySet()) {
            List<Indicator> indicators = Recommend.getIndicators(recommender, usedRecommenders, indicatorMap);
            //indicators.add(indicatorMap.get(type));
            // We just want the config, any in the list will do
            Recommend recommend = usedRecommenders.get(recommender).get(0);
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, recommend.getFutureDays(), conf.getTableDays(), recommend.getIntervalDays());
            Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
            //Map<String, Double[]> indicatorMap = dayIndicatorMap.get(j);
            // find recommendations
            int macdlen = conf.getTableDays();
            int listlen = conf.getTableDays();
            double recommendValue = 0;
            //transform(conf, buyList);
            resultMap = new HashMap<>();
            for (String id : ids) {
                Double[] aResult = new Double[2]; 
                System.out.println(result.get(id)[0]);
                System.out.println(result.get(id).getClass().getName());
                Double[] mergedResult = (Double[]) result.get(id);
                for (int i = 0; i < keys.size(); i++) {
                    String key = keys.get(i);
                    // TODO temp fix
                    CalcNode node = (CalcNode) conf.configValueMap.get(key);
                    //node.setDoBuy(useMax);
                    double value = mergedResult[i];
                    recommendValue += node.calc(value, 0);
                }
                aResult[0] = recommendValue;
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
        return resultMap.get(stock.getId());
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        headrow.add("Ag22"+title);
        headrow.add("Ag23"+title);
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] obj = resultMap.get(stock.getId());
        Object val = null;
        if (obj != null) {
            val = obj[0];
        }
        row.addarr(obj);
    }

}
