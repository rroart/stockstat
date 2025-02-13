package roart.component.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import roart.common.model.IncDecItem;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.util.PipelineUtils;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.service.util.MarketUtil;
import roart.service.model.ProfitData;

public class IncDecUtil {

    public void filterIncDecs(ComponentData param, Market market, ProfitData profitdata,
            PipelineData[] maps, boolean inc, List<String> mydates) {
        List<String> dates;
        if (mydates == null) {
            dates = param.getService().getDates(param.getService().coremlconf.getConfigData().getMarket());        
        } else {
            dates = mydates;
        }
        String category;
        if (inc) {
            category = market.getFilter().getInccategory();
        } else {
            category = market.getFilter().getDeccategory();
        }
        if (category != null) {
            PipelineData categoryMap = PipelineUtils.getPipeline(maps, category);
            if (categoryMap != null) {
                Integer offsetDays = null;
                Integer days;
                if (inc) {
                    days = market.getFilter().getIncdays();
                } else {
                    days = market.getFilter().getDecdays();
                }
                if (days != null) {
                    if (days == 0) {
                        int year = param.getInput().getEnddate().getYear();
                        year = param.getFutureDate().getYear();
                        List<String> yearDates = new ArrayList<>();
                        for (String date : dates) {
                            int aYear = Integer.valueOf(date.substring(0, 4));
                            if (year == aYear) {
                                yearDates.add(date);
                            }
                        }
                        Collections.sort(yearDates);
                        String oldestDate = yearDates.get(0);
                        int index = dates.indexOf(oldestDate);
                        offsetDays = dates.size() - 1 - index;
                    } else {
                       offsetDays = days;
                    }
                }
                Double threshold;
                if (inc) {
                    threshold = market.getFilter().getIncthreshold();
                } else {
                    threshold = market.getFilter().getDecthreshold();
                }
                Map<String, List<List<Double>>> listMap3 = new MarketUtil().getCategoryList(maps, category);
                Map<String, IncDecItem> buysFilter = new MarketUtil().incdecFilterOnIncreaseValue(market, inc ? profitdata.getBuys() : profitdata.getSells(), maps, threshold, categoryMap,
                        listMap3, offsetDays, inc);
                if (inc) {
                    profitdata.setBuys(buysFilter);
                } else {
                    profitdata.setSells(buysFilter);
                }
            }
        }
    }
    
}
