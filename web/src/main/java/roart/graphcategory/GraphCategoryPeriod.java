package roart.graphcategory;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import roart.graphindicator.GraphIndicator;
import roart.graphindicator.GraphIndicatorMACD;
import roart.graphindicator.GraphIndicatorRSI;
import roart.model.GUISize;
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

public class GraphCategoryPeriod extends GraphCategory {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;

    //private int period;

    public GraphCategoryPeriod(ControlService controlService, int i, String periodText, 
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap) {
        super(controlService, periodText);
        //period = i;
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        indicators.add(new GraphIndicatorMACD(controlService, title + " mom", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorRSI(controlService, title + " RSI", marketdatamap, periodDataMap, title));

    }

    @Override
    public void addResult(List retlist, Set<Pair> ids, GUISize guiSize) {
        try {
            String periodText = title;
            System.out.println("check3 " + periodText + " " + periodDataMap.keySet());
            int days = controlService.getTableDays();
            int topbottom = controlService.getTopBottom();

            PeriodData perioddata = periodDataMap.get(periodText);
            //System.out.println("pairsize " + periodText + " " + perioddata.pairs.size());
            DefaultCategoryDataset dataset = StockUtil.getFilterChartPeriod(days, ids, marketdatamap, perioddata);
            if (dataset != null) {
                JFreeChart c = SvgUtil.getChart(dataset, "Period " + periodText, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, topbottom);
                OutputStream r = SvgUtil.chartToStream(c, "/tmp/new20"+".svg", days, topbottom, controlService.getTableDays(), controlService.getTopBottom(), guiSize);
                retlist.add(r);
            }
            for (GraphIndicator indicator : indicators) {
                if (indicator.isEnabled()) {
                    indicator.getResult(retlist, ids, guiSize);
                }
            }

        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
