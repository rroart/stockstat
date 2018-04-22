package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
//import java.util.Collections;
//import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.component.Component;
import roart.component.ComponentFactory;
import roart.config.IclijConfig;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.config.TradeMarket;
import roart.db.IclijDbDao;
import roart.model.IncDecItem;
import roart.model.MemoryItem;
import roart.model.ResultMeta;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.util.Constants;

public class FindProfitAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String TP = "TP";
    public static final String TN = "TN";
    public static final String FP = "FP";
    public static final String FN = "FN";

    @Override
    public void goal(Action parent) {

        // find recommended picks

        IclijConfig config = IclijXMLConfig.getConfigInstance();
        boolean save = true;
        List<TradeMarket> markets = getMarkets();
        for (TradeMarket market : markets) {
            LocalDate olddate = LocalDate.now();        
            olddate = olddate.minusDays(market.getRecordage());
            List<MemoryItem> marketMemory = getMarketMemory(market.getMarket());
            getPicks(market, save, olddate, marketMemory, config);
        }
    }

    public Map<String, IncDecItem>[] getPicks(String market, boolean save, LocalDate date, List<MemoryItem> memoryItems, IclijConfig config) {
        List<TradeMarket> markets = getMarkets();
        TradeMarket foundTradeMarket = null;
        // TODO check if two markets
        for (TradeMarket aTradeMarket : markets) {
            if (market.equals(aTradeMarket.getMarket())) {
                foundTradeMarket = aTradeMarket;
                break;
            }
        }
        if (foundTradeMarket == null) {
            Map<String, IncDecItem>[] buysell = new Map[2];
            Map<String, IncDecItem> buys = new HashMap<>();
            Map<String, IncDecItem> sells = new HashMap<>();
            buysell[0] = buys;
            buysell[1] = sells;
            return buysell;
        }
        return getPicks(foundTradeMarket, save, date, memoryItems, config);
    }
    
    private Map<String, IncDecItem>[] getPicks(TradeMarket market, boolean save, LocalDate olddate, List<MemoryItem> marketMemory, IclijConfig config) {
        Map<String, IncDecItem>[] buysell = new Map[2];
        if (marketMemory == null) {
            return buysell;
        }

        List<MemoryItem> currentList = filterKeepRecent(marketMemory, olddate);
        // or make a new object instead of the object array. use this as a pair
        //System.out.println(currentList.get(0).getRecord());
        Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
        // map subcat + posit -> list
        currentList.forEach(m -> listGetterAdder(listMap, new Object[]{m.getComponent(), m.getPosition() }, m));
        Map<Object[], List<MemoryItem>> okListMap = new HashMap<>();
        Map<Object[], Double> okConfMap = new HashMap<>();
        filterMemoryListMapsWithConfidence(market, listMap, okListMap, okConfMap);
        Map<String, List<Integer>> listComponent = createComponentPositionListMap(okListMap);
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market.getMarket());
        Map<String, Component> componentMap = getComponentMap(listComponent);

        Map<String, IncDecItem> buys = new HashMap<>();
        Map<String, IncDecItem> sells = new HashMap<>();
        Component.disabler(srv.conf);
        List<String> dates = srv.getDates(srv.conf.getMarket());
        Map<String, Map<String, Object>> result0 = srv.getContent();
        Map<String, Map<String, Object>> maps = result0;
        Map<String, String> nameMap = getNameMap(maps);
        handleComponent(okListMap, okConfMap, listComponent, srv, componentMap, buys, sells, maps, nameMap, config);
        String category = market.getInccategory();
        if (category != null) {
            Map<String, Object> categoryMap = maps.get(category);
            if (categoryMap != null) {
                Integer offsetDays = null;
                Integer incdays = market.getIncdays();
                if (incdays != null) {
                    if (incdays == 0) {
                        int year = olddate.getYear();
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
                       offsetDays = incdays;
                    }
                }
                Double threshold = market.getIncthreshold();
                Map<String, List<List>> listMap3 = getCategoryList(maps, category);
                Map<String, IncDecItem> buysFilter = buyFilterOnIncreaseValue(market, buys, maps, threshold, categoryMap,
                        listMap3, offsetDays);
                buys = buysFilter;
            }
        }
        if (save) {
        try {
            for (IncDecItem item : buys.values()) {
                System.out.println(item);
                item.save();
            }
            for (IncDecItem item : sells.values()) {
                System.out.println(item);
                item.save();
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        }
        //buys = buys.values().stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());        
        buysell[0] = buys;
        buysell[1] = sells;
        return buysell;
    }

    private Map<String, List<List>> getCategoryList(Map<String, Map<String, Object>> maps, String category) {
        String newCategory = null;
        if (Constants.PRICE.equals(category)) {
            newCategory = "" + Constants.PRICECOLUMN;
        }
        if (Constants.INDEX.equals(category)) {
            newCategory = "" + Constants.INDEXVALUECOLUMN;
        }
        if (newCategory != null) {
            Map<String, Object> map = maps.get(newCategory);
            return (Map<String, List<List>>) map.get(PipelineConstants.LIST);
        }
        Map<String, List<List>> listMap3 = null;
        for (Entry<String, Map<String, Object>> entry : maps.entrySet()) {
            Map<String, Object> map = entry.getValue();
            if (category.equals(map.get(PipelineConstants.CATEGORYTITLE))) {
                listMap3 = (Map<String, List<List>>) map.get(PipelineConstants.LIST);
            }
        }
        return listMap3;
    }

    private Map<String, IncDecItem> buyFilterOnIncreaseValue(TradeMarket market, Map<String, IncDecItem> buys,
            Map<String, Map<String, Object>> maps, Double threshold, Map<String, Object> categoryMap,
            Map<String, List<List>> listMap3, Integer offsetDays) {
        Map<String, IncDecItem> buysFilter = new HashMap<>();
        for(IncDecItem item : buys.values()) {
            String key = item.getId();
            if (listMap3 == null) {
                if (categoryMap != null) {
                    System.out.println(categoryMap.keySet());
                }
                if (maps != null) {
                    System.out.println(maps.keySet());
                }
                System.out.println("market" + market.getMarket() + "null map");
                continue;
            }
            List<List> list = listMap3.get(key);
            List<Double> list0 = list.get(0);
            Double value = null;
            if (offsetDays == null) {
                value = list0.get(list0.size() - 1);
            } else {
                Double curValue = list0.get(list0.size() - 1);
                Double oldValue = list0.get(list0.size() - 1 - offsetDays);
                if (curValue != null && oldValue != null) {
                    value = curValue / oldValue;
                }
            }
            if (value == null) {
                continue;
            }
            if (value < threshold) {
                continue;
            }
            buysFilter.put(key, item);
        }
        return buysFilter;
    }

    private Map<String, String> getNameMap(Map<String, Map<String, Object>> maps) {
        Map<String, String> nameMap = null;
        for (Entry<String, Map<String, Object>> entry : maps.entrySet()) {
            Map<String, Object> map = entry.getValue();
            nameMap = (Map<String, String>) map.get(PipelineConstants.NAME);
            if (nameMap != null) {
                break;
            }
        }
        return nameMap;
    }

    private void handleComponent(Map<Object[], List<MemoryItem>> okListMap, Map<Object[], Double> okConfMap,
            Map<String, List<Integer>> listComponent, ControlService srv, Map<String, Component> componentMap,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<String, Map<String, Object>> maps,
            Map<String, String> nameMap, IclijConfig config) {
        for (Entry<String, List<Integer>> entry : listComponent.entrySet()) {
            List<Integer> positions = entry.getValue();
            String componentName = entry.getKey();
            Component component = componentMap.get(componentName);
            Component.disabler(srv.conf);
            component.enable(srv.conf);
            component.handle(srv, srv.conf, maps, positions, buys, sells, okConfMap, okListMap, nameMap, config);
            //System.out.println("Buys: " + market.getMarket() + buys);
            //System.out.println("Sells: " + market.getMarket() + sells);           
        }
    }

    private Map<String, Component> getComponentMap(Map<String, List<Integer>> listComponent) {
        Map<String, Component> componentMap = new HashMap<>();
        for (String componentName : listComponent.keySet()) {
            Component component = ComponentFactory.factory(componentName);
            componentMap.put(componentName, component);
        }
        return componentMap;
    }

    private Map<String, List<Integer>> createComponentPositionListMap(Map<Object[], List<MemoryItem>> okListMap) {
        Map<String, List<Integer>> listComponent = new HashMap<>();
        for (Object[] keys : okListMap.keySet()) {
            String component = (String) keys[0];
            Integer position = (Integer) keys[1];
            listGetterAdder(listComponent, component, position);
        }
        return listComponent;
    }

    private void filterMemoryListMapsWithConfidence(TradeMarket market, Map<Object[], List<MemoryItem>> listMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<Object[], Double> okConfMap) {
        for(Entry<Object[], List<MemoryItem>> entry : listMap.entrySet()) {
            Object[] keys = entry.getKey();
            List<MemoryItem> memoryList = entry.getValue();
            List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            if (!minOpt.isPresent()) {
                continue;
            }
            Double min = minOpt.get();
            if (min >= market.getConfidence()) {
                okListMap.put(keys, memoryList);
                okConfMap.put(keys, min);
            }
        }
    }

    private List<MemoryItem> filterKeepRecent(List<MemoryItem> marketMemory, LocalDate olddate) {
        for (MemoryItem item : marketMemory) {
            if (item.getRecord() == null) {
                item.setRecord(LocalDate.now());
            }
        }
        List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        return currentList;
    }

    private List<MemoryItem> getMarketMemory(String market) {
        List<MemoryItem> marketMemory = null;
        try {
            marketMemory = IclijDbDao.getAll(market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return marketMemory;
    }

    private List<TradeMarket> getMarkets() {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        //instance.
        List<TradeMarket> markets = null;
        try { 
            markets = IclijXMLConfig.getTradeMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return markets;
    }

    private <K, E> List<E> listGetter(Map<K, List<E>> listMap, K key) {
        return listMap.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private <K, E> void listGetterAdder(Map<K, List<E>> listMap, K key, E element) {
        List<E> list = listGetter(listMap, key);
        list.add(element);
    }

    private <T> String nullToEmpty(T s) {
        return s != null ? "" + s : "";
    }


}
