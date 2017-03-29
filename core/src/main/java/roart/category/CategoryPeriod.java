package roart.category;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import roart.config.MyConfig;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.indicator.IndicatorMACDderiv;
import roart.indicator.IndicatorMove;
import roart.indicator.IndicatorRSI;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
//import roart.model.Stock;
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

    public CategoryPeriod(MyConfig conf, int i, String periodText, List<StockItem> stocks,             Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap,
            Map<String, Integer>[] periodmap) throws Exception {
        super(conf, periodText, stocks);
        this.periodmap = periodmap;
        period = i;
        indicators.add(new IndicatorMove(conf, "Δ" + title, periodmap, period));
        indicators.add(new IndicatorMACD(conf, title + " mom", marketdatamap, periodDataMap, periodmap, title, i));
        //indicators.add(new IndicatorMACDderiv(conf, title + " md", marketdatamap, periodDataMap, periodmap, title, i));
        indicators.add(new IndicatorRSI(conf, title + " RSI", marketdatamap, periodDataMap, periodmap, title, i));

    }

    @Override
    public void addResultItemTitle(ResultItemTableRow r) {
        try {
            if (StockUtil.hasStockPeriod(stocks, period)) {
                r.add(title);
                for (Indicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        r.addarr(indicator.getResultItemTitle());
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Override
    public void addResultItem(ResultItemTableRow r, StockItem stock) {
        try {
            //System.out.print("0"+period+"0 ");

            if (StockUtil.hasStockPeriod(stocks, period)) {
                //System.out.print("1");
                r.add(StockDao.getPeriod(stock, period));
                for (Indicator indicator : indicators) {
                    //System.out.print("2");
                    if (indicator.isEnabled()) {
                        //System.out.print("ri");
                        r.addarr(indicator.getResultItem(stock));
                    }
                    else { System.out.println("not"); }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
