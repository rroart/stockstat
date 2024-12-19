package roart.aggregator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialMapD;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.data.TwoDimD;
import roart.common.pipeline.data.TwoDimd;
import roart.common.util.PipelineUtils;
import roart.indicator.util.IndicatorUtils;
import roart.pipeline.Pipeline;
import roart.pipeline.common.aggregate.Aggregator;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.StockUtil;

public class MACDBase extends Aggregator {

    private Map<String, double[][]> listMap;

    protected Map<String, List<Object>> objectMap;

    private Map<String, Double[]> resultObject;

    public MACDBase(IclijConfig conf, String catName, String catName2, Integer cat, PipelineData[] datareaders, List<String> stockDates) {
        super(conf, "macdb", cat);
        PipelineData macdmap = PipelineUtils.getPipeline(datareaders, PipelineConstants.INDICATORMACD);
        if (macdmap == null) {
            return;
        }
        SerialMapD resultObject2 = PipelineUtils.getResultMap(macdmap);

        this.resultObject = resultObject2.getMap();
        
        Map<String, PipelineData> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        PipelineData datareader = pipelineMap.get(catName);
        if (datareader == null) {
            log.info("empty {}", category);
            return;
        }
        Map<String, Double[][]> aListMap = PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.LIST));
        Map<String, double[][]> fillListMap = PipelineUtils.sconvertMapdd(datareader.get(PipelineConstants.TRUNCFILLLIST));
        Map<String, double[][]>  base100FillListMap = PipelineUtils.sconvertMapdd(datareader.get(PipelineConstants.TRUNCBASE100FILLLIST));
        this.listMap = fillListMap;
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
        if (listMap.get(id) == null) {
            log.info("LM" + listMap.size() + " " + id);
        }
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
    public PipelineData putData() {
        PipelineData map = new PipelineData();
        map.setName(getName());
        map.put(PipelineConstants.CATEGORY, category);
        map.put(PipelineConstants.CATEGORYTITLE, title);
        map.put(PipelineConstants.RESULT, new SerialMapPlain(resultMap));
        return map;
    }
    
}
