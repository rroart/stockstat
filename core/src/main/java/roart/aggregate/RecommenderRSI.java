package roart.aggregate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.category.Category;
import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.recommender.RSIRecommend;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;

public class RecommenderRSI extends Aggregator {

    Map<String, Object[]> rsiMap;
    Map<String, Double> buyMap;
    Map<String, Double> sellMap;
    Map<String, Double[]> listMap;
    
    public RecommenderRSI(MyMyConfig conf, String index, List<StockItem> stocks, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, Category[] categories) throws Exception {
        super(conf, index, 0);
         SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        this.listMap = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap);
     String wanted = "Price";
        Category cat = null;
        for (Category category : categories) {
            if (category.getTitle().equals(wanted)) {
                cat = category;
                break;
            }
        }
        List<Double> rsiLists[] = new ArrayList[2];
        for (int i = 0; i < 2; i ++) {
            rsiLists[i] = new ArrayList<>();
        }
       Object rsi = cat.getResultMap().get("RSI");
        rsiMap = (Map<String, Object[]>) rsi;
        List<String> buyList = new RSIRecommend().getBuyList();
        List<String> sellList = new RSIRecommend().getSellList();
        for (String id : listMap.keySet()) {
       // TODO not yet RSIRecommend.getBuySellRecommendations(buyMap, sellMap, conf, rsiLists, listMap, rsiMap, buyList, sellList);
        if (conf.wantRSIScore() && rsi != null) {
            String market = "tradcomm";
            Double[] rsiA = (Double[]) rsiMap.get(id);
            RSIRecommend.addToLists(marketdatamap, category, rsiLists, market, rsiA);
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
        headrow.add("Ag2"+"buy");
        headrow.add("Ag2"+"sell");
            }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] obj = rsiMap.get(stock.getId());
        Object val = null;
        if (obj != null) {
            val = obj[0];
        }
        row.add(val);
    }

}
