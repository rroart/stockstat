package roart.populate;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.util.MarketUtil;
import roart.util.ServiceUtil;

public class PopulateThread extends Thread {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void run() {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        if (instance.populate()) {
            List<Market> markets = new MarketUtil().getMarkets(false);
            for (Market market : markets) {
                Short populate = market.getConfig().getPopulate();
                if (populate == null) {
                    continue;
                }
                ComponentData param = null;
                try {
                    param = ComponentData.getParam(new ComponentInput(instance, null, null, null, null, true, false, new ArrayList<>(), new HashMap<>()), 0);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }

                Short findTime = market.getConfig().getFindtime();
                List<String> dates = param.getService().getDates(market.getConfig().getMarket());
                String date = dates.get(populate);
                int index = populate;
                while (index < dates.size() - findTime) {
                    LocalDate currentDate = null;
                    try {
                        currentDate = TimeUtil.convertDate(date);
                    } catch (ParseException e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                    LocalDate oldDate = currentDate.minusDays(findTime);
                    List<IncDecItem> incdecitems = null;
                    try {
                        incdecitems = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), oldDate, currentDate, null);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                    if (incdecitems == null || incdecitems.isEmpty()) {
                        ComponentInput componentInput = new ComponentInput(instance, null, null, currentDate, null, true, false, new ArrayList<>(), new HashMap<>());
                        ServiceUtil.getFindProfit(componentInput);
                        if (instance.getFindProfitMemoryFilter()) {
                            ServiceUtil.getImproveAboveBelow(componentInput);
                        }
                    }
                    currentDate = currentDate.plusDays(findTime);
                    date = TimeUtil.convertDate2(currentDate);
                    index = TimeUtil.getIndexEqualAfter(dates, date);                    
                }
            }
        }
        log.info("Populate end");
    }
}
