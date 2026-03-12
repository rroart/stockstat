package roart.pipeline.impl;

import java.util.*;

import roart.common.pipeline.data.*;
import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
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

    private Map<String, Long[]> volumeMap;
    private Map<String, String> currencyMap;
    
    @Override
    public SerialPipeline putData() {
        SerialPipeline list = getData();
        if (categoryTitle == null) {
            int jj = 0;
        }
        //map.setName(categoryTitle);
        list.add(new PipelineData(categoryTitle, PipelineConstants.LIST, null, new SerialMapDD(listMap)));
        list.add(new PipelineData(categoryTitle, PipelineConstants.VOLUME, null, new SerialMapL(volumeMap)));
        list.add(new PipelineData(categoryTitle, PipelineConstants.CURRENCY, null, new SerialMapPlain(currencyMap)));
        list.add(new PipelineData(categoryTitle, PipelineConstants.FILLLIST, null, new SerialMapDD(fillListMap)));
        list.add(new PipelineData(categoryTitle, PipelineConstants.BASE100LIST, null, new SerialMapDD(base100ListMap)));
        list.add(new PipelineData(categoryTitle, PipelineConstants.TRUNCFILLLIST, null, new SerialMapdd(truncFillListMap)));
        list.add(new PipelineData(categoryTitle, PipelineConstants.TRUNCBASE100LIST, null, new SerialMapdd(truncBase100ListMap)));
        list.add(new PipelineData(categoryTitle, PipelineConstants.TRUNCBASE100FILLLIST, null, new SerialMapdd(truncBase100FillListMap)));
        list.add(new PipelineData(categoryTitle, PipelineConstants.NAME, null, new SerialMapPlain(nameMap)));
        list.add(new PipelineData(categoryTitle, PipelineConstants.DATELIST, null, new SerialListPlain(dateList)));
        list.add(new PipelineData(categoryTitle, PipelineConstants.CATEGORYTITLE, null, new SerialString(categoryTitle))); // TODO
        return list;
    }
    
    public DataReader(IclijConfig conf, int category) {
        super(conf, category);
        categoryTitle = Constants.EXTRA;
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
            currentYear = MetaUtil.currentYear(marketData.meta, categoryTitle);
        }
        if (category == Constants.INDEXVALUECOLUMN) {
            categoryTitle = CategoryConstants.INDEX;
        }
        if (category == Constants.PRICECOLUMN) {
            categoryTitle = Constants.PRICE;
        }
        if (category == Constants.EXTRACOLUMN) {
            categoryTitle = Constants.EXTRA;
        }
        // TODO valid error
        this.dateList = StockDao.getDateList(market, marketdatamap);
        log.info("Stockdates {} {} {}", conf.getConfigData().getMarket(), conf.getConfigData().getDate(), this.dateList.size());
        //this.dateStringList = StockDao.getDateList(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        this.nameMap = StockDao.getNameMap(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        if (category == Constants.PRICECOLUMN) {
            this.volumeMap = DatelistToMapETL.getVolumes2(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, currentYear);
            this.currencyMap = DatelistToMapETL.getCurrencies(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, currentYear);
        }
        this.listMap = DatelistToMapETL.getArrSparse(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, currentYear);
        calculateOtherListMaps(conf, category, marketData);
    }

    void calculateOtherListMaps(IclijConfig conf, int category, MarketData marketData) {
        ValueETL.zeroPrice(this.listMap, category);
        if (!anythingHere(this.listMap)) {
            this.volumeMap = null;
            this.listMap = null;
            return;
        }
        this.listMap = ValueETL.abnormalChange(this.listMap, conf);
        if (MetaUtil.normalPeriod(marketData, category, categoryTitle)) {
        this.fillListMap = ValueETL.getReverseArrSparseFillHolesArr(conf, listMap);
        this.truncListMap = ArraysUtil.getTruncListArr(this.listMap);
        this.truncFillListMap = ArraysUtil.getTruncListArr(this.fillListMap);
        }
        if (conf.wantPercentizedPriceIndex() && MetaUtil.normalPeriod(marketData, category, categoryTitle)) {
            this.base100ListMap = ValueETL.getBase100D(this.listMap);
            this.base100FillListMap = ValueETL.getBase100D(this.fillListMap);
            this.truncBase100ListMap = ValueETL.getBase100(this.truncListMap);
            this.truncBase100FillListMap = ValueETL.getBase100(this.truncFillListMap);
        } else {
            int jj = 0;
        }
    }

    @Override
    public String pipelineName() {
        return "" + this.category;
    }
    
    private boolean anythingHere(Map<String, Double[][]> listMap) {
        if (listMap == null) {
            return false;
        }
        for (Double[][] array : listMap.values()) {
            for (int i = 0; i < array[0].length; i++) {
                if (array[0][i] != null) {
                    return true;
                }
            }
        }
        return false;
    }


}
