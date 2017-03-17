package roart.graphindicator;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import com.vaadin.server.StreamResource;

import roart.model.GUISize;
import roart.model.Stock;
import roart.service.ControlService;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.SvgUtil;
import roart.util.TaUtil;

public class GraphIndicatorMACD extends GraphIndicator {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    String key;

    public GraphIndicatorMACD(ControlService controlService, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        super(controlService, string);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        this.key = title;
    }

    @Override
    public boolean isEnabled() {
        return controlService.isMACDenabled();
    }

    @Override
    public void getResult(List retlist, Set<Pair> ids, GUISize guiSize) {
        try {
            String periodText = key;
            int days = controlService.getTableDays();
            int topbottom = controlService.getTopBottom();
            System.out.println("check2 " + periodText + " " + periodDataMap.keySet());
            PeriodData perioddata = periodDataMap.get(periodText);
            TaUtil tu = new TaUtil();
            for (Pair id : ids) {
                String market = (String) id.getFirst();
                String stockid = (String) id.getSecond();
                DefaultCategoryDataset dataset = tu.getMACDChart(days, market, stockid, ids, marketdatamap, perioddata, periodText);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Period " + periodText, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, 1);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new20"+".svg", days, topbottom, controlService.getTableDays(), 1, guiSize);
                    retlist.add(r);
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
}

