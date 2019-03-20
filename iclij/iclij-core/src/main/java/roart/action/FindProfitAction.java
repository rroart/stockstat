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

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.config.MarketConfig;
import roart.config.MarketFilter;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.result.model.ResultMeta;
import roart.service.ControlService;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class FindProfitAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String TP = "TP";
    public static final String TN = "TN";
    public static final String FP = "FP";
    public static final String FN = "FN";

    @Override
    public void goal(Action parent, ComponentData param) {

        // find recommended picks

        IclijConfig config = IclijXMLConfig.getConfigInstance();
        boolean save = true;
        List<Market> markets = getMarkets();
        for (Market market : markets) {
            LocalDate olddate = LocalDate.now();        
            olddate = olddate.minusDays(market.getFilter().getRecordage());
            List<MemoryItem> marketMemory = getMarketMemory(market.getConfig().getMarket());
            if (param == null) {
                ComponentInput input = new ComponentInput(config, olddate, market.getConfig().getMarket(), null, null, save, false, new ArrayList<>(), new HashMap<>());
                param = new ComponentData(input);
                ControlService srv = new ControlService();
                srv.getConfig();
                srv.conf.setMarket(market.getConfig().getMarket());
                srv.conf.setdate(TimeUtil.convertDate(input.getEnddate()));
                param.setService(srv);
            }
            try {
                param.setFuturedays(market.getFilter().getRecordage());
                //param.setDates(market.getFilter().getRecordage(), 0, TimeUtil.convertDate2(olddate));
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            getPicksFiltered(param, market, marketMemory);
        }
    }

    public ProfitData getPicks(ComponentData param, List<MemoryItem> memoryItems) {
        Market foundFilterMarket = findMarket(param);
        if (foundFilterMarket == null) {
            return new ProfitData();
        }
        return getPicksFiltered(param, foundFilterMarket, memoryItems);
    }

    public static Market findMarket(ComponentData param) {
        List<Market> markets = getMarkets();
        Market foundFilterMarket = null;
        // TODO check if two markets
        for (Market aMarket : markets) {
            if (param.getInput().getMarket().equals(aMarket.getConfig().getMarket())) {
                foundFilterMarket = aMarket;
                break;
            }
        }
        return foundFilterMarket;
    }
    
    private ProfitData getPicksFiltered(ComponentData param, Market market, List<MemoryItem> marketMemory) {
        log.info("Getting picks for date {}", param.getInput().getEnddate());
        if (marketMemory == null) {
            return new ProfitData();
        }

        List<MemoryItem> currentList = filterKeepRecent(marketMemory, param.getInput().getEnddate());
        // or make a new object instead of the object array. use this as a pair
        //System.out.println(currentList.get(0).getRecord());
        Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
        // map subcat + posit -> list
        currentList.forEach(m -> listGetterAdder(listMap, new Object[]{m.getComponent(), m.getPosition() }, m));
        ProfitInputData inputdata = filterMemoryListMapsWithConfidence(market, listMap);
        ProfitData profitdata = new ProfitData();
        profitdata.setInputdata(inputdata);
        Map<String, List<Integer>> listComponent = createComponentPositionListMap(inputdata.getListMap());
       
        /*
        ComponentParam param = new ComponentParam(input);
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market.getConfig().getMarket());
        srv.conf.setdate(TimeUtil.convertDate(input.getEnddate()));
        param.setService(srv);
        param.getInput().setMarket(srv.conf.getMarket());
        param.getInput().setDoPrint(false);
        param.getInput().setDoSave(false);        
        */
        
        Map<String, Component> componentMap = getComponentMap(listComponent);

        //Component.disabler(param.getService().conf);

        /*
        Map<String, Map<String, Object>> result0 = param.getService().getContent();
        Map<String, Map<String, Object>> maps = result0;
        inputdata.setResultMaps(maps);
        */
        param.getAndSetCategoryValueMap();
        Map<String, Map<String, Object>> maps = param.getResultMaps();
        Map<String, String> nameMap = getNameMap(maps);
        inputdata.setNameMap(nameMap);
        
        handleComponent(market, profitdata, listComponent, param, componentMap);
                
        List<String> dates = param.getService().getDates(param.getService().conf.getMarket());        
        String category = market.getFilter().getInccategory();
        if (category != null) {
            Map<String, Object> categoryMap = maps.get(category);
            if (categoryMap != null) {
                Integer offsetDays = null;
                Integer incdays = market.getFilter().getIncdays();
                if (incdays != null) {
                    if (incdays == 0) {
                        int year = param.getInput().getEnddate().getYear();
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
                Double threshold = market.getFilter().getIncthreshold();
                Map<String, List<List>> listMap3 = getCategoryList(maps, category);
                Map<String, IncDecItem> buysFilter = buyFilterOnIncreaseValue(market, profitdata.getBuys(), maps, threshold, categoryMap,
                        listMap3, offsetDays);
                profitdata.setBuys(buysFilter);
            }
        }
        if (param.getInput().isDoSave()) {
            IncDecItem myitem = null;
        try {
            for (IncDecItem item : profitdata.getBuys().values()) {
                myitem = item;
                System.out.println(item);
                item.save();
            }
            for (IncDecItem item : profitdata.getSells().values()) {
                myitem = item;
                System.out.println(item);
                item.save();
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            log.error("Could not save {}", myitem);
        }
        }
        //buys = buys.values().stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());        
        return profitdata;
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

    private Map<String, IncDecItem> buyFilterOnIncreaseValue(Market market, Map<String, IncDecItem> buys,
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
                System.out.println("market" + market.getConfig().getMarket() + "null map");
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
                    value = (curValue / oldValue - 1) * 100;
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

    private void handleComponent(Market market, ProfitData profitdata, Map<String, List<Integer>> listComponent, ComponentData param, Map<String, Component> componentMap) {
        for (Entry<String, List<Integer>> entry : listComponent.entrySet()) {
            List<Integer> positions = entry.getValue();
            String componentName = entry.getKey();
            Component component = componentMap.get(componentName);
            boolean evolve = param.getInput().getConfig().wantEvolveML();
            component.set(market, param, profitdata, positions, evolve);
            ComponentData componentData = component.handle(market, param, profitdata, positions, evolve);
            component.calculate(componentData, profitdata, positions);
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

    private ProfitInputData filterMemoryListMapsWithConfidence(Market market, Map<Object[], List<MemoryItem>> listMap) {
        Map<Object[], List<MemoryItem>> okListMap = new HashMap<>();
        Map<Object[], Double> okConfMap = new HashMap<>();
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
            if (min >= market.getFilter().getConfidence()) {
                okListMap.put(keys, memoryList);
                okConfMap.put(keys, min);
            }
        }
        ProfitInputData input = new ProfitInputData();
        input.setConfMap(okConfMap);
        input.setListMap(listMap);
        return input;
    }

    private List<MemoryItem> filterKeepRecent(List<MemoryItem> marketMemory, LocalDate olddate) {
        for (MemoryItem item : marketMemory) {
            if (item.getRecord() == null) {
                item.setRecord(LocalDate.now());
            }
        }
        // temp workaround
        if (olddate == null) {
            return marketMemory;
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

    private static List<Market> getMarkets() {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        //instance.
        List<Market> markets = null;
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            //log.error(Constants.EXCEPTION, e);
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
