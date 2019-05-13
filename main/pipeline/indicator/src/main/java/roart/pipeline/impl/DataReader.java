package roart.pipeline.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.pipeline.Pipeline;
import roart.stockutil.MetaUtil;
import roart.stockutil.StockDao;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;

public class DataReader extends Pipeline {
    
    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    // listMap is as originally read
    // fill is with smaller holes filled, too big will zero out older parts (if price, remove 0)
    // trunc is with all nulls removed
    // base100 is a base100 version of it
    protected Map<String, Double[][]> listMap;
    protected Map<String, Double[][]> fillListMap;

    protected Map<String, double[][]> truncListMap;
    protected Map<String, double[][]> truncFillListMap;
    
    protected Map<String, Double[][]> base100ListMap;
    protected Map<String, Double[][]> base100FillListMap;
    
    protected Map<String, double[][]> truncBase100ListMap;
    protected Map<String, double[][]> truncBase100FillListMap;
    
    Map<String, String> nameMap;
    List<Date> dateList;
    String categoryTitle;
    
    @Override
    public Map<Integer, Map<String, Object>> getResultMap() {
        Map<Integer, Map<String, Object>> resultMap = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.LIST, listMap);
        resultMap.put(category, map);
        return resultMap;
    }
    
    @Override
    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.LIST, listMap);
        map.put(PipelineConstants.FILLLIST, listMap);
        map.put(PipelineConstants.BASE100LIST, base100ListMap);
        map.put(PipelineConstants.BASE100FILLLIST, base100FillListMap);
        map.put(PipelineConstants.TRUNCLIST, truncListMap);
        map.put(PipelineConstants.TRUNCFILLLIST, truncFillListMap);
        map.put(PipelineConstants.TRUNCBASE100LIST, truncBase100ListMap);
        map.put(PipelineConstants.TRUNCBASE100FILLLIST, truncBase100FillListMap);
        map.put(PipelineConstants.NAME, nameMap);
        map.put(PipelineConstants.DATELIST, dateList);
        map.put(PipelineConstants.CATEGORYTITLE, categoryTitle);
        return map;
    }
    
    public DataReader(MyMyConfig conf, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, int category) throws Exception {
        super(conf, category);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        readData(conf, marketdatamap, category);        
    }

    // TODO make an oo version of this
    private void readData(MyMyConfig conf, Map<String, MarketData> marketdatamap, int category) throws Exception {
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        MarketData marketData = marketdatamap.get(conf.getMarket());
        // note that there are nulls in the lists with sparse
        boolean currentYear = false;
        if (category >= 0) {
            categoryTitle = marketData.periodtext[category];
            currentYear = MetaUtil.currentYear(marketData, categoryTitle);
        }
        if (category == Constants.INDEXVALUECOLUMN) {
            categoryTitle = CategoryConstants.INDEX;
        }
        if (category == Constants.PRICECOLUMN) {
            categoryTitle = Constants.PRICE;
        }
        this.dateList = StockDao.getDateList(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        this.nameMap = StockDao.getNameMap(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        this.listMap = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, currentYear);
        zeroPrice(this.listMap, category);
        this.fillListMap = getReverseArrSparseFillHolesArr(conf, listMap);
        this.truncListMap = ArraysUtil.getTruncListArr(this.listMap);
        this.truncFillListMap = ArraysUtil.getTruncListArr(this.fillListMap);
        if (conf.wantPercentizedPriceIndex() && MetaUtil.normalPeriod(marketData, category, categoryTitle)) {
            this.base100ListMap = getBase100(this.listMap, categoryTitle);
            this.base100FillListMap = getBase100(this.fillListMap, categoryTitle);
            this.truncBase100ListMap = getBase100(this.truncListMap, categoryTitle, category);
            this.truncBase100FillListMap = getBase100(this.truncFillListMap, categoryTitle, category);
        }
    }

    private void zeroPrice(Map<String, Double[][]> aListMap, int category) {
        if (category == Constants.PRICECOLUMN || category == Constants.INDEXVALUECOLUMN) {
            for (Entry<String, Double[][]> entry : aListMap.entrySet()) {
                Double[][] value = entry.getValue();
                for(int i = 0; i < value[0].length; i++) {
                    if (value[0][i] != null && value[0][i] == 0) {
                        log.info("Value 0 for {}", entry.getKey());
                        value[0][i] = null;
                        value[1][i] = null;
                        value[2][i] = null;
                    }
                }
            }
        }
    }

    private Map<String, Double[][]> getBase100(Map<String, Double[][]> aListMap, String catTitle) {
        Map<String, Double[][]> aMap = new HashMap<>();
        for (Entry<String, Double[][]> entry : aListMap.entrySet()) {
            Double[][] value = entry.getValue();
            if (value != null) {
                Double[][] newValue = new Double[value.length][];
                for (int i = 0; i < value.length; i++) {
                    newValue[i] = ArraysUtil.getPercentizedPriceIndex(value[i]);
                }
                aMap.put(entry.getKey(), newValue);
            }
        }
        return aMap;
    }

    private Map<String, double[][]> getBase100(Map<String, double[][]> aListMap, String catTitle, int cat) {
        Map<String, double[][]> aMap = new HashMap<>();
        for (Entry<String, double[][]> entry : aListMap.entrySet()) {
            double[][] value = entry.getValue();
            if (value != null) {
                double[][] newValue = new double[value.length][];
                for (int i = 0; i < value.length; i++) {
                    newValue[i] = ArraysUtil.getPercentizedPriceIndex(value[i]);
                }
                aMap.put(entry.getKey(), newValue);
            }
        }
        return aMap;
    }

    @Override
    public String pipelineName() {
        return "" + this.category;
    }

    public static Map<String, Double[][]> getReverseArrSparseFillHolesArr(MyMyConfig conf, Map<String, Double[][]> listMap) {
        Map<String, Double[][]> retMap = /*getReverse*/(listMap);
        for (Entry<String, Double[][]> entry : listMap.entrySet()) {
            Double[][] array = entry.getValue();
            Double[][] newArray = new Double[array.length][];
            for (int i = 0; i < array.length; i ++) {
                newArray[i] = ArraysUtil.fixMapHoles(array[i], null, maxHoleNumber(conf));
            }
            retMap.put(entry.getKey(), newArray);
        }      
        return retMap;
    }

    public static Map<String, Double[]> getReverseArrSparseFillHoles(MyMyConfig conf, Map<String, Double[]> listMap) {
        Map<String, Double[]> retMap = /*getReverse*/(listMap);
        for (Entry<String, Double[]> entry : listMap.entrySet()) {
            retMap.put(entry.getKey(), ArraysUtil.fixMapHoles(entry.getValue(), null, maxHoleNumber(conf)));
        }      
        return retMap;
    }

    public static int maxHoleNumber(MyMyConfig conf) {
        return conf.getMaxHoles();
    }

}
