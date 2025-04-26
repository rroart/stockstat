package roart.iclij.service.util;

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
import org.springframework.beans.factory.annotation.Autowired;

import roart.iclij.model.MapList;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.ControlService;
import roart.iclij.service.IclijServiceList;
import roart.iclij.service.IclijServiceResult;
import roart.common.constants.Constants;
import roart.common.constants.ResultMetaConstants;
import roart.common.model.ConfigDTO;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.common.model.TimingDTO;
import roart.common.pipeline.data.SerialResultMeta;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.iclij.config.Market;
import org.springframework.stereotype.Component;

@Component
public class MiscUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    IclijServiceList getHeader(String title) {
        IclijServiceList header = new IclijServiceList();
        header.setTitle(title);
        return header;
    }

    public List<TimingDTO> getCurrentTimingsRecord(LocalDate date, List<TimingDTO> listAll, Market market, String action, int days, boolean inclusiveStart) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        List<TimingDTO> filterListAll = listAll.stream().filter(m -> m.getRecord() != null).collect(Collectors.toList());
        filterListAll = filterListAll.stream().filter(m -> action.equals(m.getAction())).collect(Collectors.toList());
        List<TimingDTO> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getRecord()) < 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getRecord()) >= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    public List<TimingDTO> getCurrentTimings(LocalDate date, List<TimingDTO> listAll, Market market, String action, int days, boolean inclusiveStart) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        List<TimingDTO> filterListAll = listAll.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        filterListAll = filterListAll.stream().filter(m -> action.equals(m.getAction())).collect(Collectors.toList());
        List<TimingDTO> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getDate()) < 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    public List<TimingDTO> getCurrentTimings(LocalDate date, List<TimingDTO> listAll, Market market, String action) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        List<TimingDTO> filterListAll = listAll.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        filterListAll = filterListAll.stream().filter(m -> action.equals(m.getAction())).collect(Collectors.toList());
        List<TimingDTO> currentIncDecs = filterListAll.stream().filter(m -> newdate.compareTo(m.getDate()) == 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    public List<MemoryDTO> getCurrentMemories(LocalDate date, List<MemoryDTO> listAll, Market market, int days, boolean inclusiveStart) {
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
        List<MemoryDTO> filterListAll = listAll;
        if (market != null) {
            filterListAll = filterListAll.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        }
        List<MemoryDTO> currentIncDecs = filterListAll;
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

    public List<MLMetricsDTO> getCurrentMLMetrics(LocalDate date, List<MLMetricsDTO> listAll, Market market, int days, boolean inclusiveStart) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        List<MLMetricsDTO> filterListAll = listAll.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        List<MLMetricsDTO> currentTests = filterListAll.stream().filter(m -> olddate.compareTo(m.getDate()) < 0).collect(Collectors.toList());
        currentTests = currentTests.stream().filter(m -> newdate.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        currentTests = currentTests.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentTests;
    }

    public List<IncDecDTO> getCurrentIncDecs(LocalDate date, List<IncDecDTO> listAll, Market market, int days, boolean inclusiveStart) {
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
        List<IncDecDTO> filterListAll = listAll;
        if (market != null) {
            filterListAll = filterListAll.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        }
        List<IncDecDTO> currentIncDecs = filterListAll;
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

    public List<IncDecDTO> getCurrentIncDecs(List<IncDecDTO> listAll, String parameters) {
        if (parameters != null) {
            return listAll.stream().filter(m -> parameters.equals(m.getParameters())).collect(Collectors.toList());
        }
        return listAll;
    }

    public List<IncDecDTO> getIncDecsWithComponent(List<IncDecDTO> listAll, List<String> components) {
        return listAll.stream().filter(m -> components.contains(m.getComponent())).collect(Collectors.toList());
    }

    public List<IncDecDTO> getIncDecsWithSubcomponent(List<IncDecDTO> listAll, List<String> subcomponents) {
        return listAll.stream().filter(m -> subcomponents.contains(m.getSubcomponent())).collect(Collectors.toList());
    }

    public Set<IncDecDTO> moveAndGetCommon(Set<IncDecDTO> listInc, Set<IncDecDTO> listDec) {
        // and a new list for common items
        Set<String> incIds = listInc.stream().map(IncDecDTO::getId).collect(Collectors.toSet());
        Set<String> decIds = listDec.stream().map(IncDecDTO::getId).collect(Collectors.toSet());
        Set<String> commonIds = new HashSet<>(incIds);
        commonIds.retainAll(decIds);
        Set<IncDecDTO> common = listInc.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toSet());
        common.addAll(listDec.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toSet()));
        listInc.removeAll(common);
        listDec.removeAll(common);
        return common;
    }

    public Set<IncDecDTO> moveAndGetCommon(Set<IncDecDTO> listInc, Set<IncDecDTO> listDec, boolean verify) {
        // and a new list for common items
        Set<String> incIds = listInc.stream().map(IncDecDTO::getId).collect(Collectors.toSet());
        Set<String> decIds = listDec.stream().map(IncDecDTO::getId).collect(Collectors.toSet());
        Set<String> commonIds = new HashSet<>(incIds);
        commonIds.retainAll(decIds);
        Set<IncDecDTO> common = listInc.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toSet());
        common.addAll(listDec.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toSet()));
        listInc.removeAll(common);
        listDec.removeAll(common);
        if (true) {
            Set<IncDecDTO> mergecommon = new HashSet<>();
            for (String id : commonIds) {
                IncDecDTO inc = common.stream().filter(item -> id.equals(item.getId()) && item.isIncrease()).findAny().orElse(null);
                IncDecDTO dec = common.stream().filter(item -> id.equals(item.getId()) && !item.isIncrease()).findAny().orElse(null);
                IncDecDTO mergeitem = new IncDecDTO();
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

    public Set<IncDecDTO> moveAndGetCommon2(Set<IncDecDTO> listInc, Set<IncDecDTO> listDec, boolean verify) {
        // and a new list for common items
        Set<String> incIds = new HashSet<>();
        for (IncDecDTO item : listInc) {
            String id = item.getId() + item.getDate().toString();
            incIds.add(id);
        }
        Set<String> decIds = new HashSet<>();
        for (IncDecDTO item : listDec) {
            String id = item.getId() + item.getDate().toString();
            decIds.add(id);
        }
        Set<String> commonIds = new HashSet<>(incIds);
        commonIds.retainAll(decIds);
        Set<IncDecDTO> common = listInc.stream().filter(m -> commonIds.contains(m.getId() + m.getDate().toString())).collect(Collectors.toSet());
        common.addAll(listDec.stream().filter(m -> commonIds.contains(m.getId() + m.getDate().toString())).collect(Collectors.toSet()));
        listInc.removeAll(common);
        listDec.removeAll(common);
        if (true) {
            Set<IncDecDTO> mergecommon = new HashSet<>();
            for (String id : commonIds) {
                IncDecDTO inc = common.stream().filter(item -> id.equals(item.getId() + item.getDate().toString()) && item.isIncrease()).findAny().orElse(null);
                IncDecDTO dec = common.stream().filter(item -> id.equals(item.getId() + item.getDate().toString()) && !item.isIncrease()).findAny().orElse(null);
                IncDecDTO mergeitem = new IncDecDTO();
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
        List<ConfigDTO> filterConfigs = new ArrayList<>();
        List<ConfigDTO> configs = srv.getIo().getIdbDao().getAllConfigs(market.getConfig().getMarket(), action, component, subcomponent, JsonUtil.convert(parameters), null, null);
        for (ConfigDTO config : configs) {
            if (buy != null && config.getBuy() != null && buy != config.getBuy()) {
                continue;
            }
            filterConfigs.add(config);
        }
        Collections.sort(filterConfigs, (o1, o2) -> (o2.getRecord().compareTo(o1.getRecord())));
        Map<String, Class> type = srv.coremlconf.getConfigData().getConfigMaps().map;
        Map<String, Object> updateMap = new HashMap<>();
        if (filterConfigs.isEmpty()) {
        	return updateMap;
        }
        ConfigDTO config = filterConfigs.get(0);
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
    public List<MemoryDTO> getMarketMemory2(Market market) {
        return new ArrayList<>();
    }

    //@Override
    public List<MemoryDTO> filterKeepRecent2(List<MemoryDTO> marketMemory, LocalDate date, int days) {
        return marketMemory;
    }

    public List<MemoryDTO> filterKeepRecent(List<MemoryDTO> marketMemory, LocalDate date, int days, boolean inclusiveStart) {
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        for (MemoryDTO item : marketMemory) {
            if (item.getRecord() == null) {
                item.setRecord(LocalDate.now());
            }
        }
        // temp workaround
        if (date == null) {
            return marketMemory;
        }
        List<MemoryDTO> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getFuturedate()) < 0).collect(Collectors.toList());
        currentList = currentList.stream().filter(m -> date.compareTo(m.getFuturedate()) >= 0).collect(Collectors.toList());
        return currentList;
    }

    public List<MemoryDTO> filterKeepRecent3(List<MemoryDTO> marketMemory, LocalDate date, int days, boolean inclusiveStart) {
        if (inclusiveStart) {
            days++;
        }
        LocalDate olddate = date.minusDays(days);
        for (MemoryDTO item : marketMemory) {
            if (item.getRecord() == null) {
                item.setRecord(LocalDate.now());
            }
        }
        // temp workaround
        if (date == null) {
            return marketMemory;
        }
        List<MemoryDTO> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getDate()) < 0).collect(Collectors.toList());
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

    public Set<IncDecDTO> mergeList(Collection<IncDecDTO> itemList, boolean splitid) {
        Map<String, IncDecDTO> map = new HashMap<>();
        for (IncDecDTO item : itemList) {
            String id;
            if (!splitid) {
                id = item.getId();
            } else {
                id = item.getId() + item.getDate().toString();
            }
            IncDecDTO getDTO = map.get(id);
            if (getDTO == null) {
                IncDecDTO mergeitem = new IncDecDTO();
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
                getDTO.setScore(getDTO.getScore() + item.getScore());
                getDTO.setDescription(getDTO.getDescription() + ", " + item.getDescription());
            }
        }
        return new HashSet<>(map.values());
    }

    public List<String> getParameters(List<IncDecDTO> incdecs) {
        return incdecs
                .stream()
                .map(IncDecDTO::getParameters)
                .distinct()
                .collect(Collectors.toList());
     }

    public List<IncDecDTO> getIncDecLocals(List<IncDecDTO> incdecs) {
        List<IncDecDTO> locals = new ArrayList<>();
        for (IncDecDTO item : incdecs) {
            String localcomponent = item.getLocalcomponent();
            String[] localcomponents = null;
            if (localcomponent != null) {
                localcomponents = localcomponent.split(" ");
            }
            if (localcomponents != null && localcomponents.length > 1) {
                for (String aLocalcomponent : localcomponents) {
                    IncDecDTO newDTO = new IncDecDTO();
                    newDTO.setComponent(item.getComponent());
                    newDTO.setDate(item.getDate());
                    newDTO.setDescription(item.getDescription());
                    newDTO.setId(item.getId());
                    newDTO.setIncrease(item.isIncrease());
                    newDTO.setLocalcomponent(aLocalcomponent);
                    newDTO.setMarket(item.getMarket());
                    newDTO.setName(item.getName());
                    newDTO.setParameters(item.getParameters());
                    newDTO.setRecord(item.getRecord());
                    newDTO.setScore(item.getScore());
                    newDTO.setSubcomponent(item.getSubcomponent());;
                    locals.add(newDTO);
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

    public Pair<String, String> getComponentPair(SerialResultMeta meta) {
        String mlname = (String) meta.getMlName();
        String modelname = (String) meta.getModelName();
        String subtype = (String) meta.getSubType();
        String subsubtype = (String) meta.getSubSubType();
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
    
    public Pair<String, String> getSubComponentPair(SerialResultMeta meta) {
        String mlname = (String) meta.getMlName();
        String modelname = (String) meta.getModelName();
        return new ImmutablePair(mlname, modelname);
    }
    
}
