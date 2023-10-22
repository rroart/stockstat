package roart.pipeline.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.ArraysUtil;
import roart.common.util.TimeUtil;
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
    List<String> dateList;
    String categoryTitle;

    private Map<String, Object[][]> volumeMap;
    
    @Override
    public PipelineData putData() {
        PipelineData map = getData();
        map.put(PipelineConstants.LIST, listMap);
        map.put(PipelineConstants.VOLUME, volumeMap);
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
    
    public DataReader(IclijConfig conf, int category) {
        super(conf, category);
    }
    
    public DataReader(IclijConfig conf, Map<String, MarketData> marketdatamap, int category, String market) throws Exception {
        super(conf, category);
        this.setMarketdatamap(marketdatamap);
        readData(conf, marketdatamap, category, market);        
    }

    public Map<String, MarketData> getMarketdatamap() {
        return marketdatamap;
    }

    public void setMarketdatamap(Map<String, MarketData> marketdatamap) {
        this.marketdatamap = marketdatamap;
    }

    private void readData(IclijConfig conf, Map<String, MarketData> marketdatamap, int category, String market) throws Exception {
        String dateme = TimeUtil.format(conf.getConfigData().getDate());
        MarketData marketData = marketdatamap.get(market);
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
        this.dateList = StockDao.getDateList(market, marketdatamap);
        //this.dateStringList = StockDao.getDateList(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        this.nameMap = StockDao.getNameMap(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        if (category == Constants.PRICECOLUMN) {
            this.volumeMap = DatelistToMapETL.getVolumes(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, currentYear);
        }
        this.listMap = DatelistToMapETL.getArrSparse(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, currentYear);
        calculateOtherListMaps(conf, category, marketData);
    }

    void calculateOtherListMaps(IclijConfig conf, int category, MarketData marketData) {
        ValueETL.zeroPrice(this.listMap, category);
        this.listMap = ValueETL.abnormalChange(this.listMap, conf);
        this.fillListMap = ValueETL.getReverseArrSparseFillHolesArr(conf, listMap);
        this.truncListMap = ArraysUtil.getTruncListArr(this.listMap);
        this.truncFillListMap = ArraysUtil.getTruncListArr(this.fillListMap);
        if (conf.wantPercentizedPriceIndex() && MetaUtil.normalPeriod(marketData, category, categoryTitle)) {
            this.base100ListMap = ValueETL.getBase100D(this.listMap);
            this.base100FillListMap = ValueETL.getBase100D(this.fillListMap);
            this.truncBase100ListMap = ValueETL.getBase100(this.truncListMap);
            this.truncBase100FillListMap = ValueETL.getBase100(this.truncFillListMap);
        }
    }

    @Override
    public String pipelineName() {
        return "" + this.category;
    }

}
