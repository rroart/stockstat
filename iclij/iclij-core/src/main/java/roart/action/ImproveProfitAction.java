package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.component.Component;
import roart.component.ComponentFactory;
import roart.config.IclijConfig;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.config.TradeMarket;
import roart.db.IclijDbDao;
import roart.model.IncDecItem;
import roart.model.MapList;
import roart.model.MemoryItem;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.util.Constants;

public class ImproveProfitAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void goal(Action parent) {
        // test picks for aggreg recommend, predict etc
        // remember and make confidence
        // memory is with date, confidence %, inc/dec, semantic item
        // find recommended picks
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        //instance.
        boolean save = true;
        List<Market> markets = getMarkets(instance);
        for (Market market : markets) {
            LocalDate olddate = LocalDate.now();        
            olddate = olddate.minusDays(market.getImprovetime());
            List<MemoryItem> marketMemory = getMarketMemory(market);
            getImprovementInfo(market, save, olddate, marketMemory);
        }
    }

    public Map<String, String> getImprovements(String market, boolean save, LocalDate date, List<MemoryItem> memoryItems) {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<Market> markets = getMarkets(instance);
        Market foundMarket = null;
        // TODO check if two markets
        for (Market aMarket : markets) {
            if (market.equals(aMarket.getMarket())) {
                foundMarket = aMarket;
                break;
            }
        }
        return getImprovementInfo(foundMarket, save, date, memoryItems);
    }
    
    private Map<String, String> getImprovementInfo(Market market, boolean save, LocalDate date, List<MemoryItem> marketMemory) {
        Map<String, String> retMap = new HashMap<>();
        LocalDate olddate = LocalDate.now().minusDays(market.getImprovetime());
        List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        // or make a new object instead of the object array. use this as a pair
        Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
        // map subcat + posit -> list
        currentList.forEach(m -> { listGetterAdder(listMap, new Object[]{m.getComponent(), m.getPosition() }, m); } );
        Map<Object[], List<MemoryItem>> badListMap = new HashMap<>();
        Map<Object[], Double> badConfMap = new HashMap<>();
        for(Object[] keys : listMap.keySet()) {
            List<MemoryItem> memoryList = listMap.get(keys);
            List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            if (confidences.isEmpty()) {
                continue;
            }
            confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            if (!minOpt.isPresent()) {
                continue;
            }
            Double min = minOpt.get();
            // do the bad ones
            // do not yet improve on the good enough ones
            if (false /*min >= market.getConfidence()*/) {
                continue;
            }
            Optional<Double> maxOpt = confidences.parallelStream().reduce(Double::max);
            Double max = maxOpt.get();
            System.out.println("Mark " + market.getMarket() + " " + keys[0] + " " + min + " " + max );
            //Double conf = market.getConfidence();
            //System.out.println(conf);
            badListMap.put(keys, listMap.get(keys));
            badConfMap.put(keys, min);
        }
        Map<String, List<Integer>> listComponent = new HashMap<>();

        for (Object[] keys : badListMap.keySet()) {
            String component = (String) keys[0];
            Integer position = (Integer) keys[1];
            listGetterAdder(listComponent, component, position);
        }
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market.getMarket());
        Map<String, Component> componentMap = new HashMap<>();
        for (String componentName : listComponent.keySet()) {
            Component component = ComponentFactory.factory(componentName);
            componentMap.put(componentName, component);
        }
        
        Map<String, IncDecItem> buys = new HashMap<>();
        Map<String, IncDecItem> sells = new HashMap<>();
        Component.disabler(srv.conf);
        Map<String, Map<String, Object>> result0 = srv.getContent();
        Map<String, Map<String, Object>> maps = result0;
        Map<String, String> nameMap = getNameMap(maps);
        for (Entry<String, List<Integer>> entry : listComponent.entrySet()) {
            List<Integer> positions = entry.getValue();
            Component component = componentMap.get(entry.getKey());
            Component.disabler(srv.conf);
            component.enable(srv.conf);
            Map<String, String> map = component.improve(srv.conf, maps, positions, buys, sells, badConfMap, badListMap, nameMap);
            retMap.putAll(map);
        }
        return retMap;
    }

    private List<MemoryItem> getMarketMemory(Market market) {
        List<MemoryItem> marketMemory = null;
        try {
            marketMemory = IclijDbDao.getAll(market.getMarket());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return marketMemory;
    }

    private List<Market> getMarkets(IclijConfig instance) {
        List<Market> markets = null;
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return markets;
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
    
    private List<TradeMarket> getTradeMarkets(IclijConfig instance) {
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

    public List<MapList> getList(Map<String, String> map) {
        List<MapList> retList = new ArrayList<>();
        for (Entry<String, String> entry : map.entrySet()) {
            MapList ml = new MapList();
            ml.setKey(entry.getKey());
            ml.setValue(entry.getValue());
            retList.add(ml);
        }
        return retList;
    }
    
}

