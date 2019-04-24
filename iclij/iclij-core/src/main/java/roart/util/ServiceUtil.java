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
import roart.action.FindProfitAction;
import roart.action.ImproveProfitAction;
import roart.action.WebData;
import roart.action.UpdateDBAction;
import roart.iclij.config.IclijConfig;
import roart.common.config.MyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.config.MarketFilter;
import roart.constants.IclijConstants;
import roart.constants.IclijPipelineConstants;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MapList;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.TimingItem;
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

    public static String getWantedCategory(Map<String, Map<String, Object>> maps, String type) throws Exception {
        List<String> wantedList = new ArrayList<>();
        wantedList.add(Constants.PRICE);
        wantedList.add(Constants.INDEX);
        wantedList.add("cy");
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
        wantedList.add("cy");
        String cat = null;
        for (String wanted : wantedList) {
            Map<String, Object> map = maps.get(wanted + " " + type.toUpperCase());
            if (map != null) {
                return wanted;
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
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig i = new IclijXMLConfig();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IncDecItem> listAll = IncDecItem.getAll();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        for (Market market : markets) {
            List<IncDecItem> currentIncDecs = getCurrentIncDecs(date, listAll, market);
            List<IncDecItem> listInc = currentIncDecs.stream().filter(m -> m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listDec = currentIncDecs.stream().filter(m -> !m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listIncDec = moveAndGetCommon(listInc, listDec);
            List<IclijServiceList> subLists = getServiceList(market.getConfig().getMarket(), listInc, listDec, listIncDec);
            lists.addAll(subLists);
        }
        List<TimingItem> listAllTimings = TimingItem.getAll();
        for (Market market : markets) {
            List<TimingItem> currentTimings = getCurrentTimings(date, listAllTimings, market, IclijConstants.FINDPROFIT);
            List<IclijServiceList> subLists = getServiceList(market.getConfig().getMarket(), currentTimings);
            lists.addAll(subLists);
        }
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        /*
        ComponentData param = null; 
        try {
            param = getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
         */
        
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        FindProfitAction findProfitAction = new FindProfitAction();
        //Market market = findProfitAction.findMarket(param);
        //String marketName = market.getConfig().getMarket();
        List<String> componentList = getFindProfitComponents(componentInput.getConfig());
        for (Market market : findProfitAction.getMarkets()) {
            Map<String, Component> componentMap = findProfitAction.getComponentMap(componentList, null);
            for (Component component : componentMap.values()) {
                Map<String, Object> anUpdateMap = loadConfig(null, market, market.getConfig().getMarket(), IclijConstants.FINDPROFIT, component.getPipeline(), false);
                updateMarketMap.put(market.getConfig().getMarket(), anUpdateMap);
                updateMap.putAll(anUpdateMap);
            }
        }
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        for (Market market : findProfitAction.getMarkets()) {
        //for (Entry<String, Map<String, Object>> entry : updateMarketMap.entrySet()) {
            String marketName = market.getConfig().getMarket();
            Map<String, Object> anUpdateMap = updateMarketMap.get(marketName);
            IclijServiceList updates = convert(marketName, anUpdateMap);
            lists.add(updates);

            List<MemoryItem> marketMemory = findProfitAction.getMarketMemory(market.getConfig().getMarket());
            List<MemoryItem> currentList = findProfitAction.filterKeepRecent(marketMemory, componentInput.getEnddate());
            
            IclijServiceList memories = new IclijServiceList();
            memories.setTitle("Memories " + marketName);
            memories.setList(currentList);
            lists.add(memories);

        }
        print(result);
        return result;
    }

    public static IclijServiceResult getContentImprove(ComponentInput componentInput) throws Exception {
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig i = new IclijXMLConfig();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IncDecItem> listAll = IncDecItem.getAll();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<Market> markets = conf.getMarkets(instance);
        /*
        for (Market market : markets) {
            List<IncDecItem> currentIncDecs = getCurrentIncDecs(date, listAll, market);
            List<IncDecItem> listInc = currentIncDecs.stream().filter(m -> m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listDec = currentIncDecs.stream().filter(m -> !m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listIncDec = moveAndGetCommon(listInc, listDec);
            List<IclijServiceList> subLists = getServiceList(market.getConfig().getMarket(), listInc, listDec, listIncDec);
            lists.addAll(subLists);
        }
        */
        List<TimingItem> listAllTimings = TimingItem.getAll();
        for (Market market : markets) {
            List<TimingItem> currentTimings = getCurrentTimings(date, listAllTimings, market, IclijConstants.IMPROVEPROFIT);
            List<IclijServiceList> subLists = getServiceList(market.getConfig().getMarket(), currentTimings);
            lists.addAll(subLists);
        }
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);

        /*
        ComponentData param = null; 
        try {
            param = getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
         */
        
        Map<String, Map<String, Object>> updateMarketMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        FindProfitAction findProfitAction = new FindProfitAction();
        //Market market = findProfitAction.findMarket(param);
        //String marketName = market.getConfig().getMarket();
        List<String> componentList = getFindProfitComponents(componentInput.getConfig());
        for (Market market : findProfitAction.getMarkets()) {
            Map<String, Component> componentMap = findProfitAction.getComponentMap(componentList, null);
            for (Component component : componentMap.values()) {
                Map<String, Object> anUpdateMap = loadConfig(null, market, market.getConfig().getMarket(), IclijConstants.IMPROVEPROFIT, component.getPipeline(), false);
                updateMarketMap.put(market.getConfig().getMarket(), anUpdateMap);
                updateMap.putAll(anUpdateMap);
            }
        }
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        for (Market market : findProfitAction.getMarkets()) {
        //for (Entry<String, Map<String, Object>> entry : updateMarketMap.entrySet()) {
            String marketName = market.getConfig().getMarket();
            Map<String, Object> anUpdateMap = updateMarketMap.get(marketName);
            IclijServiceList updates = convert(marketName, anUpdateMap);
            lists.add(updates);

            List<MemoryItem> marketMemory = findProfitAction.getMarketMemory(market.getConfig().getMarket());
            List<MemoryItem> currentList = findProfitAction.filterKeepRecent(marketMemory, componentInput.getEnddate());
            
            IclijServiceList memories = new IclijServiceList();
            memories.setTitle("Memories " + marketName);
            memories.setList(currentList);
            lists.add(memories);

        }
        print(result);
        return result;
    }

    public static List<TimingItem> getCurrentTimings(LocalDate date, List<TimingItem> listAll, Market market, String action) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        LocalDate olddate = date.minusDays(market.getFilter().getRecordage());
        List<TimingItem> filterListAll = listAll.stream().filter(m -> m.getRecord() != null).collect(Collectors.toList());
        filterListAll = filterListAll.stream().filter(m -> action.equals(m.getAction())).collect(Collectors.toList());
        List<TimingItem> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getRecord()) >= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    public static List<IncDecItem> getCurrentIncDecs(LocalDate date, List<IncDecItem> listAll, Market market) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        LocalDate olddate = date.minusDays(market.getFilter().getRecordage());
        List<IncDecItem> filterListAll = listAll.stream().filter(m -> m.getRecord() != null).collect(Collectors.toList());
        List<IncDecItem> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getRecord()) >= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    private static IclijServiceList getHeader(String title) {
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

    private static List<IclijServiceList> getServiceList(String market, List<IncDecItem> listInc, List<IncDecItem> listDec,
            List<IncDecItem> listIncDec) {
        List<IclijServiceList> subLists = new ArrayList<>();
        if (!listInc.isEmpty()) {
            List<Boolean> listIncBoolean = listInc.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
            long count = listIncBoolean.stream().filter(i -> i).count();                            
            IclijServiceList inc = new IclijServiceList();
            inc.setTitle(market + " " + "Increase ( verified " + count + " / " + listIncBoolean.size() + " )");
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

    public static List<IncDecItem> moveAndGetCommon(List<IncDecItem> listInc, List<IncDecItem> listDec) {
        // and a new list for common items
        List<String> incIds = listInc.stream().map(IncDecItem::getId).collect(Collectors.toList());
        List<String> decIds = listDec.stream().map(IncDecItem::getId).collect(Collectors.toList());
        List<IncDecItem> listIncDec = listInc.stream().filter(m -> decIds.contains(m.getId())).collect(Collectors.toList());
        List<IncDecItem> listDecInc = listDec.stream().filter(m -> incIds.contains(m.getId())).collect(Collectors.toList());

        listInc.removeAll(listIncDec);
        listDec.removeAll(listDecInc);
        listIncDec.addAll(listDecInc);
        return listIncDec;
    }

    public static IclijServiceResult getVerify(ComponentInput componentInput) throws Exception {
	String type = "Verify";
        componentInput.setDoSave(componentInput.getConfig().wantVerificationSave());
        int verificationdays = componentInput.getConfig().verificationDays();
        IclijServiceResult result = getFindProfitVerify(componentInput, type, verificationdays);
        print(result);
        return result;
    }

    private static IclijServiceResult getFindProfitVerify(ComponentInput componentInput, String type, int verificationdays) throws ParseException, InterruptedException {

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
        
        List<IncDecItem> listInc = new ArrayList<>(buysells.getBuys().values());
        List<IncDecItem> listDec = new ArrayList<>(buysells.getSells().values());
        List<IncDecItem> listIncDec = moveAndGetCommon(listInc, listDec);
        if (verificationdays > 0) {
            try {
                param.setFuturedays(0);
                param.setOffset(0);
                param.setDates(0, 0, TimeUtil.convertDate2(param.getInput().getEnddate()));
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }            
            findProfitAction.getVerifyProfit(verificationdays, param.getFutureDate(), param.getService(), param.getBaseDate(), listInc, listDec);
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
            // TODO calculate backward limit for all components
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

    public static List<String> getFindProfitComponents(IclijConfig config) {
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
        if (config.wantsFindProfitMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }

    public static IclijServiceResult getImproveProfit(ComponentInput componentInput) throws Exception {
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
        IclijServiceList memories = new IclijServiceList();
        memories.setTitle("Memories");
        memories.setList(allMemoryItems);
        Map<String, Object> updateMap = new HashMap<>();
        Market market = improveProfitAction.findMarket(param);
        WebData webData = improveProfitAction.getMarket(null, param, market);        
        List<MapList> mapList = improveProfitAction.getList(webData.updateMap);
        IclijServiceList resultMap = new IclijServiceList();
        resultMap.setTitle("Improve Profit Info");
        resultMap.setList(mapList);
        retLists.add(resultMap);

        retLists.add(memories);
        
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        IclijServiceList updates = convert(null, updateMap);
        retLists.add(updates);
        return result;
    }

    public static List<String> getImproveProfitComponents(IclijConfig config) {
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

    public static Map<String, Object> loadConfig(ComponentData param, Market market, String marketName, String action, String component, boolean evolve) throws Exception {
        LocalDate now = LocalDate.now();
        LocalDate olddate = now.minusDays(market.getFilter().getRecordage());
        List<ConfigItem> filterConfigs = new ArrayList<>();
        List<ConfigItem> configs = ConfigItem.getAll();
        List<ConfigItem> currentConfigs = configs.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        for (ConfigItem config : currentConfigs) {
            if (marketName.equals(config.getMarket()) && action.equals(config.getAction()) && component.equals(config.getComponent())) {
                filterConfigs.add(config);
            }
        }
        Collections.sort(filterConfigs, (o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
        Map<String, Object> updateMap = new HashMap<>();
        for (ConfigItem config : filterConfigs) {
            updateMap.put(config.getId(), config.getValue());
        }
        return updateMap;
    }
    
}
