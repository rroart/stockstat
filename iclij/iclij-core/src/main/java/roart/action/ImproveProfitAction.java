package roart.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import roart.component.Component;
import roart.component.ComponentFactory;
import roart.config.IclijConfig;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.config.TradeMarket;
import roart.db.IclijDbDao;
import roart.model.IncDecItem;
import roart.model.MemoryItem;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;

public class ImproveProfitAction extends Action {
    @Override
    public void goal() {
        // test picks for aggreg recommend, predict etc
        // remember and make confidence
        // memory is with date, confidence %, inc/dec, semantic item
        // find recommended picks
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        //instance.
        List<MemoryItem> memory = null;
        try {
            memory = IclijDbDao.getAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<MemoryItem> toCheck = new ArrayList<>();
        List<Market> markets = null;
        try { 
            markets = instance.getMarkets();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (Market market : markets) {
            String marketName = market.getMarket();
            List<MemoryItem> marketMemory = null;
            try {
                marketMemory = IclijDbDao.getAll(market.getMarket());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, - market.getImprovetime() );
            Date olddate = cal.getTime();
            //DateUtils.is
            List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
            // or make a new object instead of the object array. use this as a pair
            //System.out.println(currentList.get(0).getRecord());
            Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
            // map subcat + posit -> list
            currentList.forEach(m -> { listGetterAdder(listMap, new Object[]{m.getComponent(), m.getPosition() }, m); } );
            Map<Object[], List<MemoryItem>> badListMap = new HashMap<>();
            Map<Object[], Double> badConfMap = new HashMap<>();
            for(Object[] keys : listMap.keySet()) {
                List<MemoryItem> memoryList = listMap.get(keys);
                List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
               //System.out.println("confs" + confidences);
                if (confidences.isEmpty()) {
                    int jj = 0;
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
            Map<String, String> nameMap = null;
            for (String key : maps.keySet()) {
                Map<String, Object> map = maps.get(key);
                nameMap = (Map<String, String>) map.get(PipelineConstants.NAME);
                if (nameMap != null) {
                    break;
                }
            }
            for (String componentName : listComponent.keySet()) {
                List<Integer> positions = listComponent.get(componentName);
                Component component = componentMap.get(componentName);
                Component.disabler(srv.conf);
                component.enable(srv.conf);
                component.improve(srv.conf, maps, positions, buys, sells, badConfMap, badListMap, nameMap);
               //System.out.println("Buys: " + market.getMarket() + buys);
               //System.out.println("Sells: " + market.getMarket() + sells);           
            }
            //String category = market.getInccategory();
            //Double threshold = market.getIncthreshold();
            //Map<String, Object> categoryMap = maps.get(category);
            //Map<String, List<List>> listMap3 = null;
            /*for (String mainkey : maps.keySet()) {
                Map<String, Object> map = maps.get(mainkey);
                for (String key : map.keySet()) {
                    if (category.equals(map.get(PipelineConstants.CATEGORYTITLE))) {
                        listMap3 = (Map<String, List<List>>) map.get(PipelineConstants.LIST);
                    }
                }
            }*/
            //Map<String, IncDecItem> buysFilter = new HashMap<>();
            /*
            for(IncDecItem item : buys.values()) {
                String key = item.getId();
                if (listMap3 == null) {
                    System.out.println(categoryMap.keySet());
                    System.out.println(maps.keySet());
                     System.out.println("market" + market.getMarket() + "null map");
                    continue;
                }
                List<List> list = listMap3.get(key);
                List<Double> list0 = list.get(0);
                Double value = list0.get(list0.size() - 1);
                if (value == null) {
                    continue;
                }
                if (value < threshold) {
                    continue;
                }
                buysFilter.put(key, item);
            }
            */
            /*
            buys = buysFilter;
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
                e.printStackTrace();
            }
            */
            //buys = buys.values().stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        }
    }
    
    void oldgoal() {
        // test picks for aggreg recommend, predict etc
        // remember and make confidence
        // memory is with date, confidence %, inc/dec, semantic item
        // find recommended picks
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        //instance.
        List<MemoryItem> memory = null;
        try {
            memory = IclijDbDao.getAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<MemoryItem> toCheck = new ArrayList<>();
        List<TradeMarket> markets = null;
        try { 
            markets = instance.getTradeMarkets();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (TradeMarket market : markets) {
            String marketName = market.getMarket();
            List<MemoryItem> marketMemory = null;
            try {
                marketMemory = IclijDbDao.getAll(market.getMarket());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, - market.getRecordage() );
            Date olddate = cal.getTime();
            //DateUtils.is
            List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
            // or make a new object instead of the object array. use this as a pair
            //System.out.println(currentList.get(0).getRecord());
            Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
            // map subcat + posit -> list
            currentList.forEach(m -> { listGetterAdder(listMap, new Object[]{m.getComponent(), m.getPosition() }, m); } );
            Map<Object[], List<MemoryItem>> badListMap = new HashMap<>();
            Map<Object[], Double> badConfMap = new HashMap<>();
            for(Object[] keys : listMap.keySet()) {
                List<MemoryItem> memoryList = listMap.get(keys);
                List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
               //System.out.println("confs" + confidences);
                if (confidences.isEmpty()) {
                    int jj = 0;
                }
                confidences = confidences.stream().filter(m -> !m.isNaN()).collect(Collectors.toList());
                Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
                if (!minOpt.isPresent()) {
                    continue;
                }
                Double min = minOpt.get();
                // do the bad ones
                // do not yet improve on the good enough ones
                if (min >= market.getConfidence()) {
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
            Component.disabler(srv.conf);
            srv.conf.setMarket(market.getMarket());
            Map<String, Component> componentMap = new HashMap<>();
            for (String componentName : listComponent.keySet()) {
                Component component = ComponentFactory.factory(componentName);
                component.enable(srv.conf);
                componentMap.put(componentName, component);
            }
            
            Map<String, IncDecItem> buys = new HashMap<>();
            Map<String, IncDecItem> sells = new HashMap<>();
            Map<String, Map<String, Object>> result0 = srv.getContent();
            Map<String, Map<String, Object>> maps = result0;
            Map<String, String> nameMap = null;
            for (String key : maps.keySet()) {
                Map<String, Object> map = maps.get(key);
                nameMap = (Map<String, String>) map.get(PipelineConstants.NAME);
                if (nameMap != null) {
                    break;
                }
            }
            for (String componentName : listComponent.keySet()) {
                List<Integer> positions = listComponent.get(componentName);
                Component component = componentMap.get(componentName);
                component.handle(srv, srv.conf, maps, positions, buys, sells, badConfMap, badListMap, nameMap);
               //System.out.println("Buys: " + market.getMarket() + buys);
               //System.out.println("Sells: " + market.getMarket() + sells);           
            }
            String category = market.getInccategory();
            Double threshold = market.getIncthreshold();
            Map<String, Object> categoryMap = maps.get(category);
            Map<String, List<List>> listMap3 = null;
            for (String mainkey : maps.keySet()) {
                Map<String, Object> map = maps.get(mainkey);
                for (String key : map.keySet()) {
                    if (category.equals(map.get(PipelineConstants.CATEGORYTITLE))) {
                        listMap3 = (Map<String, List<List>>) map.get(PipelineConstants.LIST);
                    }
                }
            }
            Map<String, IncDecItem> buysFilter = new HashMap<>();
            for(IncDecItem item : buys.values()) {
                String key = item.getId();
                if (listMap3 == null) {
                    System.out.println(categoryMap.keySet());
                    System.out.println(maps.keySet());
                     System.out.println("market" + market.getMarket() + "null map");
                    continue;
                }
                List<List> list = listMap3.get(key);
                List<Double> list0 = list.get(0);
                Double value = list0.get(list0.size() - 1);
                if (value == null) {
                    continue;
                }
                if (value < threshold) {
                    continue;
                }
                buysFilter.put(key, item);
            }
            buys = buysFilter;
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
                e.printStackTrace();
            }
            //buys = buys.values().stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        }
    }
    private <K, E> List<E> listGetter(Map<K, List<E>> listMap, K key) {
        List<E> list = listMap.get(key);
        if (list == null) {
            list = new ArrayList<>();
            //System.out.println("mapput " + key);
            listMap.put(key, list);
        }
        return list;
    }

    private <K, E> void listGetterAdder(Map<K, List<E>> listMap, K key, E element) {
        List<E> list = listGetter(listMap, key);
        list.add(element);
    }

    private <T> String nullToEmpty(T s) {
        return s != null ? "" + s : "";
    }
    
}

