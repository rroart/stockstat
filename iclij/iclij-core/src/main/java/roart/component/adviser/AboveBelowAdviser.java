package roart.component.adviser;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.constants.IclijConstants;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.util.MiscUtil;

public class AboveBelowAdviser extends Adviser {

    private List<IncDecItem> allIncDecs = null;

    private List<MemoryItem> allMemories = null;

    public AboveBelowAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param) {
        super(market, investStart, investEnd, param);
        SimulateInvestData simulateParam = (SimulateInvestData) param;
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
            allIncDecs = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), investStart, investEnd, null);
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
    
    @Override
    public List<IncDecItem> getIncs(String aParameter, int buytop,
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

        int subListSize = Math.min(buytop, myincs.size());
        myincs = myincs.subList(0, subListSize);
        return myincs;
    }
    
    @Override
    public List<String> getParameters() {
        return new MiscUtil().getParameters(allIncDecs);
    }

    @Override
    public double getReliability(LocalDate date, Boolean above) {
        IclijConfig config = param.getInput().getConfig();
        int findTimes = config.getSimulateInvestConfidenceFindtimes();
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

}
