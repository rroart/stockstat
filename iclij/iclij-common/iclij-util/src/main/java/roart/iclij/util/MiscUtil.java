package roart.iclij.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
import roart.common.util.JsonUtil;
import roart.db.IclijDbDao;
import roart.iclij.config.Market;

public class MiscUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public boolean getEvolve(int verificationdays, ComponentInput componentInput) {
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

    public List<TimingItem> getCurrentTimings(LocalDate date, List<TimingItem> listAll, Market market, String action, int days) {
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

    public List<MLMetricsItem> getCurrentMLMetrics(LocalDate date, List<MLMetricsItem> listAll, Market market, int days) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        LocalDate olddate = date.minusDays(days);
        List<MLMetricsItem> filterListAll = listAll.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        List<MLMetricsItem> currentTests = filterListAll.stream().filter(m -> olddate.compareTo(m.getDate()) <= 0).collect(Collectors.toList());
        currentTests = currentTests.stream().filter(m -> newdate.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        currentTests = currentTests.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentTests;
    }

    public List<IncDecItem> getCurrentIncDecs(LocalDate date, List<IncDecItem> listAll, Market market, int days) {
        System.out.println(market.getConfig().getMarket());
        Map<String, Long> countMap;
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        LocalDate olddate = date.minusDays(days);
        List<IncDecItem> filterListAll = listAll.stream().filter(m -> m.getDate() != null).collect(Collectors.toList());
        countMap = filterListAll.stream().collect(Collectors.groupingBy(e -> e.getMarket(), Collectors.counting()));
        System.out.println(countMap);
        List<IncDecItem> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getDate()) <= 0).collect(Collectors.toList());
        countMap = currentIncDecs.stream().collect(Collectors.groupingBy(e -> e.getMarket(), Collectors.counting()));
        System.out.println(countMap);
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getDate()) >= 0).collect(Collectors.toList());
        countMap = currentIncDecs.stream().collect(Collectors.groupingBy(e -> e.getMarket(), Collectors.counting()));
        System.out.println(countMap);
        currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    public List<IncDecItem> moveAndGetCommon(List<IncDecItem> listInc, List<IncDecItem> listDec) {
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

    public List<IncDecItem> moveAndGetCommon(List<IncDecItem> listInc, List<IncDecItem> listDec, boolean verify) {
        // and a new list for common items
        List<String> incIds = listInc.stream().map(IncDecItem::getId).collect(Collectors.toList());
        List<String> decIds = listDec.stream().map(IncDecItem::getId).collect(Collectors.toList());
        List<String> commonIds = new ArrayList<>(incIds);
        commonIds.retainAll(decIds);
        List<IncDecItem> common = listInc.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toList());
        common.addAll(listDec.stream().filter(m -> commonIds.contains(m.getId())).collect(Collectors.toList()));
        listInc.removeAll(common);
        listDec.removeAll(common);
        if (true) {
            List<IncDecItem> mergecommon = new ArrayList<>();
            for (String id : commonIds) {
                IncDecItem inc = common.stream().filter(item -> id.equals(item.getId()) && item.isIncrease()).findAny().orElse(null);
                IncDecItem dec = common.stream().filter(item -> id.equals(item.getId()) && !item.isIncrease()).findAny().orElse(null);
                IncDecItem mergeitem = new IncDecItem();
                mergeitem.setDate(inc.getDate());
                mergeitem.setDescription("Up: " + inc.getDescription() + " Down: " + dec.getDescription());
                mergeitem.setId(id);
                mergeitem.setIncrease(inc.getScore() > dec.getScore());
                mergeitem.setMarket(inc.getMarket());
                mergeitem.setName(inc.getName());
                mergeitem.setParameters(inc.getParameters());
                mergeitem.setScore(mergeitem.isIncrease() ? inc.getScore() - dec.getScore() : dec.getScore() - inc.getScore());
                mergecommon.add(mergeitem);
            }
            common = mergecommon;
        }
        return common;
    }

    public List<IncDecItem> moveAndGetCommon2(List<IncDecItem> listInc, List<IncDecItem> listDec, boolean verify) {
        // and a new list for common items
        List<String> incIds = new ArrayList<>();
        for (IncDecItem item : listInc) {
            String id = item.getId() + item.getDate().toString();
            incIds.add(id);
        }
        List<String> decIds = new ArrayList<>();
        for (IncDecItem item : listDec) {
            String id = item.getId() + item.getDate().toString();
            decIds.add(id);
        }
        List<String> commonIds = new ArrayList<>(incIds);
        commonIds.retainAll(decIds);
        List<IncDecItem> common = listInc.stream().filter(m -> commonIds.contains(m.getId() + m.getDate().toString())).collect(Collectors.toList());
        common.addAll(listDec.stream().filter(m -> commonIds.contains(m.getId() + m.getDate().toString())).collect(Collectors.toList()));
        listInc.removeAll(common);
        listDec.removeAll(common);
        if (true) {
            List<IncDecItem> mergecommon = new ArrayList<>();
            for (String id : commonIds) {
                IncDecItem inc = common.stream().filter(item -> id.equals(item.getId() + item.getDate().toString()) && item.isIncrease()).findAny().orElse(null);
                IncDecItem dec = common.stream().filter(item -> id.equals(item.getId() + item.getDate().toString()) && !item.isIncrease()).findAny().orElse(null);
                IncDecItem mergeitem = new IncDecItem();
                mergeitem.setDate(inc.getDate());
                mergeitem.setDescription("Up: " + inc.getDescription() + " Down: " + dec.getDescription());
                mergeitem.setId(inc.getId());
                mergeitem.setIncrease(inc.getScore() > dec.getScore());
                mergeitem.setMarket(inc.getMarket());
                mergeitem.setName(inc.getName());
                mergeitem.setParameters(inc.getParameters());
                mergeitem.setScore(mergeitem.isIncrease() ? inc.getScore() - dec.getScore() : dec.getScore() - inc.getScore());
                mergecommon.add(mergeitem);
            }
            common = mergecommon;
        }
        return common;
    }

    public Map<String, Object> loadConfig(ControlService srv, ComponentInput componentInput, Market market, String marketName, String action, String component, boolean evolve, Boolean buy, String subcomponent, MarketActionData marketaction, Parameters parameters) throws Exception {
        LocalDate date = componentInput.getEnddate();
        LocalDate olddate = date.minusDays(2 * marketaction.getTime(market));
        List<ConfigItem> filterConfigs = new ArrayList<>();
        List<ConfigItem> configs = IclijDbDao.getAllConfigs(market.getConfig().getMarket(), action, component, subcomponent, JsonUtil.convert(parameters), olddate, date);
        for (ConfigItem config : configs) {
            if (buy != null && config.getBuy() != null && buy != config.getBuy()) {
                continue;
            }
            filterConfigs.add(config);
        }
        Collections.sort(filterConfigs, (o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
        Map<String, Class> type = srv.conf.getType();
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

    public List<MemoryItem> filterKeepRecent(List<MemoryItem> marketMemory, LocalDate date, int days) {
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
        List<MemoryItem> currentList = marketMemory.stream().filter(m -> olddate.compareTo(m.getFuturedate()) <= 0).collect(Collectors.toList());
        currentList = currentList.stream().filter(m -> date.compareTo(m.getFuturedate()) >= 0).collect(Collectors.toList());
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

}
