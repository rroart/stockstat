package roart.aggregator.impl;

import java.util.List;
import java.util.Map;

import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.data.*;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.util.PipelineUtils;
import roart.pipeline.common.aggregate.Aggregator;
import roart.result.model.ResultItemTableRow;

public class MACDBase extends Aggregator {

    private Map<String, double[][]> listMap;

    protected Map<String, List<Object>> objectMap;

    private Map<String, Double[]> resultObject;

    public MACDBase(IclijConfig conf, String catName, String catName2, Integer cat, SerialPipeline datareaders, List<String> stockDates, Inmemory inmemory) {
        super(conf, "macdb", cat, inmemory);
        SerialPipeline macdmap = PipelineUtils.getPipelines(datareaders, PipelineConstants.INDICATORMACD, inmemory);
        if (macdmap.isEmpty()) {
            return;
        }
        SerialMapD resultObject2 = PipelineUtils.getResultMap(datareaders, PipelineConstants.INDICATORMACD, inmemory);

        this.resultObject = resultObject2.getMap();
        
        SerialPipeline datareader = PipelineUtils.getPipelines(datareaders, catName, inmemory);
        if (datareader.isEmpty()) {
            log.info("empty {}", category);
            return;
        }
        Map<String, Double[][]> aListMap = PipelineUtils.getPipelineValueAndsconvertMapDD(datareaders, catName, PipelineConstants.LIST, conf.wantsInmemoryPipelineBatchsize() > 0, inmemory);
        Map<String, double[][]> fillListMap = PipelineUtils.getPipelineValueAndsconvertMapdd(datareaders, catName, PipelineConstants.TRUNCFILLLIST, conf.wantsInmemoryPipelineBatchsize() > 0, inmemory);
        Map<String, double[][]>  base100FillListMap = PipelineUtils.getPipelineValueAndsconvertMapdd(datareaders, catName, PipelineConstants.TRUNCBASE100FILLLIST, conf.wantsInmemoryPipelineBatchsize() > 0, inmemory);
        this.listMap = fillListMap;
    }

    @Override
    public boolean isEnabled() {
        return conf.isMACDEnabled();
    }

    @Override
    public Object[] getResultItem(StockDTO stock) {
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
    public void addResultItem(ResultItemTableRow row, StockDTO stock) {
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
    public SerialPipeline putData() {
        SerialPipeline list = new SerialPipeline();
        //map.setName(getName());
        list.add(new PipelineData(getName(), PipelineConstants.CATEGORY, null, new SerialInteger(category), false));
        list.add(new PipelineData(getName(), PipelineConstants.CATEGORYTITLE, null, new SerialString(title), false));
        list.add(new PipelineData(getName(), PipelineConstants.RESULT, null, new SerialMapPlain(resultMap), true));
        return list;
    }

    @Override
    public void calculateMe(IclijConfig conf, SerialPipeline datareaders, NeuralNetCommand neuralnetcommand) throws Exception {

    }

    @Override
    public void cleanMLDaos() {

    }

}
