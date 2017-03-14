package roart.category;

import java.util.List;
import java.util.Map;

import roart.indicator.Indicator;
import roart.model.ResultItem;
import roart.model.Stock;
import roart.service.ControlService;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.StockUtil;

public class CategoryPrice extends Category {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;

    public CategoryPrice(ControlService controlService, String string, List<Stock> stocks,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap,
            Map<String, Integer>[] periodmap) {
        super(controlService, string, stocks);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
    }

    @Override
    public void addResultItemTitle(ResultItem ri) {
        try {
            if (StockUtil.hasSpecial(stocks, Constants.PRICE)) {
                ri.add(title);
                for (Indicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        ri.add(indicator.getResultItemTitle());
                    }
                }
                ri.add("Currency");
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Override
    public void addResultItem(ResultItem ri, Stock stock) {
        try {
            if (StockUtil.hasSpecial(stocks, Constants.PRICE)) {
                ri.add(stock.getPrice());
                for (Indicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        ri.add(indicator.getResultItem(stock));
                    }
                }
                ri.add(stock.getCurrency());
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}

