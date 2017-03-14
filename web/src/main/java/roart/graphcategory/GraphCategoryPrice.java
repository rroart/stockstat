package roart.graphcategory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import com.vaadin.server.StreamResource;

import roart.graphindicator.GraphIndicator;
import roart.graphindicator.GraphIndicatorMACD;
import roart.graphindicator.GraphIndicatorRSI;
import roart.model.ResultItem;
import roart.model.Stock;
import roart.service.ControlService;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.StockUtil;
import roart.util.SvgUtil;
import roart.util.TaUtil;

public class GraphCategoryPrice extends GraphCategory {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;

    public GraphCategoryPrice(ControlService controlService, String string,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap) {
        super(controlService, string);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        indicators.add(new GraphIndicatorMACD(controlService, title + " mom", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorRSI(controlService, title + " RSI", marketdatamap, periodDataMap, title));
    }

    @Override
    public void addResult(List retlist, Set<Pair> ids) {
        try {
            if (StockUtil.hasSpecial(marketdatamap, Constants.PRICE)) {
                String periodText = title;
                int days = controlService.getTableDays();
                int topbottom = controlService.getTopBottom();

                System.out.println("check " + periodText + " " + periodDataMap.keySet());
                PeriodData perioddata = periodDataMap.get(periodText);
                //PeriodData perioddata = new PeriodData();
                DefaultCategoryDataset dataseteq = null;
                if (controlService.isGraphEqualize()) {    
                    dataseteq = new DefaultCategoryDataset( );
                }
                DefaultCategoryDataset dataset = StockUtil.getFilterChartDated(days, ids, marketdatamap, perioddata, Constants.PRICE, controlService.isGraphEqualize(), dataseteq);
                if (dataset != null) {
                    String currency = null; //datedstocklists[0].get(0).getCurrency();
                    if (currency == null) {
                        currency = "Value";
                    }
                    JFreeChart c = SvgUtil.getChart(dataset, "Price", "Time " + perioddata.date0 + " - " + perioddata.date1, currency, days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+ 1 +".svg", days, topbottom, controlService.getTableDays(), controlService.getTopBottom());
                    retlist.add(r);
                }
                if (dataset != null && dataseteq != null) {
                    String currency = null; //datedstocklists[0].get(0).getCurrency();
                    if (currency == null) {
                        currency = "Value";
                    }
                    JFreeChart c = SvgUtil.getChart(dataseteq, "Price", "Time " + perioddata.date0 + " - " + perioddata.date1, currency, days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+ 1 +".svg", days, topbottom, controlService.getTableDays(), controlService.getTopBottom());
                    retlist.add(r);
                }
                for (GraphIndicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        indicator.getResult(retlist, ids);
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}

