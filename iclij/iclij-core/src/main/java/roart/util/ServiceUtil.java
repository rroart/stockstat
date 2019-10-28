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

import roart.action.Action;
import roart.action.EvolveAction;
import roart.action.FindProfitAction;
import roart.action.ImproveProfitAction;
import roart.action.MachineLearningAction;
import roart.action.MarketAction;
import roart.action.WebData;
import roart.action.UpdateDBAction;
import roart.iclij.config.IclijConfig;
import roart.common.config.MyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentMLMACD;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.config.MarketFilter;
import roart.constants.IclijConstants;
import roart.constants.IclijPipelineConstants;
import roart.db.IclijDbDao;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MapList;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.TimingItem;
import roart.iclij.model.Trend;
import roart.iclij.service.IclijServiceList;
import roart.iclij.service.IclijServiceResult;
import roart.service.ControlService;
import roart.service.model.ProfitData;

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

        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig i = new IclijXMLConfig();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IncDecItem> listAll = IclijDbDao.getAllIncDecs();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        Map<String, Object> trendMap = new HashMap<>();
        List<Market> markets = conf.getMarkets(instance);
        for (Market market : markets) {
            ComponentData param = null;
            try {
                param = ServiceUtil.getParam(componentInput, 0);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            param.setAction(IclijConstants.FINDPROFIT);
            ControlService srv = new ControlService();
            //srv.getConfig();
            param.setService(srv);
            //param.setOffset(0);
            srv.conf.setMarket(market.getConfig().getMarket());
            // the market may be incomplete, the exception and skip
            try {
                Short mystartoffset = market.getConfig().getStartoffset();
                short startoffset = mystartoffset != null ? mystartoffset : 0;
                Trend trend = new FindProfitAction().getTrend(instance.verificationDays(), date, param.getService(), startoffset);
                trendMap.put(market.getConfig().getMarket(), trend);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<IncDecItem> currentIncDecs = getCurrentIncDecs(date, listAll, market, market.getConfig().getFindtime());
            List<IncDecItem> listInc = currentIncDecs.stream().filter(m -> m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listDec = currentIncDecs.stream().filter(m -> !m.isIncrease()).collect(Collectors.toList());
            listInc = mergeList(listInc);
            listDec = mergeList(listDec);
            List<IncDecItem> listIncDec = moveAndGetCommon(listInc, listDec);
            List<IclijServiceList> subLists = getServiceList(market.getConfig().getMarket(), listInc, listDec, listIncDec);
            lists.addAll(subLists);
        }
        result.setLists(lists);

        ComponentData param = null; 
        try {
            param = getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        FindProfitAction findProfitAction = new FindProfitAction();
        getContentTimings(date, lists, markets, findProfitAction);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        //getUpdateMarkets(componentInput, param, updateMarketMap, updateMap, findProfitAction);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, findProfitAction);

        IclijServiceList trends = convert(trendMap);
        lists.add(trends);

        addRelations(componentInput, lists, new ArrayList<>());
        
        print(result);
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
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig i = new IclijXMLConfig();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IncDecItem> listAll = IclijDbDao.getAllIncDecs();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ComponentData param = null; 
        try {
            param = getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        ImproveProfitAction improveProfitAction = new ImproveProfitAction();
        getContentTimings(date, lists, markets, improveProfitAction);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(componentInput, param, updateMarketMap, updateMap, improveProfitAction);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, improveProfitAction);
        print(result);
        return result;
    }

    private static void getContentTimings(LocalDate date, List<IclijServiceList> lists, List<Market> markets, MarketAction action)
            throws Exception {
        List<TimingItem> listAllTimings = IclijDbDao.getAllTiming();
        for (Market market : markets) {
            List<TimingItem> currentTimings = getCurrentTimings(date, listAllTimings, market, action.getName(), action.getTime(market));
            List<IclijServiceList> subLists = getServiceList(market.getConfig().getMarket(), currentTimings);
            lists.addAll(subLists);
        }
    }

    private static void getUpdateMarkets(ComponentInput componentInput, ComponentData param,
            Map<String, Map<String, Object>> updateMarketMap, Map<String, Object> updateMap, MarketAction action)
            throws Exception {
        //Market market = findProfitAction.findMarket(param);
        //String marketName = market.getConfig().getMarket();
        List<String> componentList = getFindProfitComponents(componentInput.getConfig(), componentInput.getMarket());
        for (Market market : action.getMarkets()) {
            Map<Boolean, String> booleanTexts = action.getBooleanTexts();
            Boolean[] booleans = action.getBooleans();
            for (Boolean bool : booleans) {
                updateMarketMap.put(market.getConfig().getMarket() + " " + booleanTexts.get(bool), new HashMap<>());
                Map<String, Component> componentMap = action.getComponentMap(componentList, null);
                for (Component component : componentMap.values()) {
                    List<String> subcomponents = component.getSubComponents(market, param);
                    for (String subcomponent : subcomponents) {
                        Map<String, Object> anUpdateMap = loadConfig(param, market, market.getConfig().getMarket(), action.getName(), component.getPipeline(), false, bool, subcomponent);
                        updateMarketMap.get(market.getConfig().getMarket() + " " + booleanTexts.get(bool)).putAll(anUpdateMap);
                        updateMap.putAll(anUpdateMap);
                    }
                }
            }
        }
    }

    public static IclijServiceResult getContentEvolve(ComponentInput componentInput) throws Exception {
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

         List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ComponentData param = null; 
        try {
            param = getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        EvolveAction evolveAction = new EvolveAction();
        getContentTimings(date, lists, markets, evolveAction);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        getUpdateMarkets(componentInput, param, updateMarketMap, updateMap, evolveAction);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, evolveAction);
        print(result);
        return result;
    }

    private static void getContentMemoriesUpdates(ComponentInput componentInput, List<IclijServiceList> lists,
            Map<String, Map<String, Object>> updateMarketMap, MarketAction action) {
        for (Market market : action.getMarkets()) {
        //for (Entry<String, Map<String, Object>> entry : updateMarketMap.entrySet()) {
            String marketName = market.getConfig().getMarket();
            Map<Boolean, String> booleanTexts = action.getBooleanTexts();
            Boolean[] booleans = action.getBooleans();
            for (Boolean bool : booleans) {
                Map<String, Object> anUpdateMap = updateMarketMap.get(marketName + " " + booleanTexts.get(bool));
                if (anUpdateMap != null) {
                    IclijServiceList updates = convert(marketName + " " + booleanTexts.get(bool), anUpdateMap);
                    lists.add(updates);
                }
            }
            List<MemoryItem> marketMemory = action.getMarketMemory(market);
            List<MemoryItem> currentList = action.filterKeepRecent(marketMemory, componentInput.getEnddate(), action.getTime(market));
            
            IclijServiceList memories = new IclijServiceList();
            memories.setTitle("Memories " + marketName);
            memories.setList(currentList);
            lists.add(memories);
        }
    }

    public static IclijServiceResult getContentMachineLearning(ComponentInput componentInput) throws Exception {
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

         List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        ComponentData param = null; 
        try {
            param = getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        MachineLearningAction mlAction = new MachineLearningAction();
        getContentTimings(date, lists, markets, mlAction);
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        //getUpdateMarkets(componentInput, param, updateMarketMap, updateMap, mlAction);
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        getContentMemoriesUpdates(componentInput, lists, updateMarketMap, mlAction);
        print(result);
        return result;
   }
    
    public static List<TimingItem> getCurrentTimings(LocalDate date, List<TimingItem> listAll, Market market, String action, int days) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        LocalDate olddate = date.minusDays(days);
        List<TimingItem> filterListAll = listAll.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        filterListAll = filterListAll.stream().filter(m -> action.equals(m.getAction())).collect(Collectors.toList());
        List<TimingItem> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getDate()) <= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    public static List<IncDecItem> getCurrentIncDecs(LocalDate date, List<IncDecItem> listAll, Market market, int days) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        LocalDate olddate = date.minusDays(days);
        List<IncDecItem> filterListAll = listAll.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        List<IncDecItem> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getDate()) <= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
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
            incDec.setTitle(market + " " + "timing");
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
            IclijServiceList incDec = new IclijServiceList();
            incDec.setTitle(market + " " + "Increase and decrease");
            incDec.setList(listIncDec);
            subLists.add(incDec);
        }
        return subLists;
    }

    private static List<IncDecItem> mergeList(List<IncDecItem> itemList) {
        Map<String, IncDecItem> map = new HashMap<>();
        for (IncDecItem item : itemList) {
            IncDecItem getItem = map.get(item.getId());
            if (getItem == null) {
                map.put(item.getId(), item);
            } else {
                getItem.setScore(getItem.getScore() + item.getScore());
                getItem.setDescription(getItem.getDescription() + ", " + item.getDescription());
            }
        }
        return new ArrayList<>(map.values());
    }

    public static List<IncDecItem> moveAndGetCommon(List<IncDecItem> listInc, List<IncDecItem> listDec) {
        // and a new list for common items
        List<String> incIds = listInc.stream().map(IncDecItem::getId).collect(Collectors.toList());
        List<String> decIds = listDec.stream().map(IncDecItem::getId).collect(Collectors.toList());
        List<String> commonIds = new ArrayList<>(incIds);
        commonIds.retainAll(decIds);
        List<IncDecItem> common = listInc.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toList());
        common.addAll(listDec.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toList()));
        listInc.removeAll(common);
        listDec.removeAll(common);
        return common;
    }

    public static IclijServiceResult getVerify(ComponentInput componentInput) throws Exception {
	String type = "Verify";
        componentInput.setDoSave(componentInput.getConfig().wantVerificationSave());
        int verificationdays = componentInput.getConfig().verificationDays();
        IclijServiceResult result = getFindProfitVerify(componentInput, type, verificationdays);
        print(result);
        return result;
    }

    private static IclijServiceResult getFindProfitVerify(ComponentInput componentInput, String type, int verificationdays) throws Exception {

        IclijServiceResult result = new IclijServiceResult();
        result.setLists(new ArrayList<>());
        List<IclijServiceList> retLists = result.getLists();

        ComponentData param = null; 
        try {
            param = getParam(componentInput, verificationdays);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        
        FindProfitAction findProfitAction = new FindProfitAction();
	// this calculates, does not read from db
        List<MemoryItem> allMemoryItems = new ArrayList<>();
        //ProfitData picks = findProfitAction.getPicks(param, allMemoryItems);
        //getMemoryItems(componentInput.getConfig(), param, verificationdays, getFindProfitComponents(componentInput.getConfig()));
        IclijServiceList memories = new IclijServiceList();
        memories.setTitle("Memories");
        memories.setList(allMemoryItems);
        Map<String, Object> updateMap = new HashMap<>();
        //param.setUpdateMap(updateMap);
        Market market = findProfitAction.findMarket(param);
        boolean evolve = getEvolve(verificationdays, param);
        WebData myData = findProfitAction.getMarket(null, param, market, evolve);
        ProfitData buysells = myData.profitData; // findProfitAction.getPicks(param, allMemoryItems);
        updateMap = myData.updateMap;
        allMemoryItems.addAll(myData.memoryItems);
        
        List<IncDecItem> listInc = new ArrayList<>(myData.incs);
        List<IncDecItem> listDec = new ArrayList<>(myData.decs);
        listInc = mergeList(listInc);
        listDec = mergeList(listDec);
        List<IncDecItem> listIncDec = moveAndGetCommon(listInc, listDec);
        Map<String, Object> trendMap = new HashMap<>();
        Short mystartoffset = market.getConfig().getStartoffset();
        short startoffset = mystartoffset != null ? mystartoffset : 0;
        Trend trend = findProfitAction.getTrend(param.getInput().getConfig().verificationDays(), param.getInput().getEnddate(), param.getService(), startoffset);
        trendMap.put(market.getConfig().getMarket(), trend);
        if (verificationdays > 0) {
            try {
                param.setFuturedays(0);
                param.setOffset(0);
                param.setDates(0, 0, TimeUtil.convertDate2(param.getInput().getEnddate()));
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }            
            findProfitAction.getVerifyProfit(verificationdays, param.getFutureDate(), param.getService(), param.getBaseDate(), listInc, listDec, startoffset);
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
        addHeader(componentInput, type, result, param);
        
        List<IclijServiceList> subLists = getServiceList(param.getMarket(), listInc, listDec, listIncDec);
        retLists.addAll(subLists);
        
        retLists.add(memories);
       
        {
            List<TimingItem> currentTimings = (List<TimingItem>) myData.timingMap.get(market.getConfig().getMarket());
            List<IclijServiceList> subLists2 = getServiceList(market.getConfig().getMarket(), currentTimings);
            retLists.addAll(subLists2);
        }
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        IclijServiceList updates = convert(market.getConfig().getMarket(), updateMap);
        retLists.add(updates);

        IclijServiceList trends = convert(trendMap);
        retLists.add(trends);

        List<IncDecItem> listIncDecs = new ArrayList<>(myData.incs);
        listIncDecs.addAll(myData.decs);
        
        addRelations(componentInput, retLists, listIncDecs);

        return result;
    }

    public static boolean getEvolve(int verificationdays, ComponentData param) {
        Boolean evolvefirst;
        if (verificationdays > 0) {
            evolvefirst = param.getInput().getConfig().verificationEvolveFirstOnly();
        } else {
            evolvefirst = param.getInput().getConfig().singlemarketEvolveFirstOnly();
        }
        boolean evolve = true;
        if (evolvefirst && param.getInput().getLoopoffset() > 0) {
            evolve = false;
        }
        return evolve;
    }

    private static void addHeader(ComponentInput componentInput, String type, IclijServiceResult result,
            ComponentData param) {
        IclijServiceList header = new IclijServiceList();
        result.getLists().add(header);
        header.setTitle(type + " " + "Market: " + componentInput.getConfig().getMarket() + " Date: " + componentInput.getConfig().getDate() + " Offset: " + componentInput.getLoopoffset());
        List<MapList> aList = new ArrayList<>();
        header.setList(aList);
        MapList mapList = new MapList();
        mapList.setKey("Dates");
        mapList.setValue("Base " + param.getBaseDate() + " Future " + param.getFutureDate());
        aList.add(mapList);
    }

    public static ComponentData getParam(ComponentInput input, int days) throws Exception {
        ComponentData param = new ComponentData(input);
        param.setAction(IclijConstants.FINDPROFIT);
        String market = input.getConfig().getMarket();
        /*
        if (market == null) {
            throw new Exception("Market null");
        }
        */
        //LocalDate date = input.getConfig().getDate();
        ControlService srv = new ControlService();
        param.setService(srv);
        if (market != null) {
            srv.conf.setMarket(market);
            param.getInput().setMarket(market);
        }
        // verification days, 0 or something
        param.setOffset(days);
        //srv.getConfig();
        /*
        List<String> stockdates = srv.getDates(market);
	if (date == null) {
	    date = TimeUtil.convertDate(stockdates.get(stockdates.size() - 1));
	}
        if (input.getLoopoffset() != null) {
            int index = getDateIndex(date, stockdates);
            index = index - input.getLoopoffset();
            if (index <= 0) {
                throw new Exception("Index <= 0");
            }
            date = getDateIndex(stockdates, index);
        }
                
        int dateoffset = getDateOffset(date, stockdates);
        if (date == null) {
            date = getLastDate(stockdates);
        }
        log.info("Stock size {} ", stockdates.size());
        log.info("Main date {} ", date);
        String baseDateStr = stockdates.get(stockdates.size() - 1 - dateoffset - days);
        LocalDate baseDate = TimeUtil.convertDate(baseDateStr);
        log.info("Old date {} ", baseDate);
        param.setBaseDate(baseDate);
        param.setFutureDate(date);
        param.setOffset(dateoffset);
        */
        
        //param.setDates(days, input.getLoopoffset(), TimeUtil.convertDate2(input.getEnddate()));
        return param;
    }

    private static Map<String, List<List<Double>>> getSimpleContent(String market) throws Exception {
        ControlService srv = new ControlService();
        srv.getConfig();
        if (market != null) {
            srv.conf.setMarket(market);
        }
        Component.disabler(srv.conf.getConfigValueMap());
        Map<String, Map<String, Object>> result = srv.getContent();
        Integer cat = (Integer) result.get("meta").get("wantedcat");
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
        ImproveProfitAction improveProfitAction = new ImproveProfitAction();  
        getImprovements(retLists, market, date, save, improveProfitAction, allMemoryItems);
    }
     */

    private static void print(IclijServiceResult result) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(path);
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        } 
        for (IclijServiceList item : result.getLists()) {
            listWriter(writer, item, item.getList());            
        }
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    private static void listWriter(BufferedWriter writer, IclijServiceList item, List mylist) {
        try {
            writer.write(item.getTitle());
            writer.write("\n");
            if (mylist == null) {
                return;
            }
            for (Object object : mylist) {
                writer.write(object.toString());
            }
            writer.write("\n");
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

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
            ImproveProfitAction improveProfitAction, List<MemoryItem> allMemoryItems) throws Exception {
        Map<String, String> map = improveProfitAction.getMarket(param, allMemoryItems);        
        List<MapList> mapList = improveProfitAction.getList(map);
        IclijServiceList resultMap = new IclijServiceList();
        resultMap.setTitle("Improve Profit Info");
        resultMap.setList(mapList);
        retLists.add(resultMap);
    }
*/
    
    public static IclijServiceResult getFindProfit(ComponentInput componentInput) throws Exception {
	String type = "FindProfit";
        int days = 0;  // config.verificationDays();
        IclijServiceResult result = getFindProfitVerify(componentInput, type, days);
        print(result);
        return result;
    }

    public static List<String> getFindProfitComponents(IclijConfig config, String market) {
        List<String> components = new ArrayList<>();
        if (config.wantsFindProfitRecommender()) {
            components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        }
        if (config.wantsFindProfitPredictor()) {
            components.add(PipelineConstants.PREDICTORSLSTM);
        }
        if (config.wantsFindProfitMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsFindProfitMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree(market)) {
        if (config.wantsFindProfitMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsFindProfitMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsFindProfitMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsFindProfitMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
        if (config.wantsFindProfitMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }

    public static IclijServiceResult getImproveProfit(ComponentInput componentInput) throws Exception {
        try {
        int loopOffset = 0;
        int days = 0; // config.verificationDays();
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(new ArrayList<>());
        List<IclijServiceList> retLists = result.getLists();

        ComponentData param = null; 
        try {
            param = getParam(componentInput, days);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        param.getInput().setDoSave(false);
        //FindProfitAction findProfitAction = new FindProfitAction();
        ImproveProfitAction improveProfitAction = new ImproveProfitAction();  
        List<MemoryItem> allMemoryItems = new ArrayList<>(); // getMemoryItems(componentInput.getConfig(), param, days, getImproveProfitComponents(componentInput.getConfig()));
        //IclijServiceList memories = new IclijServiceList();
        //memories.setTitle("Memories");
        //memories.setList(allMemoryItems);
        Map<String, Object> updateMap = new HashMap<>();
        Market market = improveProfitAction.findMarket(param);
        WebData webData = improveProfitAction.getMarket(null, param, market, null);        
        List<MapList> mapList = improveProfitAction.getList(webData.updateMap);
        IclijServiceList resultMap = new IclijServiceList();
        resultMap.setTitle("Improve Profit Info");
        resultMap.setList(mapList);
        retLists.add(resultMap);

        //retLists.add(memories);

        List<IclijServiceList> lists = new ArrayList<>();
        Map<String, Object> timingMap = webData.timingMap;
        for (Entry<String, Object> entry : timingMap.entrySet()) {
            String marketName = entry.getKey();
            List<TimingItem> list = (List<TimingItem>) entry.getValue();
            List<IclijServiceList> subLists = getServiceList(marketName, list);
            lists.addAll(subLists);
        }

        Map<String, Object> timingMap2 = webData.timingMap2;
        for (Entry<String, Object> entry : timingMap2.entrySet()) {
            String marketName = entry.getKey();
            List<TimingItem> list = (List<TimingItem>) entry.getValue();
            List<IclijServiceList> subLists = getServiceList(marketName + " sell", list);
            lists.addAll(subLists);
        }

        result.setLists(lists);
        
        updateMap = webData.updateMap;
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        IclijServiceList updates = convert(null, updateMap);
        lists.add(updates);

        updateMap = webData.updateMap2;
        updates = convert(null, updateMap);
        lists.add(updates);

        return result;
        } catch (Exception e) {
            log.error("Ex", e);
        }
        return null;
    }

    public static List<String> getImproveProfitComponents(IclijConfig config, String market) {
        List<String> components = new ArrayList<>();
        if (config.wantsImproveProfitRecommender()) {
            components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        }
        if (config.wantsImproveProfitPredictor()) {
            components.add(PipelineConstants.PREDICTORSLSTM);
        }
        if (config.wantsImproveProfitMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsImproveProfitMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree(market)) {
        if (config.wantsImproveProfitMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsImproveProfitMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsImproveProfitMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsImproveProfitMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
       if (config.wantsImproveProfitMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
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

    public static List<MemoryItem> getMemoryItems(IclijConfig config, ComponentData param, int days, List<String> components) throws InterruptedException {
        List<MemoryItem> allMemoryItems = new ArrayList<>();
        UpdateDBAction updateDbAction = new UpdateDBAction();
        Queue<Action> serviceActions = updateDbAction.findAllMarketComponentsToCheck(param, days, config, components);
        for (Action serviceAction : serviceActions) {
            serviceAction.goal(null, param);
            Map<String, Object> resultMap = serviceAction.getLocalResultMap();
            List<MemoryItem> memoryItems = (List<MemoryItem>) resultMap.get(IclijPipelineConstants.MEMORY);
            if (memoryItems != null) {
                allMemoryItems.addAll(memoryItems);
            } else {
                log.error("Memory null");
            }
        }
        return allMemoryItems;
    }

    public static Map<String, Object> loadConfig(ComponentData param, Market market, String marketName, String action, String component, boolean evolve, Boolean buy, String subcomponent) throws Exception {
        LocalDate date = param.getInput().getEnddate();
        LocalDate olddate = date.minusDays(market.getFilter().getRecordage());
        List<ConfigItem> filterConfigs = new ArrayList<>();
        List<ConfigItem> configs = IclijDbDao.getAllConfigs(market.getConfig().getMarket(), action, component, subcomponent, olddate, date);
        for (ConfigItem config : configs) {
            if (buy != null && config.getBuy() != null && buy != config.getBuy()) {
                continue;
            }
            filterConfigs.add(config);
        }
        /*
        List<ConfigItem> configs = IclijDbDao.getAllConfigs(market.getConfig().getMarket());
        List<ConfigItem> currentConfigs = configs.stream().filter(m -> olddate.compareTo(m.getDate()) <= 0).collect(Collectors.toList());
        currentConfigs = currentConfigs.stream().filter(m -> date.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        for (ConfigItem config : currentConfigs) {
            if (marketName.equals(config.getMarket()) && action.equals(config.getAction()) && component.equals(config.getComponent()) && (subcomponent == null || subcomponent.equals(config.getSubcomponent()))) {
                if (buy != null && config.getBuy() != null && buy != config.getBuy()) {
                    continue;
                }
                filterConfigs.add(config);
            }
        }
        */
        Collections.sort(filterConfigs, (o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
        Map<String, Class> type = param.getService().conf.getType();
        Map<String, Object> updateMap = new HashMap<>();
        for (ConfigItem config : filterConfigs) {
            Object value = config.getValue();
            String string = config.getValue();
            Class myclass = type.get(config.getId());
            if (myclass == null) {
                log.error("No class for {}", config.getId());
                myclass = String.class;
            }
            log.info("Trying {} {}", config.getId(), myclass.getName());
            switch (myclass.getName()) {
            case "java.lang.String":
                break;
            case "java.lang.Integer":
                value = Integer.valueOf(string);
                break;
            case "java.lang.Double":
                value = Double.valueOf(string);
                break;
            case "java.lang.Boolean":
                value = Boolean.valueOf(string);
                break;
            default:
                log.info("unknown {}", myclass.getName());
            }

            updateMap.put(config.getId(), value);
        }
        return updateMap;
    }

    public static List<String> getEvolveComponents(IclijConfig config, String market) {
        List<String> components = new ArrayList<>();
        if (config.wantsEvolveRecommender()) {
            components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        }
        if (config.wantsEvolvePredictor()) {
            components.add(PipelineConstants.PREDICTORSLSTM);
        }
        if (config.wantsEvolveMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsEvolveMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree(market)) {
        if (config.wantsEvolveMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsEvolveMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsEvolveMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsEvolveMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
        if (config.wantsEvolveMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }

    public static List<String> getMachineLearningComponents(IclijConfig config, String market) {
        List<String> components = new ArrayList<>();
        if (config.wantsMachineLearningPredictor()) {
            components.add(PipelineConstants.PREDICTORSLSTM);
        }
        if (config.wantsMachineLearningMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsMachineLearningMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree(market)) {
        if (config.wantsMachineLearningMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsMachineLearningMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsMachineLearningMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsMachineLearningMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
        if (config.wantsMachineLearningMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }
    
}
