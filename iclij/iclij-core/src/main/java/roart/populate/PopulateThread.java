package roart.populate;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.constants.IclijConstants;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.model.TimingItem;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.util.MarketUtil;
import roart.util.ServiceUtil;

public class PopulateThread extends Thread {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static volatile List<Triple<String, String, String>> queue = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            log.error(Constants.EXCEPTION, e);
            Thread.currentThread().interrupt();
        }
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<Triple<Market, String, String>> markets = new ArrayList<>();
        if (instance.populate()) {
        	markets = new MarketUtil().getMarkets(false).stream().map(e -> new ImmutableTriple<Market, String, String>(e, null, null)).collect(Collectors.toList());
        }
        while (true) {
            List<Triple<String, String, String>> copy = new ArrayList<>(queue);
            queue.removeAll(copy);
            if (markets.isEmpty()) {
                for (Triple<String, String, String> market : copy) {
                    Market aMarket = new MarketUtil().findMarket(market.getLeft());
                    markets.add(new ImmutableTriple<Market, String, String>(aMarket, market.getMiddle(), market.getRight()));               
                }
            }
            for (Triple<Market, String, String> triplet : markets) {
            	Market market = triplet.getLeft();
                if (market.getConfig().getEnable() != null && !market.getConfig().getEnable()) {
                    continue;
                }                
                IclijConfig config = new IclijConfig(instance);
                config.setMarket(market.getConfig().getMarket());
                Short populate = market.getConfig().getPopulate();
                if (populate == null && !copy.contains(market.getConfig().getMarket())) {
                    continue;
                }
                if (populate == null) {
                	populate = 60;
                }
                ComponentData param = null;
                try {
                    param = ComponentData.getParam(new ComponentInput(config, null, null, null, null, true, false, new ArrayList<>(), new HashMap<>()), 0, market);
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
                    List<TimingItem> timingitems = null;
                    try {
                        timingitems = IclijDbDao.getAllTiming(market.getConfig().getMarket(), IclijConstants.FINDPROFIT, oldDate, currentDate);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                    /*
                    if (triplet.getMiddle() != null) {
                    	timingitems = timingitems.stream().filter(e -> triplet.getMiddle().equals(e.getComponent())).collect(Collectors.toList());
                    }
                    if (triplet.getRight() != null) {
                    	timingitems = timingitems.stream().filter(e -> triplet.getRight().equals(e.getSubcomponent())).collect(Collectors.toList());
                    }
                    */
                    LocalDate lastStockdate = TimeUtil.getBackEqualBefore2(currentDate, 0, dates);
                    config.setDate(lastStockdate);
                    if (!lastStockdate.isAfter(oldDate)) {
                        currentDate = currentDate.plusDays(findTime);
                        date = TimeUtil.convertDate2(currentDate);
                        index = TimeUtil.getIndexEqualAfter(dates, date);                    
                        continue;
                    }
                    ComponentInput componentInput = new ComponentInput(config, null, null, lastStockdate, null, true, false, new ArrayList<>(), new HashMap<>());
                    ServiceUtil.getFindProfit(componentInput, timingitems);
                    if (config.getFindProfitMemoryFilter()) {
                        try {
                            timingitems = IclijDbDao.getAllTiming(market.getConfig().getMarket(), IclijConstants.IMPROVEABOVEBELOW, oldDate, currentDate);
                        } catch (Exception e) {
                            log.error(Constants.EXCEPTION, e);
                        }
                        if (timingitems.isEmpty()) {
                            int verificationdays = param.getInput().getConfig().verificationDays();
                            LocalDate aCurrentDate = lastStockdate; //TimeUtil.getForwardEqualAfter2(currentDate, verificationdays, dates);
                            config.setDate(aCurrentDate);
                            ComponentInput componentInput3 = new ComponentInput(config, null, null, aCurrentDate, null, true, false, new ArrayList<>(), new HashMap<>());
                            try {
                                ServiceUtil.getImproveAboveBelow(componentInput3);
                            } catch (Exception e) {
                                log.error(Constants.EXCEPTION, e);
                            }
                        }
                    }
                    if (config.getFindProfitMemoryFilter()) {
                        try {
                            timingitems = IclijDbDao.getAllTiming(market.getConfig().getMarket(), IclijConstants.IMPROVEFILTER, oldDate, currentDate);
                        } catch (Exception e) {
                            log.error(Constants.EXCEPTION, e);
                        }
                        if (timingitems.isEmpty()) {
                            int verificationdays = param.getInput().getConfig().verificationDays();
                            LocalDate aCurrentDate = lastStockdate; //TimeUtil.getForwardEqualAfter2(currentDate, verificationdays, dates);
                            config.setDate(aCurrentDate);
                            ComponentInput componentInput3 = new ComponentInput(config, null, null, aCurrentDate, null, true, false, new ArrayList<>(), new HashMap<>());
                            try {
                                ServiceUtil.getImproveFilter(componentInput3);
                            } catch (Exception e) {
                                log.error(Constants.EXCEPTION, e);
                            }
                        }
                    }
                    currentDate = currentDate.plusDays(findTime);
                    date = TimeUtil.convertDate2(currentDate);
                    index = TimeUtil.getIndexEqualAfter(dates, date);                    
                }
            }
            markets.clear();
            try {
                TimeUnit.SECONDS.sleep(300);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
