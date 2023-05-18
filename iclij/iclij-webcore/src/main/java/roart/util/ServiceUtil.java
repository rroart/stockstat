package roart.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.analysis.function.Add;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.config.MarketFilter;
import roart.iclij.factory.actioncomponentconfig.ActionComponentConfigFactory;
import roart.iclij.factory.actioncomponentconfig.ComponentMap;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.iclij.verifyprofit.TrendUtil;
import roart.iclij.filter.Memories;
import roart.common.config.ConfigConstants;
import roart.common.config.MyConfig;
import roart.common.constants.Constants;
import roart.common.model.ConfigItem;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.model.MetaItem;
import roart.common.model.TimingItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.MathUtil;
import roart.common.util.MetaUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentTimeUtil;
import roart.constants.IclijConstants;
import roart.constants.IclijPipelineConstants;
import roart.db.dao.IclijDbDao;
import roart.iclij.model.MapList;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.action.DatasetActionData;
import roart.iclij.model.action.EvolveActionData;
import roart.iclij.model.action.ImproveFilterActionData;
import roart.iclij.model.action.ImproveProfitActionData;
import roart.iclij.model.action.MachineLearningActionData;
import roart.iclij.model.action.FindProfitActionData;
import roart.iclij.model.action.ImproveAboveBelowActionData;
import roart.iclij.model.action.CrossTestActionData;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.util.MLUtil;
import roart.iclij.util.MarketUtil;
import roart.iclij.util.MiscUtil;
import roart.iclij.util.RelationUtil;
import roart.iclij.service.ControlService;
import roart.iclij.service.IclijServiceList;
import roart.iclij.service.IclijServiceResult;

public class ServiceUtil {
    private static Logger log = LoggerFactory.getLogger(ServiceUtil.class);

    public static double median(Set<Double> set) {
        Double[] scores = set.toArray(new Double[set.size()]);
        Arrays.sort(scores);
        //System.out.print("Sorted Scores: ");
        for (double x : scores) {
            //System.out.print(x + " ");
        }
        //System.out.println("");

        // Calculate median (middle number)
        double median = 0;
        double pos1 = Math.floor((scores.length - 1.0) / 2.0);
        double pos2 = Math.ceil((scores.length - 1.0) / 2.0);
        if (pos1 == pos2 ) {
            median = scores[(int)pos1];
        } else {
            median = (scores[(int)pos1] + scores[(int)pos2]) / 2.0 ;
        }
        return median;
    }

    private static double median2(Set<Double> set) {
        Double[] numArray = set.toArray(new Double[set.size()]);
        Arrays.sort(numArray);
        Arrays.sort(numArray);
        int middle = numArray.length/2;
        double medianValue = 0; //declare variable 
        if (numArray.length%2 == 1) {
            medianValue = numArray[middle];
        } else {
            medianValue = (numArray[middle-1] + numArray[middle]) / 2;
        }
        return medianValue;
    }

    @Deprecated
    public static String getWantedCategory(Map<String, Map<String, Object>> maps, String type) throws Exception {
        List<String> wantedList = new ArrayList<>();
        wantedList.add(Constants.PRICE);
        wantedList.add(Constants.INDEX);
        //wantedList.add("cy");
        String cat = null;
        for (String wanted : wantedList) {
            Map<String, Object> map = maps.get(wanted);
            if (map != null) {
                if (map.containsKey(type)) {
                    LinkedHashMap<String, Object> tmpMap = (LinkedHashMap<String, Object>) map.get(type);
                    if (tmpMap.get(PipelineConstants.RESULT) != null) {
                        return wanted;
                    }
                }
            }
        }
        return cat;
    }

    public static String getWantedCategory2(Map<String, Map<String, Object>> maps, String type) throws Exception {
        System.out.println(maps.keySet());
        List<String> wantedList = new ArrayList<>();
        wantedList.add(Constants.PRICE);
        wantedList.add(Constants.INDEX);
        //wantedList.add("cy");
        String cat = null;
        for (String wanted : wantedList) {
            Map<String, Object> map = maps.get(wanted + " " + type.toUpperCase());
            if (map != null) {
                return wanted;
            }
        }
        for (String key : maps.keySet()) {
            if (key.endsWith(" " + type.toUpperCase())) {
                return key.substring(0, key.length() - 1 - type.length());
            }
        }
        return cat;
    }

    public static IclijServiceResult getConfig(IclijConfig iclijConfig) throws Exception {
        IclijConfig instance = iclijConfig;
        IclijServiceResult result = new IclijServiceResult();
        result.setConfig(instance);
        return result;
    }

    public static IclijServiceResult getContent(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        FindProfitActionData findProfitActionData = new FindProfitActionData(iclijConfig, dbDao);

        LocalDate date = componentInput.getEnddate();
        IclijConfig instance = iclijConfig;

        List<IncDecItem> listAll = dbDao.getAllIncDecs();
        Set<IncDecItem> listRel = new HashSet<>();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        Map<String, Object> trendMap = new HashMap<>();
        List<Market> markets = IclijXMLConfig.getMarkets(instance);
        markets = new MarketUtil().filterMarkets(markets, findProfitActionData.isDataset());
        for (Market market : markets) {
            ComponentData param = null;
            try {
                param = ComponentData.getParam(iclijConfig, componentInput, 0);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                return result;
            }
            List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
            // the market may be incomplete, the exception and skip
            try {
                LocalDate endDate = componentInput.getEnddate();
                LocalDate prevDate = TimeUtil.getEqualBefore(stockDates, endDate);
                short startoffset = new MarketUtil().getStartoffset(market);
                prevDate = TimeUtil.getBackEqualBefore2(prevDate, startoffset, stockDates);
                Trend trend = new TrendUtil().getTrend(instance.verificationDays(), null /*TimeUtil.convertDate2(prevDate)*/, startoffset, stockDates, param, market);
                trendMap.put(market.getConfig().getMarket(), trend);
            } catch (Exception e) {
                log.error("Trend exception for market {}", market.getConfig().getMarket());
                log.error(Constants.EXCEPTION, e);
            }
            List<IncDecItem> allCurrentIncDecs = new MiscUtil().getCurrentIncDecs(date, listAll, market, market.getConfig().getFindtime(), true);
            
            short startoffset = new MarketUtil().getStartoffset(market);
            int verificationdays = 0;

            Memories memories = new Memories(market);
            final int AVERAGE_SIZE = 5;
            LocalDate prevdate = param.getInput().getEnddate();
            prevdate = TimeUtil.getBackEqualBefore2(prevdate, verificationdays + startoffset, stockDates);
            prevdate = prevdate.minusDays(findProfitActionData.getTime(market));
            LocalDate olddate = prevdate.minusDays(((int) AVERAGE_SIZE) * findProfitActionData.getTime(market));
            getListComponents(null, param, instance, market, memories, prevdate, olddate, findProfitActionData);

            allCurrentIncDecs = allCurrentIncDecs
                    .stream()
                    .filter(e -> !memories.containsBelow(e.getComponent(), new ImmutablePair(e.getSubcomponent(), e.getLocalcomponent()), null, null, true))
                    .collect(Collectors.toList());
            
            Map<String, Set<IncDecItem>> currentIncDecMap = splitParam(allCurrentIncDecs);
            for (Entry<String, Set<IncDecItem>> entry : currentIncDecMap.entrySet()) {
                String key = entry.getKey();
                Set<IncDecItem> currentIncDecs = entry.getValue();
                Set<IncDecItem> listInc = currentIncDecs.stream().filter(m -> m.isIncrease()).collect(Collectors.toSet());
                Set<IncDecItem> listDec = currentIncDecs.stream().filter(m -> !m.isIncrease()).collect(Collectors.toSet());
                listInc = new MiscUtil().mergeList(listInc, false);
                listDec = new MiscUtil().mergeList(listDec, false);
                Set<IncDecItem> listIncDec = new MiscUtil().moveAndGetCommon(listInc, listDec, true);
                List<IclijServiceList> subLists = getServiceList(market.getConfig().getMarket(), key, listInc, listDec, listIncDec);
                lists.addAll(subLists);
                listRel.addAll(listInc);
                listRel.addAll(listDec);
                listRel.addAll(listIncDec);
            }
        }
        result.setLists(lists);

        getContentTimings(date, lists, markets, findProfitActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        //getUpdateMarkets(componentInput, param, updateMarketMap, updateMap, findProfitActionData);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, findProfitActionData, true);

        IclijServiceList trends = convert(trendMap);
        lists.add(trends);

        addRelations(componentInput, lists, listRel, dbDao, iclijConfig);

        new MiscUtil().print(result);
        return result;
    }

    // overlaps
    private static void getListComponents(WebData myData, ComponentData param, IclijConfig config,
            Market market,
            Memories memories, LocalDate prevdate, LocalDate olddate, MarketActionData actionData) {
        List<MemoryItem> marketMemory = new MarketUtil().getMarketMemory(market, IclijConstants.IMPROVEABOVEBELOW, null, null, null, olddate, prevdate, actionData.getDbDao());
        marketMemory = marketMemory.stream().filter(e -> "Confidence".equals(e.getType())).collect(Collectors.toList());
        if (!marketMemory.isEmpty()) {
            int jj = 0;
        }
        if (marketMemory == null) {
            myData.setProfitData(new ProfitData());
        }
        int AVERAGE_SIZE = 5;
        //marketMemory.addAll(myData.getMemoryItems());
        List<MemoryItem> currentList = new MiscUtil().filterKeepRecent3(marketMemory, prevdate, ((int) AVERAGE_SIZE) * actionData.getTime(market), false);
        // map subcat + posit -> list
        currentList = currentList.stream().filter(e -> !e.getComponent().equals(PipelineConstants.ABOVEBELOW)).collect(Collectors.toList());
        memories.method(currentList, config);
     }

    private static void addRelations(ComponentInput componentInput, List<IclijServiceList> lists, Set<IncDecItem> listIncDecs, IclijDbDao dbDao, IclijConfig iclijConfig) throws Exception {
        Set[] objects = new RelationUtil().method(componentInput, listIncDecs, dbDao, iclijConfig);

        IclijServiceList incdecs = new IclijServiceList();
        incdecs.setTitle("Incdecs with relations");
        incdecs.setList(new ArrayList<>((objects[0])));

        IclijServiceList relations = new IclijServiceList();
        relations.setTitle("Relations");
        relations.setList(new ArrayList<>(objects[1]));

        lists.add(incdecs);
        lists.add(relations);
    }

    public static IclijServiceResult getContentImprove(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        ImproveProfitActionData improveProfitActionData = new ImproveProfitActionData(iclijConfig, dbDao);
        LocalDate date = componentInput.getEnddate();

        List<IncDecItem> listAll = dbDao.getAllIncDecs();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = IclijXMLConfig.getMarkets(iclijConfig);
        markets = new MarketUtil().filterMarkets(markets, improveProfitActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        getContentTimings(date, lists, markets, improveProfitActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(updateMarketMap, updateMap, improveProfitActionData, param);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, improveProfitActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    public static IclijServiceResult getContentFilter(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        ImproveFilterActionData improveFilterActionData = new ImproveFilterActionData(iclijConfig, dbDao);
        LocalDate date = componentInput.getEnddate();

        List<IncDecItem> listAll = dbDao.getAllIncDecs();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = IclijXMLConfig.getMarkets(iclijConfig);
        markets = new MarketUtil().filterMarkets(markets, improveFilterActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        getContentTimings(date, lists, markets, improveFilterActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(updateMarketMap, updateMap, improveFilterActionData, param);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, improveFilterActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    public static IclijServiceResult getContentAboveBelow(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        ImproveAboveBelowActionData improveAboveBelowActionData = new ImproveAboveBelowActionData(iclijConfig, dbDao);
        LocalDate date = componentInput.getEnddate();

        List<IncDecItem> listAll = dbDao.getAllIncDecs();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = IclijXMLConfig.getMarkets(iclijConfig);
        markets = new MarketUtil().filterMarkets(markets, improveAboveBelowActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        getContentTimings(date, lists, markets, improveAboveBelowActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(updateMarketMap, updateMap, improveAboveBelowActionData, param);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, improveAboveBelowActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    private static void getContentTimings(LocalDate date, List<IclijServiceList> lists, List<Market> markets, MarketActionData action)
            throws Exception {
        List<TimingItem> listAllTimings = action.getDbDao().getAllTiming();
        for (Market market : markets) {
            List<TimingItem> currentTimings = new MiscUtil().getCurrentTimingsRecord(date, listAllTimings, market, action.getName(), action.getTime(market), true);
            List<IclijServiceList> subLists = getServiceList(market.getConfig().getMarket(), currentTimings);
            lists.addAll(subLists);
        }
    }

    private static void getContentMLTest(LocalDate date, List<IclijServiceList> lists, List<Market> markets, MarketActionData action)
            throws Exception {
        List<MLMetricsItem> listAllTimings = action.getDbDao().getAllMLMetrics();
        for (Market market : markets) {
            List<MLMetricsItem> currentTimings = new MiscUtil().getCurrentMLMetrics(date, listAllTimings, market, action.getTime(market), true);
            List<IclijServiceList> subLists = getServiceList2(market.getConfig().getMarket(), currentTimings);
            lists.addAll(subLists);
        }
    }

    private static void getUpdateMarkets(Map<String, Map<String, Object>> updateMarketMap, Map<String, Object> updateMap,
            MarketActionData actionData, ComponentData param)
                    throws Exception {
        //Market market = findProfitActionData.findMarket(param);
        //String marketName = market.getConfig().getMarket();
        long time0 = System.currentTimeMillis();
        List<MetaItem> metas = param.getService().getMetas();
        for (Market market : new MarketUtil().getMarkets(actionData.isDataset(), actionData.getIclijConfig())) {
            String marketName = market.getConfig().getMarket();
            MetaItem meta = new MetaUtil().findMeta(metas, marketName);
            boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
            List<String> componentList = actionData.getComponents(param.getInput().getConfig(), wantThree);
            Map<Boolean, String> booleanTexts = actionData.getBooleanTexts();
            Boolean[] booleans = actionData.getBooleans();
            for (Boolean bool : booleans) {
                updateMarketMap.put(market.getConfig().getMarket() + " " + booleanTexts.get(bool), new HashMap<>());
                Map<String, ActionComponentConfig> componentMap = new ComponentMap().getComponentMap(componentList, actionData.getName());
                for (ActionComponentConfig component : componentMap.values()) {
                    List<String> subcomponents = component.getSubComponents(market, param.getInput().getConfig(), null, actionData.getMLConfig(param.getInput().getConfig()));
                    for (String subcomponent : subcomponents) {
                        Map<String, Object> anUpdateMap = new MiscUtil().loadConfig(param.getService(), param.getInput(), market, market.getConfig().getMarket(), actionData.getName(), actionData.getName(), false, bool, subcomponent, actionData, null);
                        updateMarketMap.get(market.getConfig().getMarket() + " " + booleanTexts.get(bool)).putAll(anUpdateMap);
                        updateMap.putAll(anUpdateMap);
                    }
                }
            }
        }
        log.info("Gettings {}", (System.currentTimeMillis() - time0) / 1000);
    }

    public static IclijServiceResult getContentEvolve(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        EvolveActionData evolveActionData = new EvolveActionData(iclijConfig, dbDao);
        LocalDate date = componentInput.getEnddate();

        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = IclijXMLConfig.getMarkets(iclijConfig);
        markets = new MarketUtil().filterMarkets(markets, evolveActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        getContentTimings(date, lists, markets, evolveActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(updateMarketMap, updateMap, evolveActionData, param);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, evolveActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    public static IclijServiceResult getContentDataset(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        DatasetActionData datasetActionData = new DatasetActionData(iclijConfig, dbDao);
        LocalDate date = componentInput.getEnddate();

        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = IclijXMLConfig.getMarkets(iclijConfig);
        markets = new MarketUtil().filterMarkets(markets, datasetActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        getContentTimings(date, lists, markets, datasetActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(updateMarketMap, updateMap, datasetActionData, param);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, datasetActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    public static IclijServiceResult getContentCrosstest(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        CrossTestActionData crossTestActionData = new CrossTestActionData(iclijConfig, dbDao);
        LocalDate date = componentInput.getEnddate();

        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = IclijXMLConfig.getMarkets(iclijConfig);
        markets = new MarketUtil().filterMarkets(markets, crossTestActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        getContentTimings(date, lists, markets, crossTestActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(updateMarketMap, updateMap, crossTestActionData, param);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, crossTestActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    private static void getContentMemoriesUpdates(ComponentInput componentInput, List<IclijServiceList> lists,
            Map<String, Map<String, Object>> updateMarketMap, MarketActionData actionData, boolean useMemory) {
        for (Market market : new MarketUtil().getMarkets(actionData.isDataset(), actionData.getIclijConfig())) {
            //for (Entry<String, Map<String, Object>> entry : updateMarketMap.entrySet()) {
            String marketName = market.getConfig().getMarket();
            Map<Boolean, String> booleanTexts = actionData.getBooleanTexts();
            Boolean[] booleans = actionData.getBooleans();
            for (Boolean bool : booleans) {
                Map<String, Object> anUpdateMap = updateMarketMap.get(marketName + " " + booleanTexts.get(bool));
                if (anUpdateMap != null) {
                    IclijServiceList updates = convert(marketName + " " + booleanTexts.get(bool), anUpdateMap);
                    lists.add(updates);
                }
            }
            if (!useMemory) {
                continue;
            }
            List<MemoryItem> marketMemory = new MarketUtil().getMarketMemory(market, actionData.getDbDao());
            List<MemoryItem> currentList = new MiscUtil().filterKeepRecent(marketMemory, componentInput.getEnddate(), actionData.getTime(market), true);

            IclijServiceList memories = new IclijServiceList();
            memories.setTitle("Memories " + marketName);
            roundList3(currentList);
            memories.setList(currentList);
            lists.add(memories);
        }
    }

    public static IclijServiceResult getContentMachineLearning(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        MachineLearningActionData mlActionData = new MachineLearningActionData(iclijConfig, dbDao);
        LocalDate date = componentInput.getEnddate();

        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = IclijXMLConfig.getMarkets(iclijConfig);
        markets = new MarketUtil().filterMarkets(markets, mlActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        getContentTimings(date, lists, markets, mlActionData);
        getContentMLTest(date, lists, markets, mlActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        //getUpdateMarkets(componentInput, param, updateMarketMap, updateMap, mlActionData);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, mlActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    static IclijServiceList getHeader(String title) {
        IclijServiceList header = new IclijServiceList();
        header.setTitle(title);
        return header;
    }

    private static List<IclijServiceList> getServiceList(String market, List<TimingItem> listIncDec) {
        List<IclijServiceList> subLists = new ArrayList<>();
        roundList2(listIncDec);
        if (!listIncDec.isEmpty()) {
            IclijServiceList incDec = new IclijServiceList();
            incDec.setTitle(market + " " + "timing" + " " + listIncDec.stream().mapToDouble(TimingItem::getMytime).summaryStatistics());
            incDec.setList(listIncDec);
            subLists.add(incDec);
        }
        return subLists;
    }

    private static List<IclijServiceList> getServiceList2(String market, List<MLMetricsItem> listTest) {
        List<IclijServiceList> subLists = new ArrayList<>();
        roundList4(listTest);
        if (!listTest.isEmpty()) {
            IclijServiceList incDec = new IclijServiceList();
            incDec.setTitle(market + " mlstats");
            incDec.setList(listTest);
            subLists.add(incDec);
        }
        return subLists;
    }
    static List<IclijServiceList> getServiceList(String market, String text, Set<IncDecItem> listInc, Set<IncDecItem> listDec,
            Set<IncDecItem> listIncDec) {
        List<IclijServiceList> subLists = new ArrayList<>();
        roundList(listInc);
        roundList(listDec);
        roundList(listIncDec);
        if (!listInc.isEmpty()) {
            List<Boolean> listIncBoolean = listInc.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
            long count = listIncBoolean.stream().filter(i -> i).count();                            
            IclijServiceList inc = new IclijServiceList();
            String trendStr = "";
            inc.setTitle(market + " " + "Increase " + text + " ( verified " + count + " / " + listIncBoolean.size() + " )" + trendStr);
            inc.setList(new ArrayList<>(listInc));
            subLists.add(inc);
        }
        if (!listDec.isEmpty()) {
            List<Boolean> listDecBoolean = listDec.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
            long count = listDecBoolean.stream().filter(i -> i).count();                            
            IclijServiceList dec = new IclijServiceList();
            dec.setTitle(market + " " + "Decrease " + text + " ( verified " + count + " / " + listDecBoolean.size() + " )");
            dec.setList(new ArrayList<>(listDec));
            subLists.add(dec);
        }
        if (!listIncDec.isEmpty()) {
            List<Boolean> listIncDecBoolean = listIncDec.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
            long count = listIncDecBoolean.stream().filter(i -> i).count();                            
            IclijServiceList incDec = new IclijServiceList();
            incDec.setTitle(market + " " + "Increase and decrease " + text + "( verified " + count + " / " + listIncDecBoolean.size() + " )" );
            incDec.setList(new ArrayList<>(listIncDec));
            subLists.add(incDec);
        }
        wipeFields(listInc);
        wipeFields(listDec);
        wipeFields(listIncDec);
        return subLists;
    }

    public static IclijServiceResult getVerify(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        String type = "Verify";
        //componentInput.setDoSave(componentInput.getConfig().wantVerificationSave());
        //componentInput.setDoSave(false);
        int verificationdays = componentInput.getConfig().verificationDays();
        boolean rerun = componentInput.getConfig().verificationRerun();
        IclijServiceResult result = getFindProfitVerify(iclijConfig, componentInput, type, verificationdays, rerun, dbDao);
        new MiscUtil().print(result);
        return result;
    }

    private static IclijServiceResult getFindProfitVerify(IclijConfig iclijConfig, ComponentInput componentInput, String type, int verificationdays, boolean rerun, IclijDbDao dbDao) throws Exception {

        componentInput.setDoSave(componentInput.getConfig().wantsFindProfitRerunSave());
        LocalDate date = componentInput.getEnddate();

        IclijServiceResult result = new IclijServiceResult();
        result.setLists(new ArrayList<>());
        List<IclijServiceList> retLists = result.getLists();

        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        FindProfitActionData findProfitActionData = new FindProfitActionData(iclijConfig, dbDao);
        // this calculates, does not read from db
        List<MemoryItem> allMemoryItems = new ArrayList<>();
        //ProfitData picks = findProfitActionData.getPicks(param, allMemoryItems);
        //getMemoryItems(componentInput.getConfig(), param, verificationdays, getFindProfitComponents(componentInput.getConfig()));
        IclijServiceList memories = new IclijServiceList();
        memories.setTitle("Memories");
        roundList3(allMemoryItems);
        memories.setList(allMemoryItems);
        Map<String, Object> updateMap = new HashMap<>();
        //param.setUpdateMap(updateMap);
        Market market = new MarketUtil().findMarket(param.getService().conf.getMarket(), iclijConfig);
        
        LocalDate endDate = componentInput.getEnddate();
        int findTime = market.getConfig().getFindtime();

        WebData myData;
        if (rerun) {
            myData = param.getService().getRun(IclijConstants.FINDPROFIT, componentInput);
            if (iclijConfig.getFindProfitMemoryFilter()) {
                myData = param.getService().getRun(IclijConstants.IMPROVEABOVEBELOW, componentInput);
            }
            if (iclijConfig.getFindProfitMemoryFilter()) {
                myData = param.getService().getRun(IclijConstants.IMPROVEFILTER, componentInput);
            }
            //ProfitData buysells = myData.profitData; // findProfitActionData.getPicks(param, allMemoryItems);
        } else {
            myData = param.getService().getVerify(IclijConstants.FINDPROFIT, componentInput);            
        }
        updateMap = myData.getUpdateMap();
        allMemoryItems.addAll(myData.getMemoryItems());

        Map<String, Object> trendMap = new HashMap<>();
        short startoffset = new MarketUtil().getStartoffset(market);
     
        // offset is the time interval for which we are checking the two trend
        // verificationdays is the interval for above / below check
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());

        Trend trend;
        Trend trend2;
        if (rerun) {
            trend = new TrendUtil().getTrend(componentInput.getConfig().verificationDays(), null /*TimeUtil.convertDate2(olddate)*/, startoffset, stockDates, findTime, param, market);
            trend2 = new TrendUtil().getTrend(componentInput.getConfig().verificationDays(), null /*TimeUtil.convertDate2(prevdate)*/, startoffset, stockDates, param, market);
        } else {
            trend = new TrendUtil().getTrend(componentInput.getConfig().verificationDays(), null /*TimeUtil.convertDate2(olddate)*/, startoffset, stockDates, findTime, param, market);            
            trend2 = new TrendUtil().getTrend(componentInput.getConfig().verificationDays(), null /*TimeUtil.convertDate2(prevdate)*/, startoffset, stockDates, param, market);            
        }
        trendMap.put(market.getConfig().getMarket(), trend);
        trendMap.put(market.getConfig().getMarket() + "end", trend2);
        String baseDateStr = TimeUtil.convertDate2(param.getBaseDate());
        String futureDateStr = TimeUtil.convertDate2(param.getFutureDate());
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        Set<IncDecItem> allListInc = new HashSet<>(myData.getIncs());
        Set<IncDecItem> allListDec = new HashSet<>(myData.getDecs());
        allListInc = new MiscUtil().mergeList(allListInc, !rerun);
        allListDec = new MiscUtil().mergeList(allListDec, !rerun);

        Memories memoryFilter = new Memories(market);
        final int AVERAGE_SIZE = 5;
        LocalDate prevdate = param.getInput().getEnddate();
        prevdate = TimeUtil.getBackEqualBefore2(prevdate, verificationdays + startoffset, stockDates);
        prevdate = prevdate.minusDays(findProfitActionData.getTime(market));
        LocalDate olddate = prevdate.minusDays(((int) AVERAGE_SIZE) * findProfitActionData.getTime(market));
        getListComponents(null, param, iclijConfig, market, memoryFilter, prevdate, olddate, findProfitActionData);

        allListInc = allListInc
                .stream()
                .filter(e -> !memoryFilter.containsBelow(e.getComponent(), new ImmutablePair(e.getSubcomponent(), e.getLocalcomponent()), null, null, true))
                .collect(Collectors.toSet());
        allListDec = allListDec
                .stream()
                .filter(e -> !memoryFilter.containsBelow(e.getComponent(), new ImmutablePair(e.getSubcomponent(), e.getLocalcomponent()), null, null, true))
                .collect(Collectors.toSet());

        
        Set<IncDecItem> allListIncDec;
        if (rerun) {
            allListIncDec = new MiscUtil().moveAndGetCommon(allListInc, allListDec, verificationdays > 0);
        } else {
            allListIncDec = new MiscUtil().moveAndGetCommon2(allListInc, allListDec, verificationdays > 0);
        }
        Map<String, Set<IncDecItem>> allListIncDecMap = splitParam(allListIncDec);
        Map<String, Set<IncDecItem>> allListIncMap = splitParam(allListInc);
        Map<String, Set<IncDecItem>> allListDecMap = splitParam(allListDec);
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(allListIncDecMap.keySet());
        allKeys.addAll(allListDecMap.keySet());
        allKeys.addAll(allListIncMap.keySet());
        for (String key : allKeys) {
            Set<IncDecItem> listIncDec = allListIncDecMap.get(key);
            Set<IncDecItem> listInc = allListIncMap.get(key);
            Set<IncDecItem> listDec = allListDecMap.get(key);
            if (listIncDec == null) {
                listIncDec = new HashSet<>();
            }
            if (listDec == null) {
                listDec = new HashSet<>();
            }
            if (listInc == null) {
                listInc = new HashSet<>();
            }
            if (verificationdays > 0) {
                if (rerun) {
                    new VerifyProfitUtil().getVerifyProfit(verificationdays, null, null, listInc, listDec, listIncDec, startoffset, componentInput.getConfig().getFindProfitManualThreshold(), param, stockDates, market);
                } else {
                    new VerifyProfitUtil().getVerifyProfit(verificationdays, null, null, listInc, listDec, listIncDec, startoffset, componentInput.getConfig().getFindProfitManualThreshold(), param, stockDates, market);                
                }
            }
            addHeader(componentInput, type, result, baseDateStr, futureDateStr, market);

            List<IclijServiceList> subLists = getServiceList(param.getService().conf.getMarket(), key, listInc, listDec, listIncDec);
            retLists.addAll(subLists);
        }

        retLists.add(memories);

        {
            List<TimingItem> currentTimings = (List<TimingItem>) myData.getTimingMap().get(market.getConfig().getMarket());
            if (currentTimings != null) {
                List<IclijServiceList> subLists2 = getServiceList(market.getConfig().getMarket(), currentTimings);
                retLists.addAll(subLists2);
            }
        }
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        IclijServiceList updates = convert(market.getConfig().getMarket(), updateMap);
        retLists.add(updates);

        IclijServiceList trends = convert(trendMap);
        retLists.add(trends);

        Set<IncDecItem> currentIncDecs = new HashSet<>();
        List<IncDecItem> listAll = dbDao.getAllIncDecs();
        List<Market> markets = IclijXMLConfig.getMarkets(iclijConfig);
        markets = new MarketUtil().filterMarkets(markets, false);
        for (Market aMarket : markets) {
            if (rerun && !iclijConfig.wantsFindProfitRerunSave() && market.getConfig().getMarket().equals(aMarket.getConfig().getMarket()) ) {
                //List<IncDecItem> listIncDecs = new ArrayList<>();
                //listIncDecs.addAll(myData.getIncs());
                //listIncDecs.addAll(myData.getDecs());
                //listIncDecs = mergeList(listIncDecs, !rerun);
                currentIncDecs.addAll(allListInc);
                currentIncDecs.addAll(allListDec);
                currentIncDecs.addAll(allListIncDec);
                continue;
            }
            List<IncDecItem> marketCurrentIncDecs = new MiscUtil().getCurrentIncDecs(param.getFutureDate(), listAll, aMarket, market.getConfig().getFindtime(), true);
            currentIncDecs.addAll(marketCurrentIncDecs);
        }
        //roundList(currentIncDecs);
        addRelations(componentInput, retLists, currentIncDecs, dbDao, iclijConfig);

        return result;
    }

    public static IclijServiceResult getImproveAboveBelow(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        String type = "Verify";
        //componentInput.setDoSave(componentInput.getConfig().wantVerificationSave());
        //componentInput.setDoSave(false);
        int verificationdays = componentInput.getConfig().verificationDays();
        boolean rerun = componentInput.getConfig().verificationRerun();
 
        componentInput.setDoSave(componentInput.getConfig().wantsFindProfitRerunSave());

        IclijServiceResult result = new IclijServiceResult();
        result.setLists(new ArrayList<>());
        List<IclijServiceList> retLists = result.getLists();

        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        // this calculates, does not read from db
        List<MemoryItem> allMemoryItems = new ArrayList<>();
        IclijServiceList memories = new IclijServiceList();
        memories.setTitle("Memories");
        roundList3(allMemoryItems);
        memories.setList(allMemoryItems);
        Map<String, Object> updateMap = new HashMap<>();
        //param.setUpdateMap(updateMap);
        Market market = new MarketUtil().findMarket(param.getService().conf.getMarket(), iclijConfig);
        WebData myData = null;
        if (rerun) {
            myData = param.getService().getRun(IclijConstants.IMPROVEABOVEBELOW, componentInput);
            updateMap = myData.getUpdateMap();
            allMemoryItems.addAll(myData.getMemoryItems());
        } else {
            //myData = new WebData(); //param.getService().getVerify(IclijConstants.FINDPROFIT, componentInput);            
        }

        short startoffset = new MarketUtil().getStartoffset(market);
        
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        String baseDateStr = TimeUtil.convertDate2(param.getBaseDate());
        String futureDateStr = TimeUtil.convertDate2(param.getFutureDate());
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        Set<IncDecItem> allListInc = new HashSet<>(myData.getIncs());
        Set<IncDecItem> allListDec = new HashSet<>(myData.getDecs());
        allListInc = new MiscUtil().mergeList(allListInc, !rerun);
        allListDec = new MiscUtil().mergeList(allListDec, !rerun);
        Set<IncDecItem> allListIncDec;
        if (rerun) {
            allListIncDec = new MiscUtil().moveAndGetCommon(allListInc, allListDec, verificationdays > 0);
        } else {
            allListIncDec = new MiscUtil().moveAndGetCommon2(allListInc, allListDec, verificationdays > 0);
        }
        Map<String, Set<IncDecItem>> allListIncDecMap = splitParam(allListIncDec);
        Map<String, Set<IncDecItem>> allListIncMap = splitParam(allListInc);
        Map<String, Set<IncDecItem>> allListDecMap = splitParam(allListDec);
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(allListIncDecMap.keySet());
        allKeys.addAll(allListDecMap.keySet());
        allKeys.addAll(allListIncMap.keySet());
        for (String key : allKeys) {
            Set<IncDecItem> listIncDec = allListIncDecMap.get(key);
            Set<IncDecItem> listInc = allListIncMap.get(key);
            Set<IncDecItem> listDec = allListDecMap.get(key);
            if (listIncDec == null) {
                listIncDec = new HashSet<>();
            }
            if (listDec == null) {
                listDec = new HashSet<>();
            }
            if (listInc == null) {
                listInc = new HashSet<>();
            }
            if (verificationdays > 0) {
                if (rerun) {
                    new VerifyProfitUtil().getVerifyProfit(verificationdays, null, null, listInc, listDec, listIncDec, startoffset, componentInput.getConfig().getFindProfitManualThreshold(), param, stockDates, market);
                } else {
                    new VerifyProfitUtil().getVerifyProfit(verificationdays, null, null, listInc, listDec, listIncDec, startoffset, componentInput.getConfig().getFindProfitManualThreshold(), param, stockDates, market);                
                }
            }
            addHeader(componentInput, type, result, baseDateStr, futureDateStr, market);

            List<IclijServiceList> subLists = getServiceList(param.getService().conf.getMarket(), key, listInc, listDec, listIncDec);
            retLists.addAll(subLists);
        }

        retLists.add(memories);

        {
            List<TimingItem> currentTimings = (List<TimingItem>) myData.getTimingMap().get(market.getConfig().getMarket());
            if (currentTimings != null) {
                List<IclijServiceList> subLists2 = getServiceList(market.getConfig().getMarket(), currentTimings);
                retLists.addAll(subLists2);
            }
        }
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        IclijServiceList updates = convert(market.getConfig().getMarket(), updateMap);
        retLists.add(updates);

        Set<IncDecItem> currentIncDecs = new HashSet<>();
        List<IncDecItem> listAll = dbDao.getAllIncDecs();
        List<Market> markets = IclijXMLConfig.getMarkets(iclijConfig);
        markets = new MarketUtil().filterMarkets(markets, false);
        for (Market aMarket : markets) {
            if (rerun && !iclijConfig.wantsFindProfitRerunSave() && market.getConfig().getMarket().equals(aMarket.getConfig().getMarket()) ) {
                //List<IncDecItem> listIncDecs = new ArrayList<>();
                //listIncDecs.addAll(myData.getIncs());
                //listIncDecs.addAll(myData.getDecs());
                //listIncDecs = mergeList(listIncDecs, !rerun);
                currentIncDecs.addAll(allListInc);
                currentIncDecs.addAll(allListDec);
                currentIncDecs.addAll(allListIncDec);
                continue;
            }
            LocalDate date = param.getFutureDate();
            date = TimeUtil.getBackEqualBefore2(date, verificationdays, stockDates);
            List<IncDecItem> marketCurrentIncDecs = new MiscUtil().getCurrentIncDecs(date, listAll, aMarket, market.getConfig().getFindtime(), true);
            currentIncDecs.addAll(marketCurrentIncDecs);
        }
        //roundList(currentIncDecs);
        addRelations(componentInput, retLists, currentIncDecs, dbDao, iclijConfig);

        return result;
    }

    private static void addHeader(ComponentInput componentInput, String type, IclijServiceResult result,
            String basedate, String futuredate, Market market) {
        int offset = new ComponentTimeUtil().getFindProfitOffset(market, componentInput);
        IclijServiceList header = new IclijServiceList();
        result.getLists().add(header);
        header.setTitle(type + " " + "Market: " + componentInput.getConfig().getMarket() + " Date: " + componentInput.getConfig().getDate() + " Offset: " + offset + " Threshold: " + componentInput.getConfig().getFindProfitManualThreshold());
        IclijServiceList header2 = new IclijServiceList();
        result.getLists().add(header2);
        header2.setTitle(type + " " + "ML market: " + componentInput.getConfig().getMlmarket() + " Date: " + componentInput.getConfig().getDate() + " Offset: " + offset);
        List<MapList> aList = new ArrayList<>();
        header.setList(aList);
        MapList mapList = new MapList();
        mapList.setKey("Dates");
        mapList.setValue("Base " + basedate + " Future " + futuredate);
        aList.add(mapList);
    }

    @Deprecated // ?
    public static ControlService getService(ComponentInput input, int days) throws Exception {
        String market = input.getConfig().getMarket();
        String mlmarket = input.getConfig().getMlmarket();
        /*
        if (market == null) {
            throw new Exception("Market null");
        }
         */
        //LocalDate date = input.getConfig().getDate();
        ControlService srv = new ControlService(null);
        srv.getConfig();
        if (market != null) {
            srv.conf.setMarket(market);
            srv.conf.setMLmarket(mlmarket);
        }
        return srv;
    }

    private static Map<String, List<List<Double>>> getSimpleContent(IclijConfig iclijConfig, String market) throws Exception {
        ControlService srv = new ControlService(iclijConfig);
        srv.getConfig();
        if (market != null) {
            srv.conf.setMarket(market);
        }
        new MLUtil().disabler(srv.conf.getConfigValueMap());
        srv.conf.getConfigValueMap().put(ConfigConstants.MISCTHRESHOLD, null);
        Map<String, Map<String, Object>> result = srv.getContent();
        Integer cat = (Integer) result.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
        Map<String, List<List<Double>>> listMap = (Map<String, List<List<Double>>>) result.get("" + cat).get(PipelineConstants.LIST);
        return listMap;
    }

    @Deprecated
    private static boolean anythingHere3(Map<String, List<List<Double>>> listMap2, int size) {
        for (List<List<Double>> array : listMap2.values()) {
            if (size != Constants.OHLC || size != array.size()) {
                return false;
            }
            out:
                for (int i = 0; i < array.get(0).size(); i++) {
                    for (int j = 0; j < array.size() - 1; j++) {
                        if (array.get(j).get(i) == null) {
                            continue out;
                        }
                    }
                    return true;
                }
        }
        return false;
    }

    @Deprecated
    private static boolean wantThree(IclijConfig iclijConfig, String market) {
        try {
            Map<String, List<List<Double>>> listMap = getSimpleContent(iclijConfig, market);
            return anythingHere3(listMap, Constants.OHLC);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return false;
        }
    }

    /*
    if (config.wantsImproveProfit()) {
        ImproveProfitActionData improveProfitActionData = new ImproveProfitActionData();  
        getImprovements(retLists, market, date, save, improveProfitActionData, allMemoryItems);
    }
     */

    private static IclijServiceList convert(String marketName, Map<String, Object> updateMap) {
        IclijServiceList list = new IclijServiceList();
        list.setTitle("Updates for " + marketName);
        List<MapList> aList = new ArrayList<>();
        for (Entry<String, Object> map : updateMap.entrySet()) {
            MapList m = new MapList();
            m.setKey(map.getKey());
            m.setValue((String) map.getValue().toString()); 
            aList.add(m);
        }
        list.setList(aList);
        return list;
    }

    private static IclijServiceList convert(Map<String, Object> map) {
        IclijServiceList list = new IclijServiceList();
        list.setTitle("Trends");
        List<MapList> aList = new ArrayList<>();
        for (Entry<String, Object> entry : map.entrySet()) {
            MapList m = new MapList();
            m.setKey(entry.getKey());
            m.setValue(entry.getValue().toString()); 
            aList.add(m);
        }
        list.setList(aList);
        return list;
    }

    private static LocalDate getDateIndex(List<String> stocks, int index) throws ParseException {
        String newDate = stocks.get(index);
        return TimeUtil.convertDate(newDate);
    }



    /*
    @Deprecated
    private static void getImprovements(List<IclijServiceList> retLists, ComponentData param,
            ImproveProfitActionData improveProfitActionData, List<MemoryItem> allMemoryItems) throws Exception {
        Map<String, String> map = improveProfitActionData.getMarket(param, allMemoryItems);        
        List<MapList> mapList = improveProfitActionData.getList(map);
        IclijServiceList resultMap = new IclijServiceList();
        resultMap.setTitle("Improve Profit Info");
        resultMap.setList(mapList);
        retLists.add(resultMap);
    }
     */

    public static IclijServiceResult getFindProfit(IclijConfig iclijConfig, ComponentInput componentInput, IclijDbDao dbDao) throws Exception {
        String type = "FindProfit";
        int days = 0;  // config.verificationDays();
        //componentInput.setDoSave(false);
        boolean rerun = componentInput.getConfig().singlemarketRerun();
        IclijServiceResult result = getFindProfitVerify(iclijConfig, componentInput, type, days, rerun, dbDao);
        new MiscUtil().print(result);
        return result;
    }

    public static IclijServiceResult getImproveProfit(ComponentInput componentInput, IclijDbDao dbDao, IclijConfig iclijConfig) throws Exception {
        try {
            int loopOffset = 0;
            int days = 0; // config.verificationDays();
            IclijServiceResult result = new IclijServiceResult();
            result.setLists(new ArrayList<>());
            List<IclijServiceList> retLists = result.getLists();

            ComponentData param = null;
            try {
                param = ComponentData.getParam(iclijConfig, componentInput, 0);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                return result;
            }

            componentInput.setDoSave(false);
            //FindProfitActionData findProfitActionData = new FindProfitActionData();
            ImproveProfitActionData improveProfitActionData = new ImproveProfitActionData(iclijConfig, dbDao);  
            List<MemoryItem> allMemoryItems = new ArrayList<>(); // getMemoryItems(componentInput.getConfig(), param, days, getImproveProfitComponents(componentInput.getConfig()));
            //IclijServiceList memories = new IclijServiceList();
            //memories.setTitle("Memories");
            //memories.setList(allMemoryItems);
            Map<String, Object> updateMap = new HashMap<>();
            Market market = new MarketUtil().findMarket(param.getService().conf.getMarket(), iclijConfig);
            WebData webData = param.getService().getRun(IclijConstants.IMPROVEPROFIT, componentInput);
            List<MapList> mapList = new MiscUtil().getList(webData.getUpdateMap());
            IclijServiceList resultMap = new IclijServiceList();
            resultMap.setTitle("Improve Profit Info");
            resultMap.setList(mapList);
            retLists.add(resultMap);

            //retLists.add(memories);

            List<IclijServiceList> lists = new ArrayList<>();
            Map<String, List<TimingItem>> timingMap = webData.getTimingMap();
            for (Entry<String, List<TimingItem>> entry : timingMap.entrySet()) {
                String marketName = entry.getKey();
                List<TimingItem> list = (List<TimingItem>) entry.getValue();
                List<IclijServiceList> subLists = getServiceList(marketName, list);
                lists.addAll(subLists);
            }

            Map<String, List<TimingItem>> timingMap2 = webData.getTimingMap2();
            for (Entry<String, List<TimingItem>> entry : timingMap2.entrySet()) {
                String marketName = entry.getKey();
                List<TimingItem> list = (List<TimingItem>) entry.getValue();
                List<IclijServiceList> subLists = getServiceList(marketName + " sell", list);
                lists.addAll(subLists);
            }

            result.setLists(lists);

            updateMap = webData.getUpdateMap();
            Map<String, Map<String, Object>> mapmaps = new HashMap<>();
            mapmaps.put("ml", updateMap);
            result.setMaps(mapmaps);
            IclijServiceList updates = convert(null, updateMap);
            lists.add(updates);

            updateMap = webData.getUpdateMap2();
            updates = convert(null, updateMap);
            lists.add(updates);

            return result;
        } catch (Exception e) {
            log.error("Ex", e);
        }
        return null;
    }

    private static LocalDate getLastDate(List<String> stocks) throws ParseException {
        String aDate = stocks.get(stocks.size() - 1);
        return TimeUtil.convertDate(aDate);
    }

    private static int getDateIndex(LocalDate date, List<String> stocks) {
        int index;
        if (date == null) {
            index = stocks.size() - 1;
        } else {
            String aDate = TimeUtil.convertDate2(date);
            index = stocks.indexOf(aDate);
        }
        return index;
    }

    private static int getDateOffset(LocalDate date, List<String> stocks) {
        int offset = 0;
        if (date != null) {
            String aDate = TimeUtil.convertDate2(date);
            int index = stocks.indexOf(aDate);
            if (index >= 0) {
                offset = stocks.size() - 1 - index;
            }
        }
        return offset;
    }

    /*
    public static List<MemoryItem> getMemoryItemsNot(IclijConfig config, ComponentData param, int days, List<String> components) throws InterruptedException {
        List<MemoryItem> allMemoryItems = new ArrayList<>();
        UpdateDBActionData updateDbActionData = new UpdateDBActionData();
        Queue<ActionData> serviceActionDatas = updateDbActionData.findAllMarketComponentsToCheck(param, days, config, components);
        for (ActionData serviceActionData : serviceActionDatas) {
            serviceActionData.goal(null, param, null);
            Map<String, Object> resultMap = serviceActionData.getLocalResultMap();
            List<MemoryItem> memoryItems = (List<MemoryItem>) resultMap.get(IclijPipelineConstants.MEMORY);
            if (memoryItems != null) {
                allMemoryItems.addAll(memoryItems);
            } else {
                log.error("Memory null");
            }
        }
        return allMemoryItems;
    }
     */

    private static void wipeFields(Collection<IncDecItem> list) {
        for (IncDecItem item : list) {
            item.setComponent(null);
            item.setLocalcomponent(null);
            item.setSubcomponent(null);
        }
    }
    
    private static void roundList(Collection<IncDecItem> list) {
        for (IncDecItem item : list) {
            Double score = item.getScore();
            if (score != null) {
                item.setScore(MathUtil.round2(score, 3));
            }
        }        
    }

    private static void roundList2(List<TimingItem> list) {
        for (TimingItem item : list) {
            Double score = item.getScore();
            if (score != null) {
                item.setScore(MathUtil.round2(score, 3));
            }
        }        
    }

    private static void roundList3(List<MemoryItem> list) {
        for (MemoryItem item : list) {
            Double testaccuracy = item.getTestaccuracy();
            if (testaccuracy != null) {
                item.setTestaccuracy(MathUtil.round2(testaccuracy, 3));
            }
            Double testloss = item.getTestloss();
            if (testloss != null) {
                item.setTestloss(MathUtil.round2(testloss, 3));
            }
            Double confidence = item.getConfidence();
            if (confidence != null) {
                item.setConfidence(MathUtil.round2(confidence, 3));
            }
            Double learnConfidence = item.getLearnConfidence();
            if (learnConfidence != null) {
                item.setLearnConfidence(MathUtil.round2(learnConfidence, 3));
            }
            Double tpConf = item.getTpConf();
            if (tpConf != null) {
                item.setTpConf(MathUtil.round2(tpConf, 3));
            }
            Double tpProb = item.getTpProb();
            if (tpProb != null) {
                item.setTpProb(MathUtil.round2(tpProb, 3));
            }
            Double tpProbConf = item.getTpProbConf();
            if (tpProbConf != null) {
                item.setTpProbConf(MathUtil.round2(tpProbConf, 3));
            }
            Double tnConf = item.getTnConf();
            if (tnConf != null) {
                item.setTnConf(MathUtil.round2(tnConf, 3));
            }
            Double tnProb = item.getTnProb();
            if (tnProb != null) {
                item.setTnProb(MathUtil.round2(tnProb, 3));
            }
            Double tnProbConf = item.getTnProbConf();
            if (tnProbConf != null) {
                item.setTnProbConf(MathUtil.round2(tnProbConf, 3));
            }
            Double fpConf = item.getFpConf();
            if (fpConf != null) {
                item.setFpConf(MathUtil.round2(fpConf, 3));
            }
            Double fpProb = item.getFpProb();
            if (fpProb != null) {
                item.setFpProb(MathUtil.round2(fpProb, 3));
            }
            Double fpProbConf = item.getFpProbConf();
            if (fpProbConf != null) {
                item.setFpProbConf(MathUtil.round2(fpProbConf, 3));
            }
            Double fnConf = item.getFnConf();
            if (fnConf != null) {
                item.setFnConf(MathUtil.round2(fnConf, 3));
            }
            Double fnProb = item.getFnProb();
            if (fnProb != null) {
                item.setFnProb(MathUtil.round2(fnProb, 3));
            }
            Double fnProbConf = item.getFnProbConf();
            if (fnProbConf != null) {
                item.setFnProbConf(MathUtil.round2(fnProbConf, 3));
            }
        }        
    }

    private static void roundList4(List<MLMetricsItem> list) {
        for (MLMetricsItem item : list) {
            Double loss = item.getLoss();
            if (loss != null) {
                item.setLoss(MathUtil.round2(loss, 3));
            }
            Double accuracy = item.getTestAccuracy();
            if (accuracy != null) {
                item.setTestAccuracy(MathUtil.round2(accuracy, 3));
            }
            Double trainaccuracy = item.getTrainAccuracy();
            if (trainaccuracy != null) {
                item.setTrainAccuracy(MathUtil.round2(trainaccuracy, 3));
            }
        }        
    }

    public static Map<String, Set<IncDecItem>> splitParam(Collection<IncDecItem> items) {
        Map<String, Set<IncDecItem>> mymap = new HashMap<String, Set<IncDecItem>>();
        for (IncDecItem item : items) {
            String key = item.getParameters();
            Set<IncDecItem> itemlist = mymap.get(key);
            if (itemlist == null) {
                itemlist = new HashSet<>();
                mymap.put(key, itemlist);
            }
            itemlist.add(item);
        }
        return mymap;
    }


}
