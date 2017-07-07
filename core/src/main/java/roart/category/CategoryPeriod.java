package roart.category;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.indicator.IndicatorMACDderiv;
import roart.indicator.IndicatorMove;
import roart.indicator.IndicatorRSI;
import roart.indicator.IndicatorSTOCHRSI;
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

    public CategoryPeriod(MyMyConfig conf, int i, String periodText, List<StockItem> stocks,             Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap,
            Map<String, Integer>[] periodmap) throws Exception {
        super(conf, periodText, stocks);
        this.periodmap = periodmap;
        period = i;
        indicators.add(new IndicatorMove(conf, "Δ" + getTitle(), periodmap, period));
        indicators.add(new IndicatorMACD(conf, getTitle() + " MACD", marketdatamap, periodDataMap, periodmap, getTitle(), i));
        indicators.add(new IndicatorRSI(conf, getTitle() + " RSI", marketdatamap, periodDataMap, periodmap, getTitle(), i));
        indicators.add(new IndicatorSTOCHRSI(conf, getTitle() + " SRSI", marketdatamap, periodDataMap, periodmap, getTitle(), i));

    }

    @Override
    public void addResultItemTitle(ResultItemTableRow r) {
        try {
            if (StockUtil.hasStockPeriod(stocks, period)) {
                r.add(getTitle());
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
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
