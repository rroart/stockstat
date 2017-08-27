package roart.aggregate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.config.MyMyConfig;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.util.ArraysUtil;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;

public class DataReader extends Pipeline {
    
    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;
    Map<String, Double[]> listMap;
    List<Date> dateList;
    Map<String, double[]> truncListMap;

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
        map.put(PipelineConstants.DATELIST, dateList);
        map.put(PipelineConstants.TRUNCLIST, truncListMap);
        return map;
    }
    
    public DataReader(MyMyConfig conf, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, int category) throws Exception {
        super(conf, category);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        readData(conf, marketdatamap, category);        
    }

    // TODO make an oo version of this
    private void readData(MyMyConfig conf, Map<String, MarketData> marketdatamap, int category) throws Exception {
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        // note that there are nulls in the lists with sparse
        this.listMap = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        this.dateList = StockDao.getDateList(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        this.truncListMap = ArraysUtil.getTruncList(this.listMap);

        if (conf.wantPercentizedPriceIndex()) {
            
        }
    }

    @Override
    public String pipelineName() {
        return PipelineConstants.DATAREADER;
    }
}
