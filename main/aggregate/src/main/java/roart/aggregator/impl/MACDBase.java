package roart.aggregator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.category.AbstractCategory;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.indicator.util.IndicatorUtils;
import roart.pipeline.Pipeline;
import roart.pipeline.common.aggregate.Aggregator;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.StockUtil;

public class MACDBase extends Aggregator {

    private Map<String, double[][]> listMap;

    protected Map<String, List<Object>> objectMap;

    private Map<String, Double[]> resultObject;

    public MACDBase(IclijConfig conf, String catName, String catName2, Integer cat, AbstractCategory[] categories,
            Map<String, String> idNameMap, Pipeline[] datareaders) {
        super(conf, "macdb", cat);
        AbstractCategory cat2 = null;
        try {
            cat2 = StockUtil.getWantedCategory(categories, cat);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (cat2 == null) {
            return;
        }
        Object taObject = cat2.getResultMap().get(PipelineConstants.INDICATORMACDOBJECT);
        Map<String, Object> macdmap = cat2.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORMACD);
        if (macdmap == null) {
            return;
        }
        Object resultObject2 = macdmap.get(PipelineConstants.RESULT);

        this.resultObject = (Map<String, Double[]>) resultObject2;
        
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        Pipeline datareader = pipelineMap.get("" + category);
        if (datareader == null) {
            log.info("empty {}", category);
            return;
        }
        Map<String, Double[][]> aListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST);
        Map<String, double[][]> fillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCFILLLIST);
        Map<String, double[][]>  base100FillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100FILLLIST);
        this.listMap = fillListMap;
        
        /*
        Map<String, Object> resultMaps = maps.get(catName);
        if (resultMaps != null) {
            Map<String, Object> macdMaps = (Map<String, Object>) resultMaps.get(getPipeline());
            //System.out.println("macd"+ macdMaps.keySet());
            this.objectMap = (Map<String, List<Object>>) macdMaps.get(PipelineConstants.OBJECT);
        }
        */
    }

    @Override
    public boolean isEnabled() {
        return conf.isMACDEnabled();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        Object[] ret = new Object[3];
        if (resultObject != null) {
        String id = stock.getId();
        Double[] result = resultObject.get(id);
        double[] vals = listMap.get(id)[0];
        Double val = null;
        if (vals.length > 0) {
        val = vals[vals.length - 1];
        }
        if (result != null && val != null && val != 0) {
        if (result[0] != null) {
        ret[0] = result[0] / val;
        }
        if (result[2] != null) {
        ret[1] = result[2] / val;
        }
        if (result[4] != null) {
        ret[2] = result[4] / val;
        }
        }
        }
        return ret;
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] arrayResult = getResultItem(stock);
        arrayResult = round(arrayResult, 3);
        row.addarr(arrayResult);
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        headrow.add(title + Constants.WEBBR + "histb");
        headrow.add(title + Constants.WEBBR + "macdb");
        headrow.add(title + Constants.WEBBR + "sigb");
    }

    @Override
    public String getName() {
        return PipelineConstants.MACDBASE;
    }

    @Override
    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.CATEGORY, category);
        map.put(PipelineConstants.CATEGORYTITLE, title);
        map.put(PipelineConstants.RESULT, resultMap);
        return map;
    }
    
}
