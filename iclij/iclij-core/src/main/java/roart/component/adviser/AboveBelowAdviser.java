package roart.component.adviser;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.constants.IclijConstants;
import roart.db.IclijDbDao;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.util.MiscUtil;

public class AboveBelowAdviser extends Adviser {

    private List<IncDecItem> allIncDecs = null;

    private List<MemoryItem> allMemories = null;

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
        if (simulateParam.getAllMemories() != null) {
            allMemories = simulateParam.getAllMemories();
        } else {
            getAllMemories();
        }
    }
    
    private void getAllIncDecs() {
        try {
            //allIncDecs = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), investStart, investEnd, null);
            allIncDecs = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), null, null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    private void getAllMemories() {
        try {
            allMemories = IclijDbDao.getAllMemories(market.getConfig().getMarket(), IclijConstants.IMPROVEABOVEBELOW, PipelineConstants.ABOVEBELOW, null, null, investStart, investEnd);
            // also filter on params
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }        
    }
    
    //@Override
    public List<IncDecItem> getIncs2(String aParameter, int buytop,
            LocalDate date, int indexOffset, List<String> stockDates, List<String> excludes) {
        List<IncDecItem> incdecs = new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime(), false);
        incdecs = incdecs.stream().filter(e -> !excludes.contains(e.getId())).collect(Collectors.toList());
        List<IncDecItem> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              

        List<IncDecItem> myincdecs = incdecsP;
        List<IncDecItem> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toList());
        List<IncDecItem> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toList());
        List<IncDecItem> mylocals = new MiscUtil().getIncDecLocals(myincdecs);

        myincs = new MiscUtil().mergeList(myincs, true);
        mydecs = new MiscUtil().mergeList(mydecs, true);
        List<IncDecItem> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);

        Comparator<IncDecItem> incDecComparator = (IncDecItem comp1, IncDecItem comp2) -> comp2.getScore().compareTo(comp1.getScore());

        myincs.sort(incDecComparator);   
        mydecs.sort(incDecComparator);   

        //int subListSize = Math.min(buytop, myincs.size());
        //myincs = myincs.subList(0, subListSize);
        incdecs = null;
        incdecsP = null;
        myincdecs = null;
        mydecs = null;
        myincdec = null;
        return myincs;
    }
    
    @Override
    public List<String> getIncs(String aParameter, int buytop,
            LocalDate date, int indexOffset, List<String> stockDates, List<String> excludes) {
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

    @Override
    public double getReliability(LocalDate date, Boolean above) {
        int findTimes = simulateConfig.getConfidenceFindTimes();
        int verificationdays = param.getInput().getConfig().verificationDays();
        int findTime = market.getConfig().getFindtime();
        LocalDate oldDate = date.minusDays(verificationdays + findTime);
        List<MemoryItem> memories = new MiscUtil().getCurrentMemories(oldDate, allMemories, market, findTime * findTimes, false);
        int abovepositives = 0;
        int abovesize = 0;
        int belowpositives = 0;
        int belowsize = 0;
        for (MemoryItem memory : memories) {
            if (memory.getAbovepositives() == null) {
                int jj = 0;
            }
            abovepositives += memory.getAbovepositives();
            abovesize += memory.getAbovesize();
            belowpositives += memory.getBelowpositives();
            belowsize += memory.getBelowsize();
        }
        double positives = 0;
        int size = 0;
        if (above == null || above == true) {
            positives += abovepositives;
            size += abovesize;
        }
        if (above == null || above == false) {
            positives += belowpositives;
            size += belowsize;
        }
        if (size > 0) {
            return positives / size;
        }
        return 0;
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
            valueMap.put(i, valueList);
            int indexOffset = size - 1 - i;
            String dateString = stockDates.get(i);
            LocalDate date = null;
            try {
                date = new TimeUtil().convertDate(dateString);
            } catch (ParseException e) {
                log.info(Constants.EXCEPTION, e);
            }
            List<IncDecItem> incdecs = new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime(), false);
            incdecs = incdecs.stream().filter(e -> !excludes.contains(e.getId())).collect(Collectors.toList());
            List<IncDecItem> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              

            List<IncDecItem> myincdecs = incdecsP;
            List<IncDecItem> myincs = myincdecs.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> mydecs = myincdecs.stream().filter(m2 -> !m2.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> mylocals = new MiscUtil().getIncDecLocals(myincdecs);

            myincs = new MiscUtil().mergeList(myincs, true);
            mydecs = new MiscUtil().mergeList(mydecs, true);
            List<IncDecItem> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);

            Comparator<IncDecItem> incDecComparator = (IncDecItem comp1, IncDecItem comp2) -> comp2.getScore().compareTo(comp1.getScore());

            myincs.sort(incDecComparator);   
            mydecs.sort(incDecComparator);   

            //int subListSize = Math.min(buytop, myincs.size());
            //myincs = myincs.subList(0, subListSize);
            incdecs = null;
            incdecsP = null;
            myincdecs = null;
            mydecs = null;
            myincdec = null;
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
        if (valueMap != null) {
            return;
        }
        valueMap = getValues(aParameter, stockDates, new ArrayList<>(), firstidx, lastidx);                
        MyCache.getInstance().put(key, valueMap);
    }
}
