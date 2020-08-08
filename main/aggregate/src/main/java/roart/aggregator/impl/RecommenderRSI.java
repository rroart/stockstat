package roart.aggregator.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.category.AbstractCategory;
import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.etl.DatelistToMapETL;
import roart.indicator.util.IndicatorUtils;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.model.StockItem;
import roart.result.model.ResultItemTableRow;
import roart.model.data.MarketData;
import roart.stockutil.StockDao;
import roart.pipeline.common.aggregate.Aggregator;

public class RecommenderRSI extends Aggregator {

    Map<String, Object[]> rsiMap;
    Map<String, Double> buyMap;
    Map<String, Double> sellMap;
    Map<String, Double[][]> listMap;
    Map<String, double[][]> truncListMap;

    public RecommenderRSI(MyMyConfig conf, String index, Map<String, MarketData> marketdatamap, AbstractCategory[] categories) throws Exception {
        super(conf, index, 0);
        if (!isEnabled()) {
            return;
        }
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        this.listMap = DatelistToMapETL.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        //this.truncListMap = ArraysUtil.getTruncListArr(this.listMap);
        AbstractCategory cat = IndicatorUtils.getWantedCategory(categories, marketdatamap.get(conf.getMarket()).meta);
        if (cat == null) {
            return;
        }
        List<Double> rsiLists[] = new ArrayList[2];
        for (int i = 0; i < 2; i ++) {
            rsiLists[i] = new ArrayList<>();
        }
        Object rsi = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORRSI).get(PipelineConstants.RESULT);
        if (rsi == null) {
            return;
        }
        rsiMap = (Map<String, Object[]>) rsi;
        List<String> buyList = null;///new RSIRecommend().getBuyList();
        List<String> sellList = null;//new RSIRecommend().getSellList();
        for (String id : listMap.keySet()) {
            // not yet RSIRecommend.getBuySellRecommendations(buyMap, sellMap, conf, rsiLists, listMap, rsiMap, buyList, sellList);
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
        Object[] obj = new Object[2];
        obj = round(obj, 3);
        if (rsiMap != null) {
            obj = rsiMap.get(stock.getId());
        }
        if (obj == null) {
            obj = new Object[2];
        }
        row.addarr(obj);
    }

    @Override
    public String getName() {
        return PipelineConstants.RECOMMENDERRSI;
    }

}
