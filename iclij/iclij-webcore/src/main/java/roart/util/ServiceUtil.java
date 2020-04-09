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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.config.MarketFilter;
import roart.iclij.factory.actioncomponentconfig.ActionComponentConfigFactory;
import roart.iclij.factory.actioncomponentconfig.ComponentMap;
import roart.iclij.util.TrendUtil;
import roart.iclij.util.VerifyProfitUtil;
import roart.common.config.ConfigConstants;
import roart.common.config.MyConfig;
import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.MetaUtil;
import roart.common.util.TimeUtil;
import roart.constants.IclijConstants;
import roart.constants.IclijPipelineConstants;
import roart.db.IclijDbDao;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MapList;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.TimingItem;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.action.DatasetActionData;
import roart.iclij.model.action.EvolveActionData;
import roart.iclij.model.action.ImproveFilterActionData;
import roart.iclij.model.action.ImproveProfitActionData;
import roart.iclij.model.action.MachineLearningActionData;
import roart.iclij.model.action.FindProfitActionData;
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
    
    public static IclijServiceResult getConfig() throws Exception {
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        IclijServiceResult result = new IclijServiceResult();
        result.setIclijConfig(instance);
        return result;
    }
    
    public static IclijServiceResult getContent(ComponentInput componentInput) throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        FindProfitActionData findProfitActionData = new FindProfitActionData();

        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig i = new IclijXMLConfig();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IncDecItem> listAll = IclijDbDao.getAllIncDecs();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        Map<String, Object> trendMap = new HashMap<>();
        List<Market> markets = conf.getMarkets(instance);
        markets = new MarketUtil().filterMarkets(markets, findProfitActionData.isDataset());
        for (Market market : markets) {
            ControlService srv = new ControlService();
            srv.conf.setMarket(market.getConfig().getMarket());
            // the market may be incomplete, the exception and skip
            try {
                Short mystartoffset = market.getConfig().getStartoffset();
                short startoffset = mystartoffset != null ? mystartoffset : 0;
                Trend trend = new TrendUtil().getTrend(instance.verificationDays(), date, srv, startoffset);
                trendMap.put(market.getConfig().getMarket(), trend);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<IncDecItem> currentIncDecs = new MiscUtil().getCurrentIncDecs(date, listAll, market, market.getConfig().getFindtime());
            List<IncDecItem> listInc = currentIncDecs.stream().filter(m -> m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listDec = currentIncDecs.stream().filter(m -> !m.isIncrease()).collect(Collectors.toList());
            listInc = mergeList(listInc, false);
            listDec = mergeList(listDec, false);
            List<IncDecItem> listIncDec = new MiscUtil().moveAndGetCommon(listInc, listDec, true);
            List<IclijServiceList> subLists = getServiceList(market.getConfig().getMarket(), listInc, listDec, listIncDec);
            lists.addAll(subLists);
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

        addRelations(componentInput, lists, new ArrayList<>());
        
        new MiscUtil().print(result);
        return result;
    }

    private static void addRelations(ComponentInput componentInput, List<IclijServiceList> lists, List<IncDecItem> listIncDecs) throws Exception {
        List[] objects = new RelationUtil().method(componentInput, listIncDecs);
        
        IclijServiceList incdecs = new IclijServiceList();
        incdecs.setTitle("Incdecs with relations");
        incdecs.setList(objects[0]);

        IclijServiceList relations = new IclijServiceList();
        relations.setTitle("Relations");
        relations.setList(objects[1]);

        lists.add(incdecs);
        lists.add(relations);
    }

    public static IclijServiceResult getContentImprove(ComponentInput componentInput) throws Exception {
        ImproveProfitActionData improveProfitActionData = new ImproveProfitActionData();
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig i = new IclijXMLConfig();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IncDecItem> listAll = IclijDbDao.getAllIncDecs();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        markets = new MarketUtil().filterMarkets(markets, improveProfitActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ControlService srv = null; 
        try {
            srv = getService(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        getContentTimings(date, lists, markets, improveProfitActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(componentInput, srv, updateMarketMap, updateMap, improveProfitActionData);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, improveProfitActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    public static IclijServiceResult getContentFilter(ComponentInput componentInput) throws Exception {
        ImproveFilterActionData improveFilterActionData = new ImproveFilterActionData();
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig i = new IclijXMLConfig();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IncDecItem> listAll = IclijDbDao.getAllIncDecs();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        markets = new MarketUtil().filterMarkets(markets, improveFilterActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ControlService srv = null; 
        try {
            srv = getService(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        getContentTimings(date, lists, markets, improveFilterActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(componentInput, srv, updateMarketMap, updateMap, improveFilterActionData);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, improveFilterActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    private static void getContentTimings(LocalDate date, List<IclijServiceList> lists, List<Market> markets, MarketActionData action)
            throws Exception {
        List<TimingItem> listAllTimings = IclijDbDao.getAllTiming();
        for (Market market : markets) {
            List<TimingItem> currentTimings = new MiscUtil().getCurrentTimings(date, listAllTimings, market, action.getName(), action.getTime(market));
            List<IclijServiceList> subLists = getServiceList(market.getConfig().getMarket(), currentTimings);
            lists.addAll(subLists);
        }
    }

    private static void getUpdateMarkets(ComponentInput componentInput, ControlService srv,
            Map<String, Map<String, Object>> updateMarketMap, Map<String, Object> updateMap, MarketActionData actionData)
            throws Exception {
        //Market market = findProfitActionData.findMarket(param);
        //String marketName = market.getConfig().getMarket();
        long time0 = System.currentTimeMillis();
        List<MetaItem> metas = srv.getMetas();
        for (Market market : new MarketUtil().getMarkets(actionData.isDataset())) {
            String marketName = market.getConfig().getMarket();
            MetaItem meta = new MetaUtil().findMeta(metas, marketName);
            boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
            List<String> componentList = actionData.getComponents(componentInput.getConfig(), wantThree);
            Map<Boolean, String> booleanTexts = actionData.getBooleanTexts();
            Boolean[] booleans = actionData.getBooleans();
            for (Boolean bool : booleans) {
                updateMarketMap.put(market.getConfig().getMarket() + " " + booleanTexts.get(bool), new HashMap<>());
                Map<String, ActionComponentConfig> componentMap = new ComponentMap().getComponentMap(componentList, actionData.getName());
                for (ActionComponentConfig component : componentMap.values()) {
                    List<String> subcomponents = new MLUtil().getSubComponents(market, componentInput.getConfig(), null, component);
                    for (String subcomponent : subcomponents) {
                        Map<String, Object> anUpdateMap = new MiscUtil().loadConfig(srv, componentInput, market, market.getConfig().getMarket(), actionData.getName(), actionData.getName(), false, bool, subcomponent, actionData, null);
                        updateMarketMap.get(market.getConfig().getMarket() + " " + booleanTexts.get(bool)).putAll(anUpdateMap);
                        updateMap.putAll(anUpdateMap);
                    }
                }
            }
        }
        log.info("Gettings {}", (System.currentTimeMillis() - time0) / 1000);
    }

    public static IclijServiceResult getContentEvolve(ComponentInput componentInput) throws Exception {
        EvolveActionData evolveActionData = new EvolveActionData();
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

         List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        markets = new MarketUtil().filterMarkets(markets, evolveActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ControlService srv = null; 
        try {
            srv = getService(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        getContentTimings(date, lists, markets, evolveActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(componentInput, srv, updateMarketMap, updateMap, evolveActionData);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, evolveActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    public static IclijServiceResult getContentDataset(ComponentInput componentInput) throws Exception {
        DatasetActionData datasetActionData = new DatasetActionData();
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

         List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        markets = new MarketUtil().filterMarkets(markets, datasetActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ControlService srv = null; 
        try {
            srv = getService(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        getContentTimings(date, lists, markets, datasetActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(componentInput, srv, updateMarketMap, updateMap, datasetActionData);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, datasetActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    public static IclijServiceResult getContentCrosstest(ComponentInput componentInput) throws Exception {
        CrossTestActionData crossTestActionData = new CrossTestActionData();
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        markets = new MarketUtil().filterMarkets(markets, crossTestActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ControlService srv = null; 
        try {
            srv = getService(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        getContentTimings(date, lists, markets, crossTestActionData);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(componentInput, srv, updateMarketMap, updateMap, crossTestActionData);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, crossTestActionData, false);
        new MiscUtil().print(result);
        return result;
    }

    private static void getContentMemoriesUpdates(ComponentInput componentInput, List<IclijServiceList> lists,
            Map<String, Map<String, Object>> updateMarketMap, MarketActionData actionData, boolean useMemory) {
        for (Market market : new MarketUtil().getMarkets(actionData.isDataset())) {
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
            List<MemoryItem> marketMemory = new MarketUtil().getMarketMemory(market);
            List<MemoryItem> currentList = new MiscUtil().filterKeepRecent(marketMemory, componentInput.getEnddate(), actionData.getTime(market));
            
            IclijServiceList memories = new IclijServiceList();
            memories.setTitle("Memories " + marketName);
            memories.setList(currentList);
            lists.add(memories);
        }
    }

    public static IclijServiceResult getContentMachineLearning(ComponentInput componentInput) throws Exception {
        MachineLearningActionData mlActionData = new MachineLearningActionData();
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        markets = new MarketUtil().filterMarkets(markets, mlActionData.isDataset());
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        getContentTimings(date, lists, markets, mlActionData);
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
        if (!listIncDec.isEmpty()) {
            IclijServiceList incDec = new IclijServiceList();
            incDec.setTitle(market + " " + "timing" + " " + listIncDec.stream().mapToDouble(TimingItem::getMytime).summaryStatistics());
            incDec.setList(listIncDec);
            subLists.add(incDec);
        }
        return subLists;
    }

    static List<IclijServiceList> getServiceList(String market, List<IncDecItem> listInc, List<IncDecItem> listDec,
            List<IncDecItem> listIncDec) {
        List<IclijServiceList> subLists = new ArrayList<>();
        if (!listInc.isEmpty()) {
            List<Boolean> listIncBoolean = listInc.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
            long count = listIncBoolean.stream().filter(i -> i).count();                            
            IclijServiceList inc = new IclijServiceList();
            String trendStr = "";
            inc.setTitle(market + " " + "Increase ( verified " + count + " / " + listIncBoolean.size() + " )" + trendStr);
            inc.setList(listInc);
            subLists.add(inc);
        }
        if (!listDec.isEmpty()) {
            List<Boolean> listDecBoolean = listDec.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
            long count = listDecBoolean.stream().filter(i -> i).count();                            
            IclijServiceList dec = new IclijServiceList();
            dec.setTitle(market + " " + "Decrease ( verified " + count + " / " + listDecBoolean.size() + " )");
            dec.setList(listDec);
            subLists.add(dec);
        }
        if (!listIncDec.isEmpty()) {
            List<Boolean> listIncDecBoolean = listIncDec.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
            long count = listIncDecBoolean.stream().filter(i -> i).count();                            
            IclijServiceList incDec = new IclijServiceList();
            incDec.setTitle(market + " " + "Increase and decrease ( verified " + count + " / " + listIncDecBoolean.size() + " )" );
            incDec.setList(listIncDec);
            subLists.add(incDec);
        }
        return subLists;
    }

    private static List<IncDecItem> mergeList(List<IncDecItem> itemList, boolean splitid) {
        Map<String, IncDecItem> map = new HashMap<>();
        for (IncDecItem item : itemList) {
            String id;
            if (!splitid) {
                id = item.getId();
            } else {
                id = item.getId() + item.getDate().toString();
            }
            IncDecItem getItem = map.get(id);
            if (getItem == null) {
                map.put(id, item);
            } else {
                getItem.setScore(getItem.getScore() + item.getScore());
                getItem.setDescription(getItem.getDescription() + ", " + item.getDescription());
            }
        }
        return new ArrayList<>(map.values());
    }

    public static IclijServiceResult getVerify(ComponentInput componentInput) throws Exception {
        String type = "Verify";
        componentInput.setDoSave(componentInput.getConfig().wantVerificationSave());
        componentInput.setDoSave(false);
        int verificationdays = componentInput.getConfig().verificationDays();
        boolean rerun = componentInput.getConfig().verificationRerun();
        IclijServiceResult result = getFindProfitVerify(componentInput, type, verificationdays, rerun);
        new MiscUtil().print(result);
        return result;
    }

    private static IclijServiceResult getFindProfitVerify(ComponentInput componentInput, String type, int verificationdays, boolean rerun) throws Exception {

        IclijServiceResult result = new IclijServiceResult();
        result.setLists(new ArrayList<>());
        List<IclijServiceList> retLists = result.getLists();

        ControlService srv = null; 
        try {
            srv = getService(componentInput, verificationdays);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        //FindProfitActionData findProfitActionData = new FindProfitActionData();
	// this calculates, does not read from db
        List<MemoryItem> allMemoryItems = new ArrayList<>();
        //ProfitData picks = findProfitActionData.getPicks(param, allMemoryItems);
        //getMemoryItems(componentInput.getConfig(), param, verificationdays, getFindProfitComponents(componentInput.getConfig()));
        IclijServiceList memories = new IclijServiceList();
        memories.setTitle("Memories");
        memories.setList(allMemoryItems);
        Map<String, Object> updateMap = new HashMap<>();
        //param.setUpdateMap(updateMap);
        Market market = new MarketUtil().findMarket(srv.conf.getMarket());
        boolean evolve = new MiscUtil().getEvolve(verificationdays, componentInput);
        WebData myData;
        if (rerun) {
            myData = srv.getRun(IclijConstants.FINDPROFIT, componentInput);
            //ProfitData buysells = myData.profitData; // findProfitActionData.getPicks(param, allMemoryItems);
        } else {
            myData = srv.getVerify(IclijConstants.FINDPROFIT, componentInput);            
        }
        updateMap = myData.getUpdateMap();
        allMemoryItems.addAll(myData.getMemoryItems());
        
        List<IncDecItem> listInc = new ArrayList<>(myData.getIncs());
        List<IncDecItem> listDec = new ArrayList<>(myData.getDecs());
        listInc = mergeList(listInc, !rerun);
        listDec = mergeList(listDec, !rerun);
        List<IncDecItem> listIncDec;
        if (rerun) {
            listIncDec = new MiscUtil().moveAndGetCommon(listInc, listDec, verificationdays > 0);
        } else {
            listIncDec = new MiscUtil().moveAndGetCommon2(listInc, listDec, verificationdays > 0);
        }
        Map<String, Object> trendMap = new HashMap<>();
        Short mystartoffset = market.getConfig().getStartoffset();
        short startoffset = mystartoffset != null ? mystartoffset : 0;
        int loopoffset = componentInput.getLoopoffset() != null ? componentInput.getLoopoffset() : 0;
        String dateString = TimeUtil.convertDate2(componentInput.getEnddate());
        List<String> stockDates = srv.getDates(market.getConfig().getMarket());
        int dateIndex = TimeUtil.getIndexEqualBefore(stockDates, dateString);
        String aDate = stockDates.get(dateIndex - loopoffset);
        LocalDate endDate = TimeUtil.convertDate(aDate);
        Trend trend;
        Trend trend2;
        if (rerun) {
            String endDateString2 = stockDates.get(dateIndex - market.getConfig().getFindtime());
            LocalDate endDate2 = TimeUtil.convertDate(endDateString2);
            trend = new TrendUtil().getTrend(componentInput.getConfig().verificationDays(), endDate2, srv, startoffset);
            trend2 = new TrendUtil().getTrend(componentInput.getConfig().verificationDays(), endDate, srv, startoffset);
        } else {
            LocalDate prevdate = componentInput.getEnddate();
            String prevdateString = TimeUtil.convertDate2(prevdate);
            int prevdateIndex = TimeUtil.getIndexEqualBefore(stockDates, prevdateString);
            prevdateIndex = prevdateIndex - componentInput.getLoopoffset();
            Short startoffset2 = market.getConfig().getStartoffset();
            startoffset2 = startoffset2 != null ? startoffset2 : 0;
            prevdateIndex = prevdateIndex - verificationdays - startoffset2;
            prevdateString = stockDates.get(prevdateIndex);
            String olddateString = stockDates.get(prevdateIndex - market.getConfig().getFindtime());
            LocalDate olddate = null;
            try {
                prevdate = TimeUtil.convertDate(prevdateString);
                olddate = TimeUtil.convertDate(olddateString);
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }
            trend = new TrendUtil().getTrend(componentInput.getConfig().verificationDays(), olddate, srv, startoffset2, stockDates, loopoffset);            
            trend2 = new TrendUtil().getTrend(componentInput.getConfig().verificationDays(), prevdate, srv, startoffset2, stockDates, loopoffset);            
         }
        trendMap.put(market.getConfig().getMarket(), trend);
        trendMap.put(market.getConfig().getMarket() + "end", trend2);
        int offset = 0;
        int futuredays = 0;
        List<String> stockdates = srv.getDates(market.getConfig().getMarket());
        List<String> list = new TimeUtil().setDates(TimeUtil.convertDate2(componentInput.getEnddate()), stockdates, offset, componentInput.getLoopoffset(), futuredays);
        String baseDateStr = list.get(0);
        String futureDateStr = list.get(1);
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        LocalDate baseDate = TimeUtil.convertDate(baseDateStr);
        LocalDate futureDate = TimeUtil.convertDate(futureDateStr);
        if (verificationdays > 0) {
            try {
                //srv.setFuturedays(0);
                //srv.setOffset(0);
                //srv.setDates(0, 0, TimeUtil.convertDate2(componentInput.getEnddate()));
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }           
            if (rerun) {
                new VerifyProfitUtil().getVerifyProfit(verificationdays, futureDate, srv, baseDate, listInc, listDec, listIncDec, startoffset, componentInput.getConfig().getFindProfitManualThreshold());
            } else {
                new VerifyProfitUtil().getVerifyProfit(verificationdays, futureDate, srv, baseDate, listInc, listDec, listIncDec, startoffset, componentInput.getConfig().getFindProfitManualThreshold(), stockDates, loopoffset);                
            }
            /*
            List<MapList> inc = new ArrayList<>();
            List<MapList> dec = new ArrayList<>();
            IclijServiceList incMap = new IclijServiceList();
            incMap.setTitle("Increase verify");
            incMap.setList(inc);
            IclijServiceList decMap = new IclijServiceList();
            decMap.setTitle("Decrease verify");
            decMap.setList(dec);
            retLists.add(incMap);
            retLists.add(decMap);
            */
        }
        addHeader(componentInput, type, result, baseDateStr, futureDateStr);
        
        List<IclijServiceList> subLists = getServiceList(srv.conf.getMarket(), listInc, listDec, listIncDec);
        retLists.addAll(subLists);
        
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

        List<IncDecItem> listIncDecs = new ArrayList<>(myData.getIncs());
        listIncDecs.addAll(myData.getDecs());
        
        addRelations(componentInput, retLists, listIncDecs);

        return result;
    }

    private static void addHeader(ComponentInput componentInput, String type, IclijServiceResult result,
            String basedate, String futuredate) {
        IclijServiceList header = new IclijServiceList();
        result.getLists().add(header);
        header.setTitle(type + " " + "Market: " + componentInput.getConfig().getMarket() + " Date: " + componentInput.getConfig().getDate() + " Offset: " + componentInput.getLoopoffset());
        IclijServiceList header2 = new IclijServiceList();
        result.getLists().add(header2);
        header2.setTitle(type + " " + "ML market: " + componentInput.getConfig().getMlmarket() + " Date: " + componentInput.getConfig().getDate() + " Offset: " + componentInput.getLoopoffset());
        List<MapList> aList = new ArrayList<>();
        header.setList(aList);
        MapList mapList = new MapList();
        mapList.setKey("Dates");
        mapList.setValue("Base " + basedate + " Future " + futuredate);
        aList.add(mapList);
    }

    public static ControlService getService(ComponentInput input, int days) throws Exception {
        String market = input.getConfig().getMarket();
        String mlmarket = input.getConfig().getMlmarket();
        /*
        if (market == null) {
            throw new Exception("Market null");
        }
        */
        //LocalDate date = input.getConfig().getDate();
        ControlService srv = new ControlService();
        srv.getConfig();
        if (market != null) {
            srv.conf.setMarket(market);
            srv.conf.setMLmarket(mlmarket);
        }
        return srv;
    }

    private static Map<String, List<List<Double>>> getSimpleContent(String market) throws Exception {
        ControlService srv = new ControlService();
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

    private static boolean anythingHere3(Map<String, List<List<Double>>> listMap2, int size) {
        for (List<List<Double>> array : listMap2.values()) {
            if (size != 3 || size != array.size()) {
                return false;
            }
            out:
            for (int i = 0; i < array.get(0).size(); i++) {
                for (int j = 0; j < array.size(); j++) {
                    if (array.get(j).get(i) == null) {
                        continue out;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static boolean wantThree(String market) {
        try {
            Map<String, List<List<Double>>> listMap = getSimpleContent(market);
            return anythingHere3(listMap, 3);
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
    
    public static IclijServiceResult getFindProfit(ComponentInput componentInput) throws Exception {
	String type = "FindProfit";
        int days = 0;  // config.verificationDays();
        componentInput.setDoSave(false);
        boolean rerun = componentInput.getConfig().singlemarketRerun();
        IclijServiceResult result = getFindProfitVerify(componentInput, type, days, rerun);
        new MiscUtil().print(result);
        return result;
    }

    public static IclijServiceResult getImproveProfit(ComponentInput componentInput) throws Exception {
        try {
        int loopOffset = 0;
        int days = 0; // config.verificationDays();
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(new ArrayList<>());
        List<IclijServiceList> retLists = result.getLists();

        ControlService srv = null; 
        try {
            srv = getService(componentInput, days);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        componentInput.setDoSave(false);
        //FindProfitActionData findProfitActionData = new FindProfitActionData();
        ImproveProfitActionData improveProfitActionData = new ImproveProfitActionData();  
        List<MemoryItem> allMemoryItems = new ArrayList<>(); // getMemoryItems(componentInput.getConfig(), param, days, getImproveProfitComponents(componentInput.getConfig()));
        //IclijServiceList memories = new IclijServiceList();
        //memories.setTitle("Memories");
        //memories.setList(allMemoryItems);
        Map<String, Object> updateMap = new HashMap<>();
        Market market = new MarketUtil().findMarket(srv.conf.getMarket());
        WebData webData = srv.getRun(IclijConstants.IMPROVEPROFIT, componentInput);
        List<MapList> mapList = new MiscUtil().getList(webData.getUpdateMap());
        IclijServiceList resultMap = new IclijServiceList();
        resultMap.setTitle("Improve Profit Info");
        resultMap.setList(mapList);
        retLists.add(resultMap);

        //retLists.add(memories);

        List<IclijServiceList> lists = new ArrayList<>();
        Map<String, Object> timingMap = webData.getTimingMap();
        for (Entry<String, Object> entry : timingMap.entrySet()) {
            String marketName = entry.getKey();
            List<TimingItem> list = (List<TimingItem>) entry.getValue();
            List<IclijServiceList> subLists = getServiceList(marketName, list);
            lists.addAll(subLists);
        }

        Map<String, Object> timingMap2 = webData.getTimingMap2();
        for (Entry<String, Object> entry : timingMap2.entrySet()) {
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

}
