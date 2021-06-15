package roart.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import java.util.OptionalDouble;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.util.ArraysUtil;
import roart.common.util.JsonUtil;
import roart.common.util.MathUtil;
import roart.common.util.TimeUtil;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapChromosome;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.SimulateFilter;
import roart.iclij.model.SimDataItem;
import roart.iclij.util.MiscUtil;
import roart.simulate.model.SimulateStock;
import roart.simulate.model.StockHistory;
import roart.simulate.util.SimUtil;
import roart.constants.SimConstants;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.factory.InmemoryFactory;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class Sim {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public void method(String param, String string, boolean b) {
        param = getParam(param);
        Map<String, Object> myMap = convert(param);
        if (myMap.isEmpty()) {
            return;
        }
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
        if (myList.size() > 0) {
            //for ()
            Map<String, String> shortMap;
            if (!b) {
                shortMap = getAutoShortMap();
            } else {
                shortMap = getShortMap();
            }
            List<SimulateFilter[]> list = null;
            IclijConfig instance = IclijXMLConfig.getConfigInstance();
            try {
                list = IclijXMLConfig.getSimulate(instance);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            Pair<Double, AbstractChromosome> winnerPair = myList.get(0);
            IclijConfigMapChromosome winnerChromosome = (IclijConfigMapChromosome) winnerPair.getValue();
            String filterString = getFilter(winnerChromosome);
            int adviser = getAdviser(winnerChromosome);
            if (adviser == -1) {
                adviser = 0;
            }
            SimulateFilter filter;
            if (!b) {
                filter = getFilter();
            } else {
                filter = getFilter(adviser);
            }
            {
                SimulateFilter[] listoverrides = null;
                Object o = myMap.get(SimConstants.FILTER);
                SimulateFilter[] listoverrides2 = JsonUtil.convert((String)o, SimulateFilter[].class);
                SimulateFilter[] listoverride = listoverrides2;
                if (listoverride != null) {
                for (int i = 0; i < listoverride.length; i++) {
                    SimulateFilter afilter = list.get(0)[i];
                    SimulateFilter otherfilter = listoverride[i];
                    afilter.merge(otherfilter);
                }
                }
            }
            if (!list.isEmpty() && b) {
                SimulateFilter[] filters = list.get(0);
                filter = filters[adviser];
            }
            if (!list.isEmpty() && !b) {
                Set<String> set = filter.getPrintconfig();
                filter = list.get(0)[0];
                filter.setPrintconfig(set);
            }
            List<String> output = new ArrayList<>();

            //output.add("Sim " + simtext);
            //output.add("File: " + id);
            // config compare always true
            if (filter.getPopulationabove() > 0) {
                getPopulationAbove(myList, filter, output);
                output.add("");
            }
            Map<Double, List<AbstractChromosome>> chromosomeMap = groupCommon(myList, output);
            Double[] commonScore = getCommon(chromosomeMap, output, filter.isAllabove());
            Map<Double, List<AbstractChromosome>> minChromosomeMap = new HashMap<>(chromosomeMap);
            Set<Double> scores = new HashSet<>();
            Set<Double> minScores = new HashSet<>();
            List<Pair<Double, String>> summaries = new ArrayList<>();
            for (Entry<Double, List<AbstractChromosome>> entry : chromosomeMap.entrySet()) {
                List<Summary> summary = new ArrayList<>();
                Double score = entry.getKey();
                if (score < 1) {
                    break;
                }
                //output.add("Score " + score);
                List<AbstractChromosome> chromosomes = entry.getValue();
                IclijConfigMapChromosome chromosome = (IclijConfigMapChromosome) chromosomes.get(0);
                Map<String, Object> resultMap = chromosome.getResultMap();
                Double keyScore = score;
                if (filter.isAllabove()) {
                    keyScore = getAllAbove(resultMap, output, summary);
                }
                if (filter.getStable() > 0) {
                    getStable(resultMap, filter, output, summary);
                } else {
                    summary.add(new Summary(true, "Stable"));                
                }
                if (filter.getCorrelation() > 0) {
                    getCorrelation(resultMap, filter, output, summary);
                } else {
                    summary.add(new Summary(true, "Correlation"));                
                }
               if (filter.getLucky() > 0) {
                    getLucky(resultMap, filter, output, summary);
                } else {
                    summary.add(new Summary(true, "Lucky"));                
                }
                if (filter.getShortrun() > 0) {
                    getShortRun(resultMap, filter, output, summary);
                } else {
                    summary.add(new Summary(true, "ShortRun"));                
                }
                if (true /*findcluster equal value*/) {
                    // use config compare
                }
                String mysummary = "";
                boolean success = true;
                for (Summary aSummary : summary) {
                    success &= aSummary.success;
                    mysummary += ", ";
                    mysummary += aSummary.text + " : " + aSummary.success + " ";
                }
                Map<String, Object> map = chromosome.getMap();
                Map<String, Object> shorts = new HashMap<>();
                for (String key : filter.getPrintconfig()) {
                    shorts.put(shortMap.get(key), map.get(key));
                }
                mysummary = "Summary : " + success + " Score " + MathUtil.round(score, 2) + " " + mysummary + " Config : " + shorts;
                output.add(mysummary);
                output.add("");
                if (success) {
                    summaries.add(new ImmutablePair<>(keyScore, mysummary));
                    scores.add(score);
                    if (score.doubleValue() != keyScore.doubleValue()) {
                        minScores.add(keyScore);
                        minChromosomeMap.put(keyScore, chromosomes);
                    }
                }
            }
            Double maxScore = summaries.stream().mapToDouble(Pair::getKey).max().orElse(0);
            List<Pair<Double, String>> maxSummaries = summaries.stream().filter(e -> e.getKey().equals(maxScore)).collect(Collectors.toList());
            for (Pair<Double, String> aSummary : maxSummaries) {
                output.add("Max " + MathUtil.round(aSummary.getKey(), 2) + " " + aSummary.getValue());
            }
            String simtext = (String) myMap.get(EvolveConstants.TITLETEXT); // getSimtext(winnerChromosome);
            print(string + " " + simtext, "File " + id, output);
            
            //Map<String, Object> resultMap = winnerChromosome.getResultMap();
            String[] parts = simtext.split(" ");
            String market = parts[1];
            String dates = parts[3];
            String startdateStr = dates.substring(0, 10);
            String enddateStr = dates.substring(11);
            LocalDate startdate = null; 
            try {
                startdateStr = startdateStr.replace('-', '.');
                startdate = TimeUtil.convertDate(startdateStr);
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }
            LocalDate enddate = null;
            if ("end".equals(enddateStr)) {
                if (true) {
                    return;
                }
                enddate = LocalDate.now();
                enddate = dateRound(enddate);
            } else {
                try {
                    enddateStr = enddateStr.replace('-', '.');
                    enddate = TimeUtil.convertDate(enddateStr);
                } catch (ParseException e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
            if (scores.isEmpty()) {
                return;
            }
            if (filter.isUseclusters() && b && commonScore[0] != null) {
                IclijConfigMapChromosome chromosome = (IclijConfigMapChromosome) chromosomeMap.get(commonScore[0]).get(0);
                SimDataItem data = new SimDataItem();
                data.setRecord(LocalDate.now());
                data.setScore(commonScore[1]);
                data.setMarket(market);
                //data.setAdviser(adviser);
                data.setStartdate(startdate);
                data.setEnddate(enddate);
                data.setFilter(JsonUtil.convert(filter));
                data.setConfig(JsonUtil.convert(chromosome.getMap()));
                try {
                    data.save();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
            Double max = Collections.max(scores);
            if (minScores.isEmpty()) {
                minScores = scores;
            }
            Double min = Collections.max(minScores);
            List<AbstractChromosome> chromosomes = minChromosomeMap.get(min);            
            IclijConfigMapChromosome chromosome = (IclijConfigMapChromosome) chromosomes.get(0);
            //String adviser = parts[4];
            if (!b) {
                return;
            }
            SimDataItem data = new SimDataItem();
            data.setRecord(LocalDate.now());
            data.setScore(min);
            data.setMarket(market);
            //data.setAdviser(adviser);
            data.setStartdate(startdate);
            data.setEnddate(enddate);
            data.setFilter(JsonUtil.convert(filter));
            data.setConfig(JsonUtil.convert(chromosome.getMap()));
            try {
                data.save();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
    }

    public void method2(String param) {
        param = getParam(param);
        Map<String, Object> myMap = convert(param);
        if (myMap.isEmpty()) {
            return;
        }
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
        if (myList.size() > 0) {
            List<String> output = new ArrayList<>();

            Map<Double, List<AbstractChromosome>> chromosomeMap = groupCommon(myList, output);
            getCommon(chromosomeMap, output, false);
            Map<Double, List<AbstractChromosome>> minChromosomeMap = new HashMap<>(chromosomeMap);
            Set<Double> scores = new HashSet<>();
            Set<Double> minScores = new HashSet<>();
            List<Pair<Double, String>> summaries = new ArrayList<>();
            for (Entry<Double, List<AbstractChromosome>> entry : chromosomeMap.entrySet()) {
                List<Summary> summary = new ArrayList<>();
                Double score = entry.getKey();
                List<AbstractChromosome> chromosomes = entry.getValue();
                Double keyScore = score;
                String mysummary = "";
                boolean success = true;
                for (Summary aSummary : summary) {
                    success &= aSummary.success;
                    mysummary += ", ";
                    mysummary += aSummary.text + " : " + aSummary.success + " ";
                }
                mysummary = "Summary : " + success + " Score " + MathUtil.round(score, 2) + " " + mysummary;
                output.add(mysummary);
                output.add("");
                if (success) {
                    summaries.add(new ImmutablePair<>(keyScore, mysummary));
                    scores.add(score);
                    if (score.doubleValue() != keyScore.doubleValue()) {
                        minScores.add(keyScore);
                        minChromosomeMap.put(keyScore, chromosomes);
                    }
                }
            }
            Double maxScore = summaries.stream().mapToDouble(Pair::getKey).max().orElse(0);
            List<Pair<Double, String>> maxSummaries = summaries.stream().filter(e -> e.getKey().equals(maxScore)).collect(Collectors.toList());
            for (Pair<Double, String> aSummary : maxSummaries) {
                output.add("Max " + MathUtil.round(aSummary.getKey(), 2) + " " + aSummary.getValue());
            }
            String simtext = (String) myMap.get(EvolveConstants.TITLETEXT); // getSimtext(winnerChromosome);
            print("simauto " + simtext, "File " + id, output);
        }
    }

    private LocalDate dateRound(LocalDate enddate) {
        if (enddate.getDayOfMonth() > 1) {
            enddate = enddate.withDayOfMonth(1);
            if (enddate.getMonthValue() == 12) {
                enddate = enddate.withMonth(1);
                enddate = enddate.withYear(enddate.getYear() + 1);
            } else {
                enddate = enddate.withMonth(enddate.getMonthValue() + 1);                
            }
        }
        return enddate;
    }

    private SimulateFilter getFilter(int adviser) {
        SimulateFilter filter;
        Set<String> generalSimConfig = new HashSet<>();
        generalSimConfig.add(IclijConfigConstants.SIMULATEINVESTINTERVAL);
        generalSimConfig.add(IclijConfigConstants.SIMULATEINVESTINDICATORREVERSE);
        Set<String> periodSimConfig = new HashSet<>();
        periodSimConfig.add(IclijConfigConstants.SIMULATEINVESTINTERVAL);
        periodSimConfig.add(IclijConfigConstants.SIMULATEINVESTPERIOD);
        periodSimConfig.add(IclijConfigConstants.SIMULATEINVESTINDICATORREVERSE);
        Set<String> timeSimConfig = new HashSet<>();
        timeSimConfig.add(IclijConfigConstants.SIMULATEINVESTINTERVAL);
        timeSimConfig.add(IclijConfigConstants.SIMULATEINVESTDAY);
        timeSimConfig.add(IclijConfigConstants.SIMULATEINVESTINDICATORREVERSE);
        SimulateFilter[] filters = new SimulateFilter[10];
        filter = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, generalSimConfig);
        filters[0] = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, generalSimConfig);
        filters[1] = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, periodSimConfig);
        filters[2] = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, generalSimConfig);
        filters[3] = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, generalSimConfig);
        filters[4] = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, generalSimConfig);
        filters[5] = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, generalSimConfig);
        filters[6] = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, timeSimConfig);
        filters[7] = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, generalSimConfig);
        filters[8] = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, generalSimConfig);
        filters[9] = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, generalSimConfig);
        filter = filters[adviser];
        return filter;
    }

    private SimulateFilter getFilter() {
        SimulateFilter filter;
        Set<String> generalSimConfig = new HashSet<>();
        generalSimConfig.add(IclijConfigConstants.AUTOSIMULATEINVESTINTERVAL);
        generalSimConfig.add(IclijConfigConstants.AUTOSIMULATEINVESTPERIOD);
        filter = new SimulateFilter(5, 0.9, 0.5, 0.8, true, 16, true, generalSimConfig);
        return filter;
    }

    private Map<String, String> getShortMap() {
        Map<String, String> shortMap = new HashMap<>();
        shortMap.put(IclijConfigConstants.SIMULATEINVESTINTERVAL, "i");
        shortMap.put(IclijConfigConstants.SIMULATEINVESTDAY, "d");
        shortMap.put(IclijConfigConstants.SIMULATEINVESTPERIOD, "p");
        shortMap.put(IclijConfigConstants.SIMULATEINVESTINDICATORREVERSE, "ir");
        return shortMap;
    }

    private Map<String, String> getAutoShortMap() {
        Map<String, String> shortMap = new HashMap<>();
        shortMap.put(IclijConfigConstants.AUTOSIMULATEINVESTINTERVAL, "i");
        shortMap.put(IclijConfigConstants.AUTOSIMULATEINVESTPERIOD, "p");
        return shortMap;
    }

    private String getParam(String param) {
        InmemoryMessage message = JsonUtil.convert(param, InmemoryMessage.class);
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        Inmemory inmemory = InmemoryFactory.get(instance.getInmemoryServer(), instance.getInmemoryHazelcast(), instance.getInmemoryRedis());
        String newparam = inmemory.read(message);
        inmemory.delete(message);
        return newparam;
    }

    private int getAdviser(IclijConfigMapChromosome chromosome) {
        Map<String, Object> map = chromosome.getMap();
        if (!map.containsKey(IclijConfigConstants.SIMULATEINVESTADVISER)) {
            return -1;
        }
        int adviser = (int) map.get(IclijConfigConstants.SIMULATEINVESTADVISER);
        return adviser;
    }

    private String getFilter(IclijConfigMapChromosome chromosome) {
        Map<String, Object> map = chromosome.getMap();
        String adviser = (String) map.get(IclijConfigConstants.SIMULATEINVESTFILTERS);
        return adviser;
    }

    private String getSimtext(IclijConfigMapChromosome chromosome) {
        Map<String, Object> resultMap = chromosome.getResultMap();
        Map<String, Object> aMap = (Map<String, Object>) resultMap.get("0");
        String simtext = (String) aMap.get(EvolveConstants.SIMTEXT);
        return simtext;
    }

    private void getShortRun(Map<String, Object> resultMap, SimulateFilter filter, List<String> output, List<Summary> summary) {
        boolean notshort = true;
        Map<String, Object> aMap = (Map<String, Object>) resultMap.get("0");
        List<StockHistory> history = (List<StockHistory>) aMap.get(SimConstants.HISTORY);
        if (history.size() < filter.getShortrun()) {
            notshort = false;
        }
        //output.add("");
        summary.add(new Summary(notshort, "ShortRun"));
    }

    private void getLucky(Map<String, Object> resultMap, SimulateFilter filter, List<String> output, List<Summary> summary) {
        boolean notlucky = true;
        for (Entry<String, Object> entry : resultMap.entrySet()) {
            Map<String, Object> aMap = (Map<String, Object>) entry.getValue();
            List<StockHistory> history = (List<StockHistory>) aMap.get(SimConstants.HISTORY);
            if (history.isEmpty()) {
                continue;
            }
            StockHistory last = history.get(history.size() - 1);
            double total = last.getCapital().amount + last.getSum().amount - 1;
            if (total == 0.0) {
                continue;
            }
            List<Pair<String, Double>> list = SimUtil.getTradeStocks(aMap);
            int cnt = 3;
            for (Pair<String, Double> anEntry : list) {
                output.add(anEntry.getKey() + " " + MathUtil.round(anEntry.getValue(), 2) + " " + MathUtil.round(anEntry.getValue() / total, 2));
                cnt--;
                if (cnt <= 0) {
                    break;
                }
            }
            double max = 0;
            if (!list.isEmpty()) {
                max = list.get(0).getValue();
            }
            if (max / total > filter.getLucky()) {
                notlucky = false;
            }
            output.add("Lucky " + MathUtil.round(max, 2) + " " + MathUtil.round(total, 2) + " " + MathUtil.round(max / total, 2));
        }
        summary.add(new Summary(notlucky, "Lucky"));
    }

    private void getStable(Map<String, Object> resultMap, SimulateFilter filter, List<String> output, List<Summary> summary) {
        boolean stable = true;
        for (Entry<String, Object> entry : resultMap.entrySet()) {
            Map<String, Object> aMap = (Map<String, Object>) entry.getValue();
            List<StockHistory> history = (List<StockHistory>) aMap.get(SimConstants.HISTORY);
            if (history.isEmpty()) {
                continue;
            }
            StockHistory last = history.get(history.size() - 1);
            double lasttotal = last.getCapital().amount + last.getSum().amount;
            List<Double> values = new ArrayList<>();
            StockHistory firstHistory = history.get(0);
            double prevtotal = firstHistory.getCapital().amount + firstHistory.getSum().amount;
            for (StockHistory aHistory : history) {
                double total = aHistory.getCapital().amount + aHistory.getSum().amount;
                values.add(total - prevtotal);
                prevtotal = total;
            }
            Collections.sort(values);
            Collections.reverse(values);
            double total = 0;
            int count = 1 + values.size() / 10;
            for (int i = 0; i < count; i++) {
                total += values.get(i);
            }
            if (total / lasttotal > filter.getStable()) {
                stable = false;
                //break;
            }
            output.add("Stable " + MathUtil.round(total, 2) + " " + MathUtil.round(lasttotal, 2) + " " + MathUtil.round(total / lasttotal, 2));
        }
        summary.add(new Summary(stable, "Stable"));
    }

    private void getCorrelation(Map<String, Object> resultMap, SimulateFilter filter, List<String> output, List<Summary> summary) {
        boolean correlation = true;
        for (Entry<String, Object> entry : resultMap.entrySet()) {
            Map<String, Object> aMap = (Map<String, Object>) entry.getValue();
            List<Double> capitalList = (List<Double>) aMap.get(SimConstants.PLOTCAPITAL);
            if (capitalList.isEmpty()) {
                continue;
            }
            Double[] capArray = capitalList.toArray(new Double[0]);
            double[] cap = ArraysUtil.convert(capArray);
            double[] geom = MathUtil.getGeoSeq(cap);
            SpearmansCorrelation sc = new SpearmansCorrelation();
            double sp = sc.correlation(cap, geom);
            KendallsCorrelation kc = new KendallsCorrelation();
            double ke = kc.correlation(cap, geom);
            PearsonsCorrelation pc = new PearsonsCorrelation();
            double pe = pc.correlation(cap, geom);
            
            double average = (sp + ke + pe) / 3;
            if (average > filter.getStable()) {
                correlation = false;
                //break;
            }
            output.add("Correlation " + MathUtil.round(sp, 2) + " " + MathUtil.round(ke, 2) + " " + MathUtil.round(pe, 2) + " " + MathUtil.round(average, 2));
        }
        summary.add(new Summary(correlation, "Correlation"));
    }

    private Double getAllAbove(Map<String, Object> resultMap, List<String> output, List<Summary> summary) {
        boolean above = true;
        List<Double> scores = new ArrayList<>();
        for (Entry<String, Object> entry : resultMap.entrySet()) {
            Map<String, Object> aMap = (Map<String, Object>) entry.getValue();
            double score = (double) aMap.get(SimConstants.SCORE);
            scores.add(score);
            if (score < 1) {
                above = false;
                //break;
            }
        }
        //output.add("Score " + Collections.min(scores));
        if (scores.isEmpty()) {
            return 0.0;
        }
        output.add("Allabove " + MathUtil.round(Collections.min(scores), 2) + " . " + MathUtil.round(Collections.max(scores), 2) + " " + above);
        summary.add(new Summary(above, "MinScore (" + MathUtil.round(Collections.min(scores), 2) + ")"));
        return Collections.min(scores);
    }

    private void getPopulationAbove(List<Pair<Double, AbstractChromosome>> myList, SimulateFilter filter,
            List<String> output) {
        int max = Math.min(filter.getPopulationabove(), myList.size());
        boolean above = true;
        for (int i = 0; i < max; i++) {
            Pair<Double, AbstractChromosome> aPair = myList.get(i);
            if (aPair.getLeft() < 1) {
                above = false;
                break;
            }
        }
        output.add("Populationabove " + max + " " + above);
        //summary.add(new Summary(above, "Populationabove " + max));
    }

    private Map<Double, List<AbstractChromosome>> groupCommon(List<Pair<Double, AbstractChromosome>> myList, List<String> output) {
        List<Pair<Double, List<AbstractChromosome>>> retlist = new ArrayList<>();
        Map<Double, List<AbstractChromosome>> chromosomeMap = new LinkedHashMap<>();
        for (Pair<Double, AbstractChromosome> aPair : myList) {
            new MiscUtil().listGetterAdder(chromosomeMap, aPair.getLeft(), aPair.getRight());
        }
        return chromosomeMap;
    }

    private Double[] getCommon(Map<Double, List<AbstractChromosome>> chromosomeMap, List<String> output, Boolean isAllabove) {
        Double[] commonScore = { null, null };
        for (Entry<Double, List<AbstractChromosome>> entry : chromosomeMap.entrySet()) {
            Double score = entry.getKey();
            List<AbstractChromosome> aList = entry.getValue();
            if (aList.size() == 1) {
                continue;
            }
            IclijConfigMapChromosome firstChromosome = (IclijConfigMapChromosome) aList.get(0);
            Map<String, Object> aMap = firstChromosome.getMap();
            for (int i = 1; i < aList.size(); i++) {
                IclijConfigMapChromosome anotherChromosome = (IclijConfigMapChromosome) aList.get(i);
                Map<String, Object> anotherMap = anotherChromosome.getMap();
                MapDifference<String, Object> diff = Maps.difference(aMap, anotherMap);
                aMap = diff.entriesInCommon();
            }
            Set<String> keys = aMap.keySet();
            List<Map<String, Object>> list = new ArrayList<>();
            for (AbstractChromosome chromosome : aList) {
                IclijConfigMapChromosome anotherChromosome = (IclijConfigMapChromosome) chromosome;
                Map<String, Object> anotherMap = new HashMap<>(anotherChromosome.getMap());
                anotherMap.keySet().removeAll(keys);
                list.add(anotherMap);
            }
            List<String> list2 = new ArrayList<>();
            list2 = new ArrayList<>(firstChromosome.getMap().keySet());
            list2.removeAll(keys);
            output.add("Common");
            output.add(entry.getKey() + " " + aList.size() + " " + aMap);
            output.add("" + list2);
            output.add("");
            for (AbstractChromosome aChromosome : aList) {
                IclijConfigMapChromosome anotherChromosome = (IclijConfigMapChromosome) aChromosome;
                Map<String, Object> resultMap = anotherChromosome.getResultMap();
                Double keyScore = score;
                if (isAllabove) {
                    keyScore = getAllAbove(resultMap, new ArrayList<String>(), new ArrayList<Summary>());
                    if (keyScore < 1) {
                        continue;
                    }
                    if (commonScore[0] == null) {
                        commonScore[0] = score;
                        commonScore[1] = keyScore;
                        break;
                    }
                }
            }
        }
        return commonScore;
    }

    private Map<String, Object> convert(String param) {
        Map<String, Object> map = new HashMap<>();
        List<Pair<Double, AbstractChromosome>> myList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Map<String, Object> res0 = null;
        try {
            //res0 = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, IclijConfigMapChromosome>>>>(){});
            res0 = mapper.readValue(param, new TypeReference<Map<String, Object>>(){});
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            log.error(Constants.EXCEPTION, e);
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            log.error(Constants.EXCEPTION, e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(Constants.EXCEPTION, e);
        }
        if (res0 == null) {
            return map;
        }
        for (Entry<String, Object> entry : res0.entrySet()) {
            //System.out.println(entry.getKey() + " " + entry.getValue().getClass().getName());
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value.getClass() == String.class) {
                //map.put(key, value);
            } else {
                value = mapper.convertValue(value, new TypeReference<List<LinkedHashMap<Double, IclijConfigMapChromosome>>>(){});
            }
            map.put(key, value);
        }
        String id = (String) map.get(EvolveConstants.ID);
        List<Map<Double, AbstractChromosome>> res = (List<Map<Double, AbstractChromosome>>) map.get(id);
        //List<Map> list = JsonUtil.convert(param, List.class);
        SimulateStock s;
        for (Map<Double, AbstractChromosome> aMap : res) {
            for (Entry<Double, AbstractChromosome> entry : aMap.entrySet()) {
                Double score = entry.getKey();
                AbstractChromosome chromosome = entry.getValue();
                Pair<Double, AbstractChromosome> pair = new ImmutablePair<>(score, chromosome);
                myList.add(pair);

                Map<String, Object> resultMap = new HashMap<>();
                Map map2 = chromosome.getResultMap();
                Map<String, Map> map3 = map2;
                for (Entry<String, Map> entry3 : map3.entrySet()) {
                    Map<String, Object> map4 = entry3.getValue();
                    for (Entry<String, Object> entry4 : map4.entrySet()) {
                        //System.out.println(entry4);
                    }
                    List alist = (List) map4.get(SimConstants.HISTORY);
                    List<StockHistory> history = mapper.convertValue(alist, new TypeReference<List<StockHistory>>(){});
                    List alist2 = (List) map4.get(SimConstants.STOCKHISTORY);

                    List<SimulateStock> stockhistory = mapper.convertValue(alist2, new TypeReference<List<SimulateStock>>(){});
                    int jj = 0;
                    Double ascore = (Double) map4.get(SimConstants.SCORE);
                    String startdate = (String) map4.get(SimConstants.STARTDATE);
                    String enddate = (String) map4.get(SimConstants.ENDDATE);
                    String simtext = (String) map4.get(EvolveConstants.SIMTEXT);
                    Map<String, Object> newMap = new HashMap<>();
                    newMap.put(SimConstants.HISTORY, history);
                    newMap.put(SimConstants.STOCKHISTORY, stockhistory);
                    newMap.put(SimConstants.SCORE, ascore);
                    newMap.put(SimConstants.STARTDATE, startdate);
                    newMap.put(SimConstants.ENDDATE, enddate);
                    newMap.put(EvolveConstants.SIMTEXT, simtext);
                    resultMap.put(entry3.getKey(), newMap);
                }
                chromosome.setResultMap(resultMap);
            }
        }
        //list.size();
        map.put(id, myList);
        return map;
    }
    
    public String print(String title, String subtitle, List<String> individuals) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(title + "\n\n");
            if (subtitle != null) {
                writer.write(subtitle + "\n\n");
            }
            for (String individual : individuals) {
                writer.write(individual + "\n");            
            }
            writer.write("\n");
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
        return path.getFileName().toString();
    }
    
    class Summary {
        public boolean success;
        public String text;
        
        public Summary(boolean success, String text) {
            this.success = success;
            this.text = text;
        }
    }

}
