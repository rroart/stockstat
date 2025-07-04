package roart.iclij.component.adviser;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.constants.Constants;
import roart.common.model.AboveBelowDTO;
import roart.common.model.IncDecDTO;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.constants.IclijConstants;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.service.util.MiscUtil;

public class AboveBelowAdviser extends Adviser {

    private List<IncDecDTO> allIncDecs = null;

    //private List<MemoryDTO> allMemories = null;

    private Map<Integer, List<IncDecDTO>> valueMap;

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
            allIncDecs = param.getService().getIo().getIdbDao().getAllIncDecs(market.getConfig().getMarket(), null, null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    @Override
    public List<String> getIncs(String aParameter, int buytop,
            int indexOffset, List<String> stockDates, List<String> excludes) {
        this.aParameter = aParameter;
        int idx = stockDates.size() - 1 - indexOffset;
        List<IncDecDTO> incdecs = valueMap.get(idx);
        List<IncDecDTO> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);
        List<String> list = incdecsP.stream().map(IncDecDTO::getId).collect(Collectors.toList());
        return list;
    }
    
    @Override
    public List<String> getParameters() {
        return new MiscUtil().getParameters(allIncDecs);
    }

    public Map<Integer, List<IncDecDTO>> getValues(String aParameter,
                                                   List<String> stockDates, List<String> excludes, int firstidx, int lastidx) {
        Map<Integer, List<IncDecDTO>> valueMap = new HashMap<>();
        int size = stockDates.size();
        int start = size - 1 - firstidx;
        int end = size - 1 - lastidx;
        //List<Pair<String, Double>> valueList = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            List<IncDecDTO> valueList = new ArrayList<>();
            //valueMap.put(i, valueList);
            int indexOffset = size - 1 - i;
            String dateString = stockDates.get(i);
            LocalDate date = null;
            try {
                date = new TimeUtil().convertDate(dateString);
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<IncDecDTO> incdecs = new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime(), false);
            incdecs = incdecs.stream().filter(e -> !excludes.contains(e.getId())).collect(Collectors.toList());
            List<IncDecDTO> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);

            List<IncDecDTO> myincdecs = incdecsP;
            Set<IncDecDTO> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toSet());
            Set<IncDecDTO> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toSet());
            List<IncDecDTO> mylocals = new MiscUtil().getIncDecLocals(myincdecs);
            if (simulateConfig.getAbovebelow()) {
                Set<IncDecDTO> mys = new HashSet<>();
                Date mydate = TimeUtil.convertDate3(date.minusDays(market.getConfig().getFindtime()));
                Date olddate = TimeUtil.convertDate3(date.minusDays(market.getConfig().getFindtime()));
                List<String>[] list = p(market.getConfig().getMarket(), olddate, mydate);
                List<String> components = list[0];
                List<String> subcomponents = list[1];
                if (!components.isEmpty() || !subcomponents.isEmpty()) {                    
                    for (IncDecDTO item : myincs) {
                        if (components.contains(item.getComponent()) || subcomponents.contains(item.getSubcomponent())) {
                            mys.add(item);
                        }
                    }
                    myincs = mys;
                }
            }
            myincs = new MiscUtil().mergeList(myincs, true);
            mydecs = new MiscUtil().mergeList(mydecs, true);
            Set<IncDecDTO> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);

            Comparator<IncDecDTO> incDecComparator = (IncDecDTO comp1, IncDecDTO comp2) -> comp2.getScore().compareTo(comp1.getScore());

            List<IncDecDTO> myincl = new ArrayList<>(myincs);
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
        valueMap = (Map<Integer, List<IncDecDTO>>) MyCache.getInstance().get(key);
        Map<Integer, List<IncDecDTO>> newValueMap = null;
        if (valueMap == null || VERIFYCACHE) {
            long time0 = System.currentTimeMillis();
            newValueMap = getValues(aParameter, stockDates, new ArrayList<>(), firstidx, lastidx);                
            log.info("time millis {}", System.currentTimeMillis() - time0);
        }
        if (VERIFYCACHE && valueMap != null) {
            for (Entry<Integer, List<IncDecDTO>> entry : newValueMap.entrySet()) {
                int key2 = entry.getKey();
                List<IncDecDTO> v2 = entry.getValue();
                List<IncDecDTO> v = valueMap.get(key2);
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
    
    public List<String>[] p(String market, Date startdate, Date enddate) {
        List<AboveBelowDTO> list = null;
        try {
            list = param.getService().getIo().getIdbDao().getAllAboveBelow(market, startdate, enddate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<String> retComponents = new ArrayList<>(); 
        List<String> retSubcomponents = new ArrayList<>(); 
        for (AboveBelowDTO item : list) {
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
