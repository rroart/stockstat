package roart.iclij.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MapList;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.TimingItem;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.ControlService;
import roart.iclij.service.IclijServiceList;
import roart.iclij.service.IclijServiceResult;
import roart.common.constants.Constants;
import roart.common.constants.ResultMetaConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.db.IclijDbDao;
import roart.iclij.config.Market;

public class MiscUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    // ?
    @Deprecated
    public boolean getEvolve(int verificationdays, ComponentInput componentInput) {
        if (true) return true;
        Boolean evolvefirst;
        if (verificationdays > 0) {
            evolvefirst = componentInput.getConfig().verificationEvolveFirstOnly();
        } else {
            evolvefirst = componentInput.getConfig().singlemarketEvolveFirstOnly();
        }
        boolean evolve = true;
        if (evolvefirst && componentInput.getLoopoffset() > 0) {
            evolve = false;
        }
        return evolve;
    }

    IclijServiceList getHeader(String title) {
        IclijServiceList header = new IclijServiceList();
        header.setTitle(title);
        return header;
    }

    public List<TimingItem> getCurrentTimingsRecord(LocalDate date, List<TimingItem> listAll, Market market, String action, int days, boolean inclusiveStart) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        List<TimingItem> filterListAll = listAll.stream().filter(m -> m.getRecord() != null).collect(Collectors.toList());
        filterListAll = filterListAll.stream().filter(m -> action.equals(m.getAction())).collect(Collectors.toList());
        List<TimingItem> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getRecord()) < 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getRecord()) >= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    public List<TimingItem> getCurrentTimings(LocalDate date, List<TimingItem> listAll, Market market, String action, int days, boolean inclusiveStart) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        List<TimingItem> filterListAll = listAll.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        filterListAll = filterListAll.stream().filter(m -> action.equals(m.getAction())).collect(Collectors.toList());
        List<TimingItem> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getDate()) < 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    public List<TimingItem> getCurrentTimings(LocalDate date, List<TimingItem> listAll, Market market, String action) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        List<TimingItem> filterListAll = listAll.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        filterListAll = filterListAll.stream().filter(m -> action.equals(m.getAction())).collect(Collectors.toList());
        List<TimingItem> currentIncDecs = filterListAll.stream().filter(m -> newdate.compareTo(m.getDate()) == 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    public List<MemoryItem> getCurrentMemories(LocalDate date, List<MemoryItem> listAll, Market market, int days, boolean inclusiveStart) {
        //System.out.println(market.getConfig().getMarket());
        Map<String, Long> countMap;
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        List<MemoryItem> filterListAll = listAll;
        if (market != null) {
            filterListAll = filterListAll.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        }
        List<MemoryItem> currentIncDecs = filterListAll;
        currentIncDecs = currentIncDecs.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        countMap = currentIncDecs.stream().collect(Collectors.groupingBy(e -> e.getMarket(), Collectors.counting()));
        //System.out.println(countMap);
        currentIncDecs = currentIncDecs.stream().filter(m -> olddate.compareTo(m.getDate()) < 0).collect(Collectors.toList());
        countMap = currentIncDecs.stream().collect(Collectors.groupingBy(e -> e.getMarket(), Collectors.counting()));
        //System.out.println(countMap);
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        countMap = currentIncDecs.stream().collect(Collectors.groupingBy(e -> e.getMarket(), Collectors.counting()));
        //System.out.println(countMap);
        return currentIncDecs;
    }

    public List<MLMetricsItem> getCurrentMLMetrics(LocalDate date, List<MLMetricsItem> listAll, Market market, int days, boolean inclusiveStart) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        List<MLMetricsItem> filterListAll = listAll.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        List<MLMetricsItem> currentTests = filterListAll.stream().filter(m -> olddate.compareTo(m.getDate()) < 0).collect(Collectors.toList());
        currentTests = currentTests.stream().filter(m -> newdate.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        currentTests = currentTests.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentTests;
    }

    public List<IncDecItem> getCurrentIncDecs(LocalDate date, List<IncDecItem> listAll, Market market, int days, boolean inclusiveStart) {
        //System.out.println(market.getConfig().getMarket());
        Map<String, Long> countMap;
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        List<IncDecItem> filterListAll = listAll;
        if (market != null) {
            filterListAll = filterListAll.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        }
        List<IncDecItem> currentIncDecs = filterListAll;
        currentIncDecs = currentIncDecs.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        countMap = currentIncDecs.stream().collect(Collectors.groupingBy(e -> e.getMarket(), Collectors.counting()));
        //System.out.println(countMap);
        currentIncDecs = currentIncDecs.stream().filter(m -> olddate.compareTo(m.getDate()) < 0).collect(Collectors.toList());
        countMap = currentIncDecs.stream().collect(Collectors.groupingBy(e -> e.getMarket(), Collectors.counting()));
        //System.out.println(countMap);
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        countMap = currentIncDecs.stream().collect(Collectors.groupingBy(e -> e.getMarket(), Collectors.counting()));
        //System.out.println(countMap);
        return currentIncDecs;
    }

    public List<IncDecItem> getCurrentIncDecs(List<IncDecItem> listAll, String parameters) {
        if (parameters != null) {
            return listAll.stream().filter(m -> parameters.equals(m.getParameters())).collect(Collectors.toList());
        }
        return listAll;
    }

    public List<IncDecItem> getIncDecsWithComponent(List<IncDecItem> listAll, List<String> components) {
        return listAll.stream().filter(m -> components.contains(m.getComponent())).collect(Collectors.toList());
    }

    public List<IncDecItem> getIncDecsWithSubcomponent(List<IncDecItem> listAll, List<String> subcomponents) {
        return listAll.stream().filter(m -> subcomponents.contains(m.getSubcomponent())).collect(Collectors.toList());
    }

    public Set<IncDecItem> moveAndGetCommon(Set<IncDecItem> listInc, Set<IncDecItem> listDec) {
        // and a new list for common items
        Set<String> incIds = listInc.stream().map(IncDecItem::getId).collect(Collectors.toSet());
        Set<String> decIds = listDec.stream().map(IncDecItem::getId).collect(Collectors.toSet());
        Set<String> commonIds = new HashSet<>(incIds);
        commonIds.retainAll(decIds);
        Set<IncDecItem> common = listInc.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toSet());
        common.addAll(listDec.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toSet()));
        listInc.removeAll(common);
        listDec.removeAll(common);
        return common;
    }

    public Set<IncDecItem> moveAndGetCommon(Set<IncDecItem> listInc, Set<IncDecItem> listDec, boolean verify) {
        // and a new list for common items
        Set<String> incIds = listInc.stream().map(IncDecItem::getId).collect(Collectors.toSet());
        Set<String> decIds = listDec.stream().map(IncDecItem::getId).collect(Collectors.toSet());
        Set<String> commonIds = new HashSet<>(incIds);
        commonIds.retainAll(decIds);
        Set<IncDecItem> common = listInc.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toSet());
        common.addAll(listDec.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toSet()));
        listInc.removeAll(common);
        listDec.removeAll(common);
        if (true) {
            Set<IncDecItem> mergecommon = new HashSet<>();
            for (String id : commonIds) {
                IncDecItem inc = common.stream().filter(item -> id.equals(item.getId()) && item.isIncrease()).findAny().orElse(null);
                IncDecItem dec = common.stream().filter(item -> id.equals(item.getId()) && !item.isIncrease()).findAny().orElse(null);
                IncDecItem mergeitem = new IncDecItem();
                mergeitem.setComponent(inc.getComponent());
                mergeitem.setDate(inc.getDate());
                mergeitem.setDescription("Up: " + inc.getDescription() + " Down: " + dec.getDescription());
                mergeitem.setId(id);
                mergeitem.setIncrease(inc.getScore() > dec.getScore());
                mergeitem.setLocalcomponent("Up: " + inc.getLocalcomponent() + " Down: " + dec.getLocalcomponent());
                mergeitem.setMarket(inc.getMarket());
                mergeitem.setName(inc.getName());
                mergeitem.setParameters(inc.getParameters());
                mergeitem.setScore(mergeitem.isIncrease() ? inc.getScore() - dec.getScore() : dec.getScore() - inc.getScore());
                mergeitem.setSubcomponent(inc.getSubcomponent());
                mergecommon.add(mergeitem);
            }
            common = mergecommon;
        }
        return common;
    }

    public Set<IncDecItem> moveAndGetCommon2(Set<IncDecItem> listInc, Set<IncDecItem> listDec, boolean verify) {
        // and a new list for common items
        Set<String> incIds = new HashSet<>();
        for (IncDecItem item : listInc) {
            String id = item.getId() + item.getDate().toString();
            incIds.add(id);
        }
        Set<String> decIds = new HashSet<>();
        for (IncDecItem item : listDec) {
            String id = item.getId() + item.getDate().toString();
            decIds.add(id);
        }
        Set<String> commonIds = new HashSet<>(incIds);
        commonIds.retainAll(decIds);
        Set<IncDecItem> common = listInc.stream().filter(m -> commonIds.contains(m.getId() + m.getDate().toString())).collect(Collectors.toSet());
        common.addAll(listDec.stream().filter(m -> commonIds.contains(m.getId() + m.getDate().toString())).collect(Collectors.toSet()));
        listInc.removeAll(common);
        listDec.removeAll(common);
        if (true) {
            Set<IncDecItem> mergecommon = new HashSet<>();
            for (String id : commonIds) {
                IncDecItem inc = common.stream().filter(item -> id.equals(item.getId() + item.getDate().toString()) && item.isIncrease()).findAny().orElse(null);
                IncDecItem dec = common.stream().filter(item -> id.equals(item.getId() + item.getDate().toString()) && !item.isIncrease()).findAny().orElse(null);
                IncDecItem mergeitem = new IncDecItem();
                mergeitem.setComponent(inc.getComponent());
                mergeitem.setDate(inc.getDate());
                mergeitem.setDescription("Up: " + inc.getDescription() + " Down: " + dec.getDescription());
                mergeitem.setId(inc.getId());
                mergeitem.setIncrease(inc.getScore() > dec.getScore());
                mergeitem.setLocalcomponent("Up: " + inc.getLocalcomponent() + " Down: " + dec.getLocalcomponent());
                mergeitem.setMarket(inc.getMarket());
                mergeitem.setName(inc.getName());
                mergeitem.setParameters(inc.getParameters());
                mergeitem.setScore(mergeitem.isIncrease() ? inc.getScore() - dec.getScore() : dec.getScore() - inc.getScore());
                mergeitem.setSubcomponent(inc.getSubcomponent());
                mergecommon.add(mergeitem);
            }
            common = mergecommon;
        }
        return common;
    }

    public Map<String, Object> loadConfig(ControlService srv, ComponentInput componentInput, Market market, String marketName, String action, String component, boolean evolve, Boolean buy, String subcomponent, MarketActionData marketaction, Parameters parameters) throws Exception {
        //LocalDate date = componentInput.getEnddate();
        //LocalDate olddate = date.minusDays(2 * marketaction.getTime(market));
        List<ConfigItem> filterConfigs = new ArrayList<>();
        List<ConfigItem> configs = IclijDbDao.getAllConfigs(market.getConfig().getMarket(), action, component, subcomponent, JsonUtil.convert(parameters), null, null);
        for (ConfigItem config : configs) {
            if (buy != null && config.getBuy() != null && buy != config.getBuy()) {
                continue;
            }
            filterConfigs.add(config);
        }
        Collections.sort(filterConfigs, (o1, o2) -> (o2.getRecord().compareTo(o1.getRecord())));
        Map<String, Class> type = srv.conf.getType();
        Map<String, Object> updateMap = new HashMap<>();
        if (filterConfigs.isEmpty()) {
        	return updateMap;
        }
        ConfigItem config = filterConfigs.get(0);
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
        return updateMap;
    }

    public void print(IclijServiceResult result) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (IclijServiceList item : result.getLists()) {
                listWriter(writer, item, item.getList());            
            }
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    private void listWriter(BufferedWriter writer, IclijServiceList item, List mylist) {
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

    //@Override
    public List<MemoryItem> getMarketMemory2(Market market) {
        return new ArrayList<>();
    }

    //@Override
    public List<MemoryItem> filterKeepRecent2(List<MemoryItem> marketMemory, LocalDate date, int days) {
        return marketMemory;
    }

    public List<MemoryItem> filterKeepRecent(List<MemoryItem> marketMemory, LocalDate date, int days, boolean inclusiveStart) {
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        for (MemoryItem item : marketMemory) {
            if (item.getRecord() == null) {
                item.setRecord(LocalDate.now());
            }
        }
        // temp workaround
        if (date == null) {
            return marketMemory;
        }
        List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getFuturedate()) < 0).collect(Collectors.toList());
        currentList = currentList.stream().filter(m -> date.compareTo(m.getFuturedate()) >= 0).collect(Collectors.toList());
        return currentList;
    }

    public List<MemoryItem> filterKeepRecent3(List<MemoryItem> marketMemory, LocalDate date, int days, boolean inclusiveStart) {
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        for (MemoryItem item : marketMemory) {
            if (item.getRecord() == null) {
                item.setRecord(LocalDate.now());
            }
        }
        // temp workaround
        if (date == null) {
            return marketMemory;
        }
        List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getDate()) < 0).collect(Collectors.toList());
        currentList = currentList.stream().filter(m -> date.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        return currentList;
    }

    public List<MapList> getList(Map<String, Object> map) {
        List<MapList> retList = new ArrayList<>();
        for (Entry<String, Object> entry : map.entrySet()) {
            MapList ml = new MapList();
            ml.setKey(entry.getKey());
            ml.setValue((String) entry.getValue().toString());
            retList.add(ml);
        }
        return retList;
    }

    public Set<IncDecItem> mergeList(Collection<IncDecItem> itemList, boolean splitid) {
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
                IncDecItem mergeitem = new IncDecItem();
                mergeitem.setComponent(item.getComponent());
                mergeitem.setDate(item.getDate());
                mergeitem.setDescription(item.getDescription());
                mergeitem.setId(item.getId());
                mergeitem.setIncrease(item.isIncrease());
                mergeitem.setLocalcomponent(item.getLocalcomponent());
                mergeitem.setMarket(item.getMarket());
                mergeitem.setName(item.getName());
                mergeitem.setParameters(item.getParameters());
                mergeitem.setScore(item.getScore());
                mergeitem.setSubcomponent(item.getSubcomponent());
                map.put(id, mergeitem);
            } else {
                getItem.setScore(getItem.getScore() + item.getScore());
                getItem.setDescription(getItem.getDescription() + ", " + item.getDescription());
            }
        }
        return new HashSet<>(map.values());
    }

    public List<String> getParameters(List<IncDecItem> incdecs) {
        return incdecs
                .stream()
                .map(IncDecItem::getParameters)
                .distinct()
                .collect(Collectors.toList());
     }

    public List<IncDecItem> getIncDecLocals(List<IncDecItem> incdecs) {
        List<IncDecItem> locals = new ArrayList<>();
        for (IncDecItem item : incdecs) {
            String localcomponent = item.getLocalcomponent();
            String[] localcomponents = null;
            if (localcomponent != null) {
                localcomponents = localcomponent.split(" ");
            }
            if (localcomponents != null && localcomponents.length > 1) {
                for (String aLocalcomponent : localcomponents) {
                    IncDecItem newItem = new IncDecItem();
                    newItem.setComponent(item.getComponent());
                    newItem.setDate(item.getDate());
                    newItem.setDescription(item.getDescription());
                    newItem.setId(item.getId());
                    newItem.setIncrease(item.isIncrease());
                    newItem.setLocalcomponent(aLocalcomponent);
                    newItem.setMarket(item.getMarket());
                    newItem.setName(item.getName());
                    newItem.setParameters(item.getParameters());
                    newItem.setRecord(item.getRecord());
                    newItem.setScore(item.getScore());
                    newItem.setSubcomponent(item.getSubcomponent());;
                    locals.add(newItem);
                }
            } else {
                locals.add(item);
            }
        }
        return locals;
    }

    private <K, E> List<E> listGetter(Map<K, List<E>> listMap, K key) {
        return listMap.computeIfAbsent(key, k -> new ArrayList<>());
    }

    public <K, E> void listGetterAdder(Map<K, List<E>> listMap, K key, E element) {
        List<E> list = listGetter(listMap, key);
        list.add(element);
    }

    public Pair<String, String> getComponentPair(List meta) {
        String mlname = (String) meta.get(ResultMetaConstants.MLNAME);
        String modelname = (String) meta.get(ResultMetaConstants.MODELNAME);
        String subtype = (String) meta.get(ResultMetaConstants.SUBTYPE);
        String subsubtype = (String) meta.get(ResultMetaConstants.SUBSUBTYPE);
        String subcomponent = mlname + " " + modelname;
        String localcomponent = null;
        if (subtype != null) {
            localcomponent = subtype + subsubtype;
        }
        return new ImmutablePair<>(subcomponent, localcomponent);
    }

    public Pair<String, String> getSubComponentPair(List meta) {
        String mlname = (String) meta.get(ResultMetaConstants.MLNAME);
        String modelname = (String) meta.get(ResultMetaConstants.MODELNAME);
        return new ImmutablePair(mlname, modelname);
    }
    
    // Duplicated
    public double fitness(Collection<IncDecItem> myincs, Collection<IncDecItem> mydecs, Collection<IncDecItem> myincdec, int minimum, Boolean buy) {
        double incdecFitness;
        int fitnesses = 0;
        double decfitness = 0;
        Pair<Long, Integer> dec = new ImmutablePair(Long.valueOf(0), 0);
        if (buy == null || buy == false) {
            dec = countsize(mydecs);
        }
        if (dec.getRight() != 0) {
            decfitness = ((double) dec.getLeft()) / dec.getRight();
            fitnesses++;
        }
        Pair<Long, Integer> inc = new ImmutablePair(Long.valueOf(0), 0);
        if (buy == null || buy == true) {
            inc = countsize(myincs);
        }
        double incfitness = 0;
        if (inc.getRight() != 0) {
            incfitness = ((double) inc.getLeft()) / inc.getRight();
            fitnesses++;
        }
        Pair<Long, Integer> incdec = new ImmutablePair(Long.valueOf(0), 0);
        if (true) {
            incdec = countsize(myincdec);
            fitnesses++;
        }
        double incdecfitness = 0;
        if (incdec.getRight() != 0) {
            incdecfitness = ((double) incdec.getLeft()) / incdec.getRight();
            fitnesses++;
        }
        
        double fitnessAvg = 0;
        if (fitnesses != 0) {
            fitnessAvg = (decfitness + incfitness + incdecfitness) / fitnesses;
        }
        incdecFitness = fitnessAvg;
        double fitnessAll = 0;
        long count = dec.getLeft() + inc.getLeft() + incdec.getLeft();
        int size = dec.getRight() + inc.getRight() + incdec.getRight();
        if (size > 0) {
            fitnessAll = ((double) count) / size;
        }
        incdecFitness = fitnessAll;
        if (minimum > 0 && size < minimum) {
            log.info("Fit sum too small {} < {}", size, minimum);
            incdecFitness = 0;
        }
        log.info("Fit {} ( {} / {} ) {} ( {} / {} ) {} ( {} / {} ) {} {} ( {} / {} )", decfitness, dec.getLeft(), dec.getRight(), incfitness, inc.getLeft(), inc.getRight(), incdecfitness, incdec.getLeft(), incdec.getRight(), fitnessAvg, fitnessAll, count, size);
        return incdecFitness;
    }
    
    public Pair<Long, Integer>[] fitness2(Collection<IncDecItem> myincs, Collection<IncDecItem> mydecs, Collection<IncDecItem> myincdec, int minimum, Boolean buy) {
        double incdecFitness;
        int fitnesses = 0;
        double decfitness = 0;
        Pair<Long, Integer> dec = new ImmutablePair(Long.valueOf(0), 0);
        if (buy == null || buy == false) {
            dec = countsize(mydecs, false);
        }
        if (dec.getRight() != 0) {
            decfitness = ((double) dec.getLeft()) / dec.getRight();
            fitnesses++;
        }
        Pair<Long, Integer> inc = new ImmutablePair(Long.valueOf(0), 0);
        if (buy == null || buy == true) {
            inc = countsize(myincs, true);
        }
        double incfitness = 0;
        if (inc.getRight() != 0) {
            incfitness = ((double) inc.getLeft()) / inc.getRight();
            fitnesses++;
        }
        Pair<Long, Integer> incdecabove = new ImmutablePair(Long.valueOf(0), 0);
        Pair<Long, Integer> incdecbelow = new ImmutablePair(Long.valueOf(0), 0);
        if (true) {
            incdecabove = countsize(myincdec, true);
            incdecbelow = countsize(myincdec, false);
            fitnesses++;
        }
        double incdecfitness = 0;
        if (incdecabove.getRight() != 0) {
            incdecfitness = ((double) incdecabove.getLeft()) / incdecabove.getRight();
            fitnesses++;
        }
        
        double fitnessAvg = 0;
        if (fitnesses != 0) {
            fitnessAvg = (decfitness + incfitness + incdecfitness) / fitnesses;
        }
        incdecFitness = fitnessAvg;
        double fitnessAll = 0;
        long count = dec.getLeft() + inc.getLeft() + incdecabove.getLeft();
        int size = dec.getRight() + inc.getRight() + incdecabove.getRight();
        if (size > 0) {
            fitnessAll = ((double) count) / size;
        }
        incdecFitness = fitnessAll;
        if (minimum > 0 && size < minimum) {
            //log.info("Fit sum too small {} < {}", size, minimum);
            incdecFitness = 0;
        }
        //log.info("Fit {} ( {} / {} ) {} ( {} / {} ) {} ( {} / {} ) {} {} ( {} / {} )", decfitness, dec.getLeft(), dec.getRight(), incfitness, inc.getLeft(), inc.getRight(), incdecfitness, incdec.getLeft(), incdec.getRight(), fitnessAvg, fitnessAll, count, size);
        Pair<Long, Integer>[] pairs = new ImmutablePair[2];
        pairs[0] = new ImmutablePair<Long, Integer>(inc.getLeft() + incdecabove.getLeft(), inc.getRight() + incdecabove.getRight());
        pairs[1] = new ImmutablePair<Long, Integer>(dec.getLeft() + incdecbelow.getLeft(), dec.getRight() + incdecbelow.getRight());
        return pairs;
    }
    
    public static Pair<Long, Integer> countsize(Collection<IncDecItem> list) {
        List<Boolean> listBoolean = list.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
        long count = listBoolean.stream().filter(i -> i).count();                            
        int size = listBoolean.size();
        return new ImmutablePair(count, size);

    }
    
    public static Pair<Long, Integer> countsize(Collection<IncDecItem> list, boolean above) {
        list = list.stream().filter(e -> e.isIncrease() == above).collect(Collectors.toList());
        List<Boolean> listBoolean = list.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
        long count = listBoolean.stream().filter(i -> i).count();                            
        int size = listBoolean.size();
        return new ImmutablePair(count, size);

    }
    
}
