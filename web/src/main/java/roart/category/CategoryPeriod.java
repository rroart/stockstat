package roart.category;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.indicator.IndicatorMove;
import roart.indicator.IndicatorRSI;
import roart.model.ResultItem;
import roart.model.Stock;
import roart.service.ControlService;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.StockUtil;
import roart.util.TaUtil;

public class CategoryPeriod extends Category {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;

    private int period;

    public CategoryPeriod(ControlService controlService, int i, String periodText, List<Stock> stocks,             Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap,
            Map<String, Integer>[] periodmap) {
        super(controlService, periodText, stocks);
        this.periodmap = periodmap;
        period = i;
        indicators.add(new IndicatorMove(controlService, "Δ" + title, periodmap, period));
        indicators.add(new IndicatorMACD(controlService, title + " mom", marketdatamap, periodDataMap, periodmap, title));
        indicators.add(new IndicatorRSI(controlService, title + " RSI", marketdatamap, periodDataMap, periodmap, title));

    }

    @Override
    public void addResultItemTitle(ResultItem ri) {
        try {
            if (StockUtil.hasStockPeriod(stocks, period)) {
                ri.add(title);
                for (Indicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        ri.add(indicator.getResultItemTitle());
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Override
    public void addResultItem(ResultItem ri, Stock stock) {
        try {
            //System.out.print("0"+period+"0 ");

            if (StockUtil.hasStockPeriod(stocks, period)) {
                //System.out.print("1");
                ri.add(StockDao.getPeriod(stock, period));
                for (Indicator indicator : indicators) {
                    //System.out.print("2");
                    if (indicator.isEnabled()) {
                        //System.out.print("ri");
                        ri.add(indicator.getResultItem(stock));
                    }
                    else { System.out.println("not"); }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}