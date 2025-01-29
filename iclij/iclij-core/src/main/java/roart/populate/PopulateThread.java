package roart.populate;

import java.net.InetAddress;
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
import roart.common.leader.MyLeader;
import roart.common.leader.impl.MyLeaderFactory;
import roart.common.model.TimingItem;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.constants.IclijConstants;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.ControlService;
import roart.iclij.service.util.MarketUtil;
import roart.util.ServiceUtil;

public class PopulateThread extends Thread {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private IclijDbDao dbDao;

    private IclijConfig iclijConfig;

    public static volatile List<Triple<String, String, String>> queue = Collections.synchronizedList(new ArrayList<>());

    public PopulateThread(IclijConfig iclijConfig, IclijDbDao dbDao) {
        this.iclijConfig = iclijConfig;
        this.dbDao = dbDao;
    }

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            log.error(Constants.EXCEPTION, e);
            Thread.currentThread().interrupt();
        }
        
        // if leader
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        long lastMain = 0;
        MyLeader leader = new MyLeaderFactory().create("populate",  hostname, iclijConfig, ControlService.curatorClient, null /*GetHazelcastInstance.instance(conf.getInmemoryHazelcast())*/);

        IclijConfig instance = iclijConfig;
        List<Triple<Market, String, String>> markets = new ArrayList<>();
        if (instance.populate()) {
        	markets = new MarketUtil().getMarkets(false, iclijConfig).stream().map(e -> new ImmutableTriple<Market, String, String>(e, null, null)).collect(Collectors.toList());
        }

        while (true) {
            boolean leading = leader.await(1, TimeUnit.SECONDS);
            if (!leading) {
                log.info("I am not populate leader");
            } else {
            log.info("I am populate leader");
            List<Triple<String, String, String>> copy = new ArrayList<>(queue);
            queue.removeAll(copy);
            if (markets.isEmpty()) {
                for (Triple<String, String, String> market : copy) {
                    Market aMarket = new MarketUtil().findMarket(market.getLeft(), iclijConfig);
                    markets.add(new ImmutableTriple<Market, String, String>(aMarket, market.getMiddle(), market.getRight()));               
                }
            }
            for (Triple<Market, String, String> triplet : markets) {
            	Market market = triplet.getLeft();
                if (market.getConfig().getEnable() != null && !market.getConfig().getEnable()) {
                    continue;
                }                
                IclijConfig config = new IclijConfig(instance);
                config.getConfigData().setMarket(market.getConfig().getMarket());
                Short populate = market.getConfig().getPopulate();
                if (populate == null && !copy.contains(market.getConfig().getMarket())) {
                    continue;
                }
                if (populate == null) {
                	populate = 60;
                }
                ComponentData param = null;
                try {
                    param = ComponentData.getParam(iclijConfig, new ComponentInput(config.getConfigData(), null, null, null, null, true, false, new ArrayList<>(), new HashMap<>()), 0, market, null);
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
                        timingitems = dbDao.getAllTiming(market.getConfig().getMarket(), IclijConstants.FINDPROFIT, oldDate, currentDate);
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
                    config.getConfigData().setDate(lastStockdate);
                    if (!lastStockdate.isAfter(oldDate)) {
                        currentDate = currentDate.plusDays(findTime);
                        date = TimeUtil.convertDate2(currentDate);
                        index = TimeUtil.getIndexEqualAfter(dates, date);                    
                        continue;
                    }
                    ComponentInput componentInput = new ComponentInput(config.getConfigData(), null, null, lastStockdate, null, true, false, new ArrayList<>(), new HashMap<>());
                    ServiceUtil.getFindProfit(componentInput, timingitems, dbDao, iclijConfig);
                    if (config.getFindProfitMemoryFilter()) {
                        try {
                            timingitems = dbDao.getAllTiming(market.getConfig().getMarket(), IclijConstants.IMPROVEABOVEBELOW, oldDate, currentDate);
                        } catch (Exception e) {
                            log.error(Constants.EXCEPTION, e);
                        }
                        if (timingitems.isEmpty()) {
                            int verificationdays = iclijConfig.verificationDays();
                            LocalDate aCurrentDate = lastStockdate; //TimeUtil.getForwardEqualAfter2(currentDate, verificationdays, dates);
                            config.getConfigData().setDate(aCurrentDate);
                            ComponentInput componentInput3 = new ComponentInput(config.getConfigData(), null, null, aCurrentDate, null, true, false, new ArrayList<>(), new HashMap<>());
                            try {
                                ServiceUtil.getImproveAboveBelow(componentInput3, dbDao, iclijConfig);
                            } catch (Exception e) {
                                log.error(Constants.EXCEPTION, e);
                            }
                        }
                    }
                    if (config.getFindProfitMemoryFilter()) {
                        try {
                            timingitems = dbDao.getAllTiming(market.getConfig().getMarket(), IclijConstants.IMPROVEFILTER, oldDate, currentDate);
                        } catch (Exception e) {
                            log.error(Constants.EXCEPTION, e);
                        }
                        if (timingitems.isEmpty()) {
                            int verificationdays = iclijConfig.verificationDays();
                            LocalDate aCurrentDate = lastStockdate; //TimeUtil.getForwardEqualAfter2(currentDate, verificationdays, dates);
                            config.getConfigData().setDate(aCurrentDate);
                            ComponentInput componentInput3 = new ComponentInput(config.getConfigData(), null, null, aCurrentDate, null, true, false, new ArrayList<>(), new HashMap<>());
                            try {
                                ServiceUtil.getImproveFilter(componentInput3, dbDao, iclijConfig);
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
            }
            log.info("Leader status populate: {}", leader.isLeader());
            try {
                TimeUnit.SECONDS.sleep(300);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
