package roart.iclij.component.adviser;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.type.TypeReference;

import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.constants.IclijConstants;
import roart.db.IclijDbDao;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.model.AboveBelowItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.util.MiscUtil;

public class AboveBelowAdviser extends Adviser {

    private List<IncDecItem> allIncDecs = null;

    //private List<MemoryItem> allMemories = null;

    private Map<Integer, List<IncDecItem>> valueMap;

    private String aParameter;
    
    public AboveBelowAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        super(market, investStart, investEnd, param, simulateConfig);
        SimulateInvestData simulateParam;
        if (param instanceof SimulateInvestData) {
            simulateParam = (SimulateInvestData) param;
        } else {
            simulateParam = new SimulateInvestData(param);
        }
        if (simulateParam.getAllIncDecs() != null) {
            allIncDecs = simulateParam.getAllIncDecs();
        } else {
            getAllIncDecs();
        }
        /*
        if (simulateParam.getAllMemories() != null) {
            allMemories = simulateParam.getAllMemories();
        } else {
            getAllMemories();
        }
        */
    }
    
    private void getAllIncDecs() {
        try {
            //allIncDecs = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), investStart, investEnd, null);
            allIncDecs = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), null, null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    @Override
    public List<String> getIncs(String aParameter, int buytop,
            int indexOffset, List<String> stockDates, List<String> excludes) {
        this.aParameter = aParameter;
        int idx = stockDates.size() - 1 - indexOffset;
        List<IncDecItem> incdecs = valueMap.get(idx);
        List<IncDecItem> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              
        List<String> list = incdecsP.stream().map(IncDecItem::getId).collect(Collectors.toList());
        return list;
    }
    
    @Override
    public List<String> getParameters() {
        return new MiscUtil().getParameters(allIncDecs);
    }

    public Map<Integer, List<IncDecItem>> getValues(String aParameter,
            List<String> stockDates, List<String> excludes, int firstidx, int lastidx) {
        Map<Integer, List<IncDecItem>> valueMap = new HashMap<>();
        int size = stockDates.size();
        int start = size - 1 - firstidx;
        int end = size - 1 - lastidx;
        //List<Pair<String, Double>> valueList = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            List<IncDecItem> valueList = new ArrayList<>();
            //valueMap.put(i, valueList);
            int indexOffset = size - 1 - i;
            String dateString = stockDates.get(i);
            LocalDate date = null;
            try {
                date = new TimeUtil().convertDate(dateString);
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<IncDecItem> incdecs = new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime(), false);
            incdecs = incdecs.stream().filter(e -> !excludes.contains(e.getId())).collect(Collectors.toList());
            List<IncDecItem> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              

            List<IncDecItem> myincdecs = incdecsP;
            Set<IncDecItem> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toSet());
            Set<IncDecItem> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toSet());
            List<IncDecItem> mylocals = new MiscUtil().getIncDecLocals(myincdecs);
            if (simulateConfig.getAbovebelow()) {
                Set<IncDecItem> mys = new HashSet<>();
                LocalDate mydate = date.minusDays(market.getConfig().getFindtime());
                LocalDate olddate = mydate.minusDays(market.getConfig().getFindtime());
                List<String>[] list = p(market.getConfig().getMarket(), olddate, mydate);
                List<String> components = list[0];
                List<String> subcomponents = list[1];
                if (!components.isEmpty() || !subcomponents.isEmpty()) {                    
                    for (IncDecItem item : myincs) {
                        if (components.contains(item.getComponent()) || subcomponents.contains(item.getSubcomponent())) {
                            mys.add(item);
                        }
                    }
                    myincs = mys;
                }
            }
            myincs = new MiscUtil().mergeList(myincs, true);
            mydecs = new MiscUtil().mergeList(mydecs, true);
            Set<IncDecItem> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);

            Comparator<IncDecItem> incDecComparator = (IncDecItem comp1, IncDecItem comp2) -> comp2.getScore().compareTo(comp1.getScore());

            List<IncDecItem> myincl = new ArrayList<>(myincs);
            myincl.sort(incDecComparator);   
            valueMap.put(i, myincl);

        }
        return valueMap;
    }
    
    @Override
    public void getValueMap(List<String> stockDates, int firstidx, int lastidx,
            Map<String, List<List<Double>>> categoryValueMap) {
        int start = stockDates.size() - 1 - firstidx;
        int end = stockDates.size() - 1 - lastidx;
        String investStart = stockDates.get(start);
        String investEnd = stockDates.get(end);
        String key = CacheConstants.SIMULATEINVESTADVISER + market.getConfig().getMarket() + this.getClass().getName() + investStart + investEnd;
        valueMap = (Map<Integer, List<IncDecItem>>) MyCache.getInstance().get(key);
        Map<Integer, List<IncDecItem>> newValueMap = null;
        if (valueMap == null || VERIFYCACHE) {
            long time0 = System.currentTimeMillis();
            newValueMap = getValues(aParameter, stockDates, new ArrayList<>(), firstidx, lastidx);                
            log.info("time millis {}", System.currentTimeMillis() - time0);
        }
        if (VERIFYCACHE && valueMap != null) {
            for (Entry<Integer, List<IncDecItem>> entry : newValueMap.entrySet()) {
                int key2 = entry.getKey();
                List<IncDecItem> v2 = entry.getValue();
                List<IncDecItem> v = valueMap.get(key2);
                if (v2 != null && !v2.equals(v)) {
                    log.error("Difference with cache");
                }
            }
        }
        if (valueMap != null) {
            return;
        }
        valueMap = newValueMap;
        MyCache.getInstance().put(key, valueMap);
    }
    
    public List<String>[] p(String market, LocalDate startdate, LocalDate enddate) {
        List<AboveBelowItem> list = null;
        try {
            list = AboveBelowItem.getAll(market, startdate, enddate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<String> retComponents = new ArrayList<>(); 
        List<String> retSubcomponents = new ArrayList<>(); 
        for (AboveBelowItem item : list) {
            String components = item.getComponents();
            String[] componentList = JsonUtil.convert(components, String[].class);
            String subcomponents = item.getSubcomponents();
            String[] subcomponentList = JsonUtil.convert(subcomponents, String[].class);            
            retComponents.addAll(Arrays.asList(componentList));
            retSubcomponents.addAll(Arrays.asList(subcomponentList));
        }
        return new List[] { retComponents, retSubcomponents }; 
    }
    
}
