package roart.aggregate;

import java.util.List;
import java.util.Map;

import roart.category.Category;
import roart.config.MyConfig;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.util.MarketData;
import roart.util.PeriodData;

public class AggregatorRecommenderMacdRsi extends Aggregator {

    Map<String, Object[]> macdMap;
    
    public AggregatorRecommenderMacdRsi(MyConfig conf, String index, List<StockItem> stocks, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, Category[] categories) {
        super(conf, index, 0);
       String wanted = "Price";
        Category cat = null;
        for (Category category : categories) {
            if (category.getTitle().equals(wanted)) {
                cat = category;
                break;
            }
        }
        Object macd = cat.getResultMap().get("MACD");
        Object rsi = cat.getResultMap().get("RSI");
        macdMap = (Map<String, Object[]>) macd;
        Map<String, Object[]> rsiMap = (Map<String, Object[]>) rsi;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantMACDRSIRecommender();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        return macdMap.get(stock.getId());
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        headrow.add("Ag"+title);
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] obj = macdMap.get(stock.getId());
        Object val = null;
        if (obj != null) {
            val = obj[0];
        }
        row.add(val);
    }

}
