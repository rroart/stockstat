package roart.graphindicator;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import com.vaadin.server.StreamResource;

import roart.service.ControlService;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.SvgUtil;
import roart.util.TaUtil;

public class GraphIndicatorRSI extends GraphIndicator {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    String key;

    public GraphIndicatorRSI(ControlService controlService, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        super(controlService, string);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        this.key = title;
    }

    @Override
    public boolean isEnabled() {
        return controlService.isRSIenabled();
    }

    @Override
    public void getResult(List retlist, Set<Pair> ids) {
        try {
            String periodText = key;
            int days = controlService.getTableDays();
            int topbottom = controlService.getTopBottom();
            PeriodData perioddata = periodDataMap.get(periodText);
            TaUtil tu = new TaUtil();
            for (Pair id : ids) {
                String market = (String) id.getFirst();
                String stockid = (String) id.getSecond();
                DefaultCategoryDataset dataset = tu.getRSIChart(days, market, stockid, ids, marketdatamap, perioddata, periodText);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Period " + periodText, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, 1);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+".svg", days, topbottom, controlService.getTableDays(), 1);
                    retlist.add(r);
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}

