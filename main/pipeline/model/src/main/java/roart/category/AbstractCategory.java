package roart.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.indicator.AbstractIndicator;
import roart.pipeline.Pipeline;
import roart.result.model.ResultItemTableRow;

public abstract class AbstractCategory {

    protected static Logger log = LoggerFactory.getLogger(AbstractCategory.class);

    private String title;
    protected IclijConfig conf;
    protected List<StockDTO> stocks;
    protected List<AbstractIndicator> indicators = new ArrayList<>();
    private Map<String, AbstractIndicator> indicatorMap = new HashMap<>();
    protected PipelineData[] datareaders;
    protected int period;
    protected Map<String, Object[]> resultMap;
    protected int dataArraySize;

    protected Inmemory inmemory;
   
    public AbstractCategory(IclijConfig conf, String periodText, List<StockDTO> stocks, PipelineData[] datareaders, Inmemory inmemory) {
        this.conf = conf;
        setTitle(periodText);
        this.stocks = stocks;
        this.datareaders = datareaders;
        this.inmemory = inmemory;
    }

    public int getPeriod() {
        return period;
    }
    
    public abstract void addResultItemTitle(ResultItemTableRow r);

    public abstract void addResultItem(ResultItemTableRow r, StockDTO stock);

    public static void mapAdder(Map<Integer, List<ResultItemTableRow>> map, Integer key, List<ResultItemTableRow> add) {
        List<ResultItemTableRow> val = map.computeIfAbsent(key, k -> new ArrayList<>());
        val.addAll(add);
    }

    public Map<Integer, List<ResultItemTableRow>> otherTables() {
        Map<Integer, List<ResultItemTableRow>> allTablesMap = new HashMap<>();
        for (AbstractIndicator indicator : indicators) {
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

    /*
    public List<AbstractPredictor> getPredictors() {
        return predictors;
    }

    public void setPredictors(List<AbstractPredictor> predictors) {
        this.predictors = predictors;
    }
    */

    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        for (AbstractIndicator indicator : indicators) {
            if (indicator.isEnabled()) {
                Map<String, Object> tmpMap = indicator.getResultMap();
                if (tmpMap != null) {
                    map.putAll(tmpMap);
                }
           }
        }
        return map;
    }
    
    public Map<String, PipelineData> putData() {
        Map<String, PipelineData> map = new HashMap<>();
        for (AbstractIndicator indicator : indicators) {
            if (indicator.isEnabled()) {
                PipelineData tmpMap = indicator.putData();
                if (tmpMap != null) {
                    log.debug("Adding indicator {}", indicator.indicatorName());
                    log.debug("exist {}", map.containsKey(indicator.indicatorName()));
                    map.put(indicator.indicatorName(), tmpMap);
                }
           }
        }
        return map;
    }
    
    protected void createIndicatorMap(String periodText) {
        for (AbstractIndicator indicator : indicators) {
            indicatorMap.put(indicator.indicatorName(), indicator);
        }
    }
    
    public Map<String, AbstractIndicator> getIndicatorMap() {
        return indicatorMap;
    }
    
    public abstract boolean hasContent() throws Exception;
    
    @Deprecated
    public Map<String, Object> getLocalResultMapNot() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.RESULT, resultMap);
        return map;
    }
    
    public abstract Double[] getData(StockDTO stock) throws Exception;
    
    public void createResultMap(IclijConfig conf, List<StockDTO> stocks) throws Exception {
        resultMap = new HashMap<>();
        dataArraySize = 0;
        if (stocks == null) {
            int jj = 0;
            return;
        }
        for (StockDTO stock : stocks) {
            Double[] value = getData(stock);
            resultMap.put(stock.getId(), value);
            for (int i = dataArraySize; i < value.length; i++) {
                if (value[i] != null) {
                    dataArraySize = i + 1;
                }
            }
        }
        if (dataArraySize == 3) {
            dataArraySize = 4;
        }
    }

    /*
    @Deprecated
    protected void addPredictor(AbstractPredictor predictor) {
        if (predictor.isEnabled()) {
            predictors.add(predictor);
        }
    }

    @Deprecated
    public void addPredictors(AbstractCategory[] categories) throws Exception {        
    }

    @Deprecated
    public void calculatePredictors(AbstractCategory[] categories) throws Exception {        
        for (AbstractCategory category : categories) {
            for (AbstractPredictor predictor : category.predictors) {
                predictor.calculate();
            }
        }
    }
*/

}

