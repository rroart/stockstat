package roart.iclij.evolution.fitness.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.model.action.MarketActionData;
import roart.common.constants.Constants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.IncDecDTO;
import roart.common.pipeline.data.PipelineData;
import roart.component.model.ComponentData;
import roart.component.util.IncDecUtil;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.WebData;
import roart.iclij.service.util.MarketUtil;
import roart.iclij.service.util.MiscUtil;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.model.io.IO;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.iclij.model.Parameters;

public class FitnessMarketFilterCommon {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public double fitnessCommon(MarketActionData action, ComponentData param, Market market, ProfitData profitdata, Boolean buy, List<String> stockDates, List<IncDecDTO> incdecs, Parameters parameters, String componentName, Map<String, Object> map) {
        WebData myData = new WebData();
        myData.setIncs(new ArrayList<>());
        myData.setDecs(new ArrayList<>());
        myData.setUpdateMap(new HashMap<>());
        myData.setMemoryDTOs(new ArrayList<>());
        myData.setUpdateMap2(new HashMap<>());
        myData.setTimingMap(new HashMap<>());
        Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(param.getService().getIclijConfig());
        List<IncDecDTO> myincdecs = new ArrayList<>(incdecs);
        PipelineData[] maps = param.getResultMaps();
        new MarketUtil().fillProfitdata(profitdata, myincdecs);
        new IncDecUtil().filterIncDecs(param, market, profitdata, maps, true, stockDates, inmemory);
        new IncDecUtil().filterIncDecs(param, market, profitdata, maps, false, stockDates, inmemory);
        Set<IncDecDTO> myincs = new HashSet<>(profitdata.getBuys().values());
        Set<IncDecDTO> mydecs = new HashSet<>(profitdata.getSells().values());
        myincs = new MiscUtil().mergeList(myincs, true);
        mydecs = new MiscUtil().mergeList(mydecs, true);
        Set<IncDecDTO> myincdec = new MiscUtil().moveAndGetCommon(myincs, mydecs, true);
        try {
            int verificationdays = param.getConfig().verificationDays();
            myData.setProfitData(profitdata);
        
            Memories listMap = new Memories(market);
            ProfitInputData inputdata = new ProfitInputData();
            listMap.method(myData.getMemoryDTOs(), param.getConfig());        
            profitdata.setInputdata(inputdata);
        
            short startoffset = new MarketUtil().getStartoffset(market);
            if (verificationdays > 0) {
                new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), myincs, mydecs, myincdec, startoffset, parameters.getThreshold(), stockDates, param.getCategoryValueMap());
            }
        } catch (Exception e3) {
            log.error(Constants.EXCEPTION, e3);
        }

        //myincs = new ArrayList<>(profitdata.getBuys().values());
        //mydecs = new ArrayList<>(profitdata.getSells().values());
        
        double incdecFitness = 0.0;
        try {
            incdecFitness = fitness(myincs, mydecs, myincdec, param.getConfig().getImproveAbovebelowFitnessMinimum(), buy);
            log.info("Fit #{} {} ", this.hashCode(), market.getFilter());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        // or rather verified incdec
        log.info("Fit {} {}", componentName, incdecFitness);
        //configSaves(param, getMap());
        param.getUpdateMap().putAll(map);
        return incdecFitness;
    }
    
    public double fitness(Collection<IncDecDTO> myincs, Collection<IncDecDTO> mydecs, Collection<IncDecDTO> myincdec, int minimum, Boolean buy) {
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
    
    public static Pair<Long, Integer> countsize(Collection<IncDecDTO> list) {
        List<Boolean> listBoolean = list.stream().map(IncDecDTO::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
        long count = listBoolean.stream().filter(i -> i).count();                            
        int size = listBoolean.size();
        return new ImmutablePair(count, size);

    }
    
}
