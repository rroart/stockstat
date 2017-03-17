package roart.graphcategory;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.HashSet;
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

public class GraphCategoryPeriodTopBottom extends GraphCategory {

    List<Stock>[][] stocklistPeriod;

    private int period;

    public GraphCategoryPeriodTopBottom(ControlService controlService, int i,
            String periodText, List<Stock>[][] stocklistPeriod) {
        super(controlService, periodText);
        period = i;
        this.stocklistPeriod = stocklistPeriod;
    }

    @Override
    public void addResult(List retlist, Set<Pair> ids, GUISize guiSize) {
        try {
            int days = controlService.getTableDays();
            int topbottom = controlService.getTopBottom();
            String date0 = null;
            String date1 = null;
            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            for (int i = days - 1; i > 0; i--) {
                if (!stocklistPeriod[0][i].isEmpty()) {
                    date0 = dt.format(stocklistPeriod[0][i].get(0).getDate());
                    break;
                }
            }
            if (!stocklistPeriod[0][0].isEmpty()) {
                date1 = dt.format(stocklistPeriod[0][0].get(0).getDate());
            }
            {
                DefaultCategoryDataset dataset = StockUtil.getTopChart(days, topbottom,
                        stocklistPeriod, period);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Top period " + title, "Time " + date0 + " - " + date1, "Value", days, topbottom);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new2"+ period +".svg", days, topbottom, controlService.getTableDays(), controlService.getTopBottom(), guiSize);
                    retlist.add(r);
                }
            }
            {
                DefaultCategoryDataset dataset = StockUtil.getBottomChart(days, topbottom,
                        stocklistPeriod, period);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Bottom period " + title, "Time " + date0 + " - " + date1, "Value", days, topbottom);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new3"+ period +".svg", days, topbottom, controlService.getTableDays(), controlService.getTopBottom(), guiSize);
                    retlist.add(r);
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
