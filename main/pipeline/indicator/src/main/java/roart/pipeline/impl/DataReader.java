package roart.pipeline.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.pipeline.Pipeline;
import roart.stockutil.MetaUtil;
import roart.stockutil.StockDao;
import roart.model.data.MarketData;
import roart.etl.DatelistToMapETL;
import roart.etl.ValueETL;

public class DataReader extends Pipeline {
    
    private Map<String, MarketData> marketdatamap;

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
        map.put(PipelineConstants.FILLLIST, fillListMap);
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
    
    public DataReader(MyMyConfig conf, Map<String, MarketData> marketdatamap, int category) throws Exception {
        super(conf, category);
        this.setMarketdatamap(marketdatamap);
        readData(conf, marketdatamap, category);        
    }

    public Map<String, MarketData> getMarketdatamap() {
        return marketdatamap;
    }

    public void setMarketdatamap(Map<String, MarketData> marketdatamap) {
        this.marketdatamap = marketdatamap;
    }

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
        this.listMap = DatelistToMapETL.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, currentYear);
        Double[][] e = listMap.get("F00000ZHEV");
        if (e != null) {
            int jj = 0;
        }
        ValueETL.zeroPrice(this.listMap, category);
        this.fillListMap = ValueETL.getReverseArrSparseFillHolesArr(conf, listMap);
        this.truncListMap = ArraysUtil.getTruncListArr(this.listMap);
        this.truncFillListMap = ArraysUtil.getTruncListArr(this.fillListMap);
        if (conf.wantPercentizedPriceIndex() && MetaUtil.normalPeriod(marketData, category, categoryTitle)) {
            this.base100ListMap = ValueETL.getBase100D(this.listMap);
            this.base100FillListMap = ValueETL.getBase100D(this.fillListMap);
            this.truncBase100ListMap = ValueETL.getBase100(this.truncListMap);
            this.truncBase100FillListMap = ValueETL.getBase100(this.truncFillListMap);
        }
        Double[][] f = listMap.get("F00000ZHEV");
        if (f != null) {
            int jj = 0;
        }
    }

    @Override
    public String pipelineName() {
        return "" + this.category;
    }

}
