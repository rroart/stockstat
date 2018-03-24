package roart.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.predictor.Predictor;
import roart.util.StockDao;
import roart.util.StockUtil;

public abstract class Category {

    protected static Logger log = LoggerFactory.getLogger(Category.class);

    private String title;
    protected MyMyConfig conf;
    protected List<StockItem> stocks;
    protected List<Indicator> indicators = new ArrayList<>();
    protected List<Predictor> predictors = new ArrayList<>();
    private Map<String, Indicator> indicatorMap = new HashMap<>();
    protected Pipeline[] datareaders;
    protected int period;
    protected Map<String, Object[]> resultMap;
    int dataArraySize;
   
    public Category(MyMyConfig conf, String periodText, List<StockItem> stocks, Pipeline[] datareaders) {
        this.conf = conf;
        setTitle(periodText);
        this.stocks = stocks;
        this.datareaders = datareaders;
    }

    public int getPeriod() {
        return period;
    }
    
    public abstract void addResultItemTitle(ResultItemTableRow r);

    public abstract void addResultItem(ResultItemTableRow r, StockItem stock);

    public static void mapAdder(Map<Integer, List<ResultItemTableRow>> map, Integer key, List<ResultItemTableRow> add) {
        List<ResultItemTableRow> val = map.computeIfAbsent(key, k -> new ArrayList<>());
        val.addAll(add);
    }

    public Map<Integer, List<ResultItemTableRow>> otherTables() {
        Map<Integer, List<ResultItemTableRow>> allTablesMap = new HashMap<>();
        for (Indicator indicator : indicators) {
            Map<Integer, List<ResultItemTableRow>> tables = indicator.otherTables();
            if (tables == null) {
                continue;
            }
            for (Entry<Integer, List<ResultItemTableRow>> entry : tables.entrySet()) {
                mapAdder(allTablesMap, entry.getKey(), entry.getValue());
            }
        }
        return allTablesMap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Predictor> getPredictors() {
        return predictors;
    }

    public void setPredictors(List<Predictor> predictors) {
        this.predictors = predictors;
    }

    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        for (Predictor predictor : predictors) {
            if (predictor.isEnabled()) {
                Map<String, Object> tmpMap = predictor.getResultMap();
                if (tmpMap != null) {
                    map.putAll(tmpMap);
                }
            }
        }
        for (Indicator indicator : indicators) {
            if (indicator.isEnabled()) {
                Map<String, Object> tmpMap = indicator.getResultMap();
                if (tmpMap != null) {
                    map.putAll(tmpMap);
                }
           }
        }
        return map;
    }
    
    public Map<String, Map<String, Object>> getIndicatorLocalResultMap() {
        Map<String, Map<String, Object>> map = new HashMap<>();
        for (Predictor predictor : predictors) {
            if (predictor.isEnabled()) {
                Map<String, Object> tmpMap = predictor.getLocalResultMap();
                if (tmpMap != null) {
                    System.out.println("Adding predictor " + predictor.predictorName());
                    System.out.println("exist " + map.containsKey(predictor.predictorName()));
                    map.put(predictor.predictorName(), tmpMap);
                }
            }
        }
        for (Indicator indicator : indicators) {
            if (indicator.isEnabled()) {
                Map<String, Object> tmpMap = indicator.getLocalResultMap();
                if (tmpMap != null) {
                    System.out.println("Adding indicator " + indicator.indicatorName());
                    System.out.println("exist " + map.containsKey(indicator.indicatorName()));
                    map.put(indicator.indicatorName(), tmpMap);
                }
           }
        }
        return map;
    }
    
    protected void createIndicatorMap(String periodText) {
        for (Indicator indicator : indicators) {
            indicatorMap.put(indicator.indicatorName(), indicator);
        }
    }
    
    public Map<String, Indicator> getIndicatorMap() {
        return indicatorMap;
    }
    
    public boolean hasContent() throws Exception {
        return StockUtil.hasStockValue(stocks, period);
    }
    
    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.RESULT, resultMap);
        return map;
    }
    
    public Double[] getData(StockItem stock) throws Exception {
        return StockDao.getValue(stock, period);
    }
    
    public void createResultMap(MyMyConfig conf, List<StockItem> stocks) throws Exception {
        resultMap = new HashMap<>();
        dataArraySize = 0;
        for (StockItem stock : stocks) {
            Double[] value = getData(stock);
            resultMap.put(stock.getId(), value);
            for (int i = dataArraySize; i < value.length; i++) {
                if (value[i] != null) {
                    dataArraySize = i + 1;
                }
            }
        }
    }

}

