package roart.aggregate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.category.Category;
import roart.category.CategoryConstants;
import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.evaluation.RSIRecommend;
import roart.indicator.IndicatorUtils;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.pipeline.PipelineConstants;
import roart.util.ArraysUtil;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;

public class RecommenderRSI extends Aggregator {

    Map<String, Object[]> rsiMap;
    Map<String, Double> buyMap;
    Map<String, Double> sellMap;
    Map<String, Double[][]> listMap;
    Map<String, double[][]> truncListMap;
   
    public RecommenderRSI(MyMyConfig conf, String index, List<StockItem> stocks, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, Category[] categories) throws Exception {
        super(conf, index, 0);
         SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        this.listMap = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        this.truncListMap = ArraysUtil.getTruncListArr(this.listMap);
        Category cat = IndicatorUtils.getWantedCategory(categories);
        List<Double> rsiLists[] = new ArrayList[2];
        for (int i = 0; i < 2; i ++) {
            rsiLists[i] = new ArrayList<>();
        }
       Object rsi = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORRSI).get(PipelineConstants.RESULT);
        rsiMap = (Map<String, Object[]>) rsi;
        List<String> buyList = null;///new RSIRecommend().getBuyList();
        List<String> sellList = null;//new RSIRecommend().getSellList();
        for (String id : listMap.keySet()) {
       // TODO not yet RSIRecommend.getBuySellRecommendations(buyMap, sellMap, conf, rsiLists, listMap, rsiMap, buyList, sellList);
        if (conf.wantRSIScore() && rsi != null) {
            String market = null;
            Double[] rsiA = (Double[]) rsiMap.get(id);
            if (rsiA != null) {
            IndicatorUtils.addToLists(marketdatamap, category, rsiLists, market, rsiA);
            }
        }
        }
    }

    @Override
    public boolean isEnabled() {
        return conf.wantRecommenderRSI();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        Object[] fields = new Object[2];
        int retindex = 0;
        String id = stock.getId();
        Double buy = buyMap.get(id);
        fields[retindex++] = buy;
        Double sell = sellMap.get(id);
        fields[retindex++] = sell;
        return fields;
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        headrow.add("RSI " + Constants.WEBBR + "buy");
        headrow.add("RSI " + Constants.WEBBR + "sell");
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] obj = rsiMap.get(stock.getId());
        Object val = null;
        if (obj != null) {
            val = obj[0];
        }
        if (obj == null) {
            obj = new Object[2];
        }
        row.addarr(obj);
        //row.add(val);
    }
    
    @Override
    public String getName() {
        return PipelineConstants.RECOMMENDERRSI;
    }

}
