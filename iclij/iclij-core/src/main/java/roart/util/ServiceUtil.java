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
import roart.action.UpdateDBAction;
import roart.action.VerifyProfitAction;
import roart.iclij.config.IclijConfig;
import roart.common.config.MyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.config.IclijXMLConfig;
import roart.config.TradeMarket;
import roart.constants.IclijPipelineConstants;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MapList;
import roart.iclij.model.MemoryItem;
import roart.iclij.service.IclijServiceList;
import roart.iclij.service.IclijServiceResult;
import roart.service.ControlService;

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
    
    public static IclijServiceResult getConfig() throws Exception {
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        IclijServiceResult result = new IclijServiceResult();
        result.setIclijConfig(instance);
        return result;
    }
    
    public static IclijServiceResult getContent(IclijConfig iclijConfig) throws Exception {
        LocalDate date = iclijConfig.getDate();
        IclijXMLConfig i = new IclijXMLConfig();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IncDecItem> listAll = IncDecItem.getAll();
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(getHeader("Content"));
        List<TradeMarket> markets = conf.getTradeMarkets(instance);
        for (TradeMarket market : markets) {
            if (date == null) {
                date = LocalDate.now();
            }
            LocalDate newdate = date;
            LocalDate olddate = date.minusDays(market.getRecordage());
            listAll = listAll.stream().filter(m -> m.getRecord() != null).collect(Collectors.toList());
            List<IncDecItem> currentIncDecs = listAll.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
            currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getRecord()) >= 0).collect(Collectors.toList());
            currentIncDecs = currentIncDecs.stream().filter(m -> market.getMarket().equals(m.getMarket())).collect(Collectors.toList());
            List<IncDecItem> listInc = currentIncDecs.stream().filter(m -> m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listDec = currentIncDecs.stream().filter(m -> !m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listIncDec = moveAndGetCommon(listInc, listDec);
            List<IclijServiceList> subLists = getServiceList(market.getMarket(), listInc, listDec, listIncDec);
            lists.addAll(subLists);
        }
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);
        print(result);
        return result;
    }

    private static IclijServiceList getHeader(String title) {
        IclijServiceList header = new IclijServiceList();
        header.setTitle(title);
        return header;
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

    private static List<IncDecItem> moveAndGetCommon(List<IncDecItem> listInc, List<IncDecItem> listDec) {
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

    public static IclijServiceResult getVerify(IclijConfig config, Integer loopOffset) throws InterruptedException, ParseException {
	String type = "Verify";
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(new ArrayList<>());
        List<IclijServiceList> retLists = result.getLists();
        String market = config.getMarket();
        if (market == null) {
            return result;
        }
        LocalDate date = config.getDate();
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (loopOffset != null) {
            int index = getDateIndex(date, stocks);
            index = index - loopOffset;
            // TODO calculate backward limit for all components
            if (index <= 0) {
                return result;
            }
            date = getDateIndex(stocks, index);
        }
        int days = config.verificationDays();
        getFindProfitVerify(result, retLists, config, type, market, loopOffset, date, srv, stocks, days);
        print(result);
        return result;
    }

    private static void getFindProfitVerify(IclijServiceResult result, List<IclijServiceList> retLists,
            IclijConfig config, String type, String market, Integer loopOffset, LocalDate date, ControlService srv,
            List<String> stocks, int days) throws ParseException, InterruptedException {
        int offset = getDateOffset(date, stocks);
        if (date == null) {
            date = getLastDate(stocks);
        }
        log.info("Stock size {} ", stocks.size());
        log.info("Main date {} ", date);
        String aDate = stocks.get(stocks.size() - 1 - offset - days);
        LocalDate oldDate = TimeUtil.convertDate(aDate);
        log.info("Old date {} ", oldDate);
        IclijServiceList header = new IclijServiceList();
        result.getLists().add(header);
        header.setTitle(type + " " + "Market: " + config.getMarket() + " Date: " + config.getDate() + " Offset: " + loopOffset);
        List<MapList> aList = new ArrayList<>();
        header.setList(aList);
        MapList mapList = new MapList();
        mapList.setKey("Dates");
        mapList.setValue("Base " + oldDate + " Future " + date);
        aList.add(mapList);
        boolean save = config.wantVerificationSave();
        FindProfitAction findProfitAction = new FindProfitAction();
        List<MemoryItem> allMemoryItems = getMemoryItems(config, market, days, date, offset, save);
        IclijServiceList memories = new IclijServiceList();
        memories.setTitle("Memories");
        memories.setList(allMemoryItems);
        Map<String, Object> updateMap = new HashMap<>();
        Map<String, IncDecItem>[] buysells = findProfitAction.getPicks(market, save, oldDate, allMemoryItems, config, updateMap);
        List<IncDecItem> listInc = new ArrayList<>(buysells[0].values());
        List<IncDecItem> listDec = new ArrayList<>(buysells[1].values());
        List<IncDecItem> listIncDec = moveAndGetCommon(listInc, listDec);
	if (days > 0) {
	    getVerifyProfit(retLists, days, date, srv, oldDate, listInc, listDec);
	}
        List<IclijServiceList> subLists = getServiceList(market, listInc, listDec, listIncDec);
        retLists.addAll(subLists);
        
        retLists.add(memories);
        
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        IclijServiceList updates = convert(updateMap);
        retLists.add(updates);
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

    private static IclijServiceList convert(Map<String, Object> updateMap) {
        IclijServiceList list = new IclijServiceList();
        list.setTitle("Updates");
        List<MapList> aList = new ArrayList<>();
        for (Entry<String, Object> map : updateMap.entrySet()) {
            MapList m = new MapList();
            m.setKey(map.getKey());
            m.setValue((String) map.getValue()); 
            aList.add(m);
        }
        list.setList(aList);
        return list;
    }

    private static LocalDate getDateIndex(List<String> stocks, int index) throws ParseException {
        String newDate = stocks.get(index);
        return TimeUtil.convertDate(newDate);
    }

    private static void getVerifyProfit(List<IclijServiceList> retLists, int days, LocalDate date, ControlService srv,
            LocalDate oldDate, List<IncDecItem> listInc, List<IncDecItem> listDec) {
        log.info("Verify compare date {} with {}", oldDate, date);
        LocalDate futureDate = date;
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        Component.disabler(srv.conf);
        Map<String, Map<String, Object>> resultMaps = srv.getContent();
        Map maps = (Map) resultMaps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        Integer category = (Integer) maps.get(PipelineConstants.CATEGORY);
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);

        VerifyProfitAction verify = new VerifyProfitAction();
        List<MapList> inc = verify.doVerify(listInc, days, true, categoryValueMap, oldDate);
        IclijServiceList incMap = new IclijServiceList();
        incMap.setTitle("Increase verify");
        incMap.setList(inc);
        List<MapList> dec = verify.doVerify(listDec, days, false, categoryValueMap, oldDate);
        IclijServiceList decMap = new IclijServiceList();
        decMap.setTitle("Decrease verify");
        decMap.setList(dec);
        retLists.add(incMap);
        retLists.add(decMap);
    }

    private static void getImprovements(List<IclijServiceList> retLists, String market, LocalDate date, boolean save,
            ImproveProfitAction improveProfitAction, List<MemoryItem> allMemoryItems) {
        Map<String, String> map = improveProfitAction.getImprovements(market, save, date, allMemoryItems);        
        List<MapList> mapList = improveProfitAction.getList(map);
        IclijServiceList resultMap = new IclijServiceList();
        resultMap.setTitle("Improve Profit Info");
        resultMap.setList(mapList);
        retLists.add(resultMap);
    }

    public static IclijServiceResult getFindProfit(IclijConfig config, Integer loopOffset) throws InterruptedException, ParseException {
	String type = "FindProfit";
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(new ArrayList<>());
        List<IclijServiceList> retLists = result.getLists();
        String market = config.getMarket();
        if (market == null) {
            return result;
        }
        int days = 0;  // config.verificationDays();
        LocalDate date = config.getDate();
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (loopOffset != null) {
            int index = getDateIndex(date, stocks);
            index = index - loopOffset;
            // TODO calculate backward limit for all components
            if (index <= 0) {
                return result;
            }
            date = getDateIndex(stocks, index);
        }
        getFindProfitVerify(result, retLists, config, type, market, loopOffset, date, srv, stocks, days);
        print(result);
        return result;
    }

    public static IclijServiceResult getImproveProfit(IclijConfig config, Integer loopOffset) throws InterruptedException, ParseException {
        loopOffset = 0;
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(new ArrayList<>());
        List<IclijServiceList> retLists = result.getLists();
        String market = config.getMarket();
        if (market == null) {
            return result;
        }
        LocalDate date = config.getDate();
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (loopOffset != null) {
            int index = getDateIndex(date, stocks);
            index = index - loopOffset;
            // TODO calculate backward limit for all components
            if (index <= 0) {
                return result;
            }
            date = getDateIndex(stocks, index);
        }
        int days = 0; // config.verificationDays();
        int offset = getDateOffset(date, stocks);
        if (date == null) {
            date = getLastDate(stocks);
        }
        log.info("Main date {} ", date);
        String aDate = stocks.get(stocks.size() - 1 - offset - days);
        LocalDate oldDate = TimeUtil.convertDate(aDate);
        log.info("Old date {} ", oldDate);
        boolean save = false;
        //FindProfitAction findProfitAction = new FindProfitAction();
        ImproveProfitAction improveProfitAction = new ImproveProfitAction();  
        List<MemoryItem> allMemoryItems = getMemoryItems(config, market, days, date, offset, save);
        IclijServiceList memories = new IclijServiceList();
        memories.setTitle("Memories");
        memories.setList(allMemoryItems);
        Map<String, Object> updateMap = new HashMap<>();
        getImprovements(retLists, market, date, save, improveProfitAction, allMemoryItems);

        retLists.add(memories);
        
        Map<String, Map<String, Object>> mapmaps = new HashMap<>();
        mapmaps.put("ml", updateMap);
        result.setMaps(mapmaps);
        IclijServiceList updates = convert(updateMap);
        retLists.add(updates);
        return result;
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

    private static List<MemoryItem> getMemoryItems(IclijConfig config, String market, int days, LocalDate date,
            int offset, boolean save) throws InterruptedException {
        List<MemoryItem> allMemoryItems = new ArrayList<>();
        UpdateDBAction updateDbAction = new UpdateDBAction();
        Queue<Action> serviceActions = updateDbAction.findAllMarketComponentsToCheck(market, date, days + offset, save, config);
        for (Action serviceAction : serviceActions) {
            serviceAction.goal(null);
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

}
