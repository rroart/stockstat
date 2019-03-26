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

import roart.iclij.config.IclijConfig;
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
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MapList;
import roart.iclij.model.MemoryItem;
import roart.service.ControlService;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class ImproveProfitAction extends Action {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void goal(Action parent, ComponentData param) {
        // test picks for aggreg recommend, predict etc
        // remember and make confidence
        // memory is with date, confidence %, inc/dec, semantic item
        // find recommended picks
        IclijConfig config = IclijXMLConfig.getConfigInstance();
        //instance.
        if (param == null) {
            ComponentInput input = new ComponentInput(config, LocalDate.now(), config.getMarket(), null, null, false, false, new ArrayList<>(), new HashMap<>());
            param = new ComponentData(input);
            ControlService srv = new ControlService();
            //srv.getConfig();
            param.setService(srv);
            srv.conf.setMarket(config.getMarket());
            srv.conf.setdate(TimeUtil.convertDate(input.getEnddate()));
        }
        boolean save = true;
        List<Market> markets = getMarkets(config);
        for (Market market : markets) {
            LocalDate olddate = LocalDate.now();        
            olddate = olddate.minusDays(market.getConfig().getImprovetime());
            List<MemoryItem> marketMemory = getMarketMemory(market);
            ComponentInput input = new ComponentInput(config, olddate, market.getConfig().getMarket(), null, null, save, false, new ArrayList<>(), new HashMap<>());
            getImprovementInfo(market, marketMemory, param);
        }
    }

    public Map<String, String> getImprovements(ComponentData param, List<MemoryItem> memoryItems) {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<Market> markets = getMarkets(instance);
        Market foundMarket = null;
        // TODO check if two markets
        for (Market aMarket : markets) {
            if (param.getMarket().equals(aMarket.getConfig().getMarket())) {
                foundMarket = aMarket;
                break;
            }
        }
        return getImprovementInfo(foundMarket, memoryItems, param);
    }
    
    private Map<String, String> getImprovementInfo(Market market, List<MemoryItem> marketMemory, ComponentData param) {
        Map<String, String> retMap = new HashMap<>();
        LocalDate olddate = LocalDate.now().minusDays(market.getConfig().getImprovetime());
        List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        // or make a new object instead of the object array. use this as a pair
        Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
        // map subcat + posit -> list
        currentList.forEach(m -> { listGetterAdder(listMap, new Object[]{m.getComponent(), m.getPosition() }, m); } );
        ProfitInputData inputdata = filterMemoryListMapsWithConfidence(market, listMap);
        ProfitData profitdata = new ProfitData();
        profitdata.setInputdata(inputdata);
        Map<String, List<Integer>> listComponent = new HashMap<>();

        for (Object[] keys : inputdata.getListMap().keySet()) {
            String component = (String) keys[0];
            Integer position = (Integer) keys[1];
            listGetterAdder(listComponent, component, position);
        }
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market.getConfig().getMarket());
        Map<String, Component> componentMap = new HashMap<>();
        for (String componentName : listComponent.keySet()) {
            Component component = ComponentFactory.factory(componentName);
            componentMap.put(componentName, component);
        }
        
        //Component.disabler(srv.conf);
        /*
        Map<String, Map<String, Object>> result0 = srv.getContent();
        Map<String, Map<String, Object>> maps = result0;
        */

        param.getAndSetCategoryValueMap();
        Map<String, Map<String, Object>> maps = param.getResultMaps();

        inputdata.setResultMaps(maps);
        Map<String, String> nameMap = getNameMap(maps);
        inputdata.setNameMap(nameMap);
        for (Entry<String, List<Integer>> entry : listComponent.entrySet()) {
            List<Integer> positions = entry.getValue();
            Component component = componentMap.get(entry.getKey());
            Map<String, Object> valueMap = new HashMap<>();
            Component.disabler(valueMap);
            component.enable(valueMap);
            Map<String, String> map = component.improve(market, srv.conf, profitdata, positions);
            retMap.putAll(map);
        }
        return retMap;
    }

    private ProfitInputData filterMemoryListMapsWithConfidence(Market market, Map<Object[], List<MemoryItem>> listMap) {
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
            System.out.println("Mark " + market.getConfig().getMarket() + " " + keys[0] + " " + min + " " + max );
            //Double conf = market.getConfidence();
            //System.out.println(conf);
            badListMap.put(keys, listMap.get(keys));
            badConfMap.put(keys, min);
        }
        ProfitInputData input = new ProfitInputData();
        input.setConfMap(badConfMap);
        input.setListMap(badListMap);
        return input;
    }

    private List<MemoryItem> getMarketMemory(Market market) {
        List<MemoryItem> marketMemory = null;
        try {
            marketMemory = IclijDbDao.getAll(market.getConfig().getMarket());
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
    
    @Deprecated
    private List<MarketFilter> getTradeMarkets(IclijConfig instance) {
        List<MarketFilter> markets = null;
        try { 
            markets = IclijXMLConfig.getFilterMarkets(instance);
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

