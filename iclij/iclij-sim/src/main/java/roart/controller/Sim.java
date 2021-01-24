package roart.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;

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
import roart.common.util.JsonUtil;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapChromosome;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.SimulateFilter;
import roart.iclij.util.MiscUtil;
import roart.simulate.SimulateStock;
import roart.simulate.StockHistory;
import roart.constants.SimConstants;

public class Sim {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public void method(String param) {
        Map<String, List<Pair<Double, AbstractChromosome>>> myMap = convert(param);
        String id = myMap.keySet().iterator().next();
        List<Pair<Double, AbstractChromosome>> myList = myMap.get(id);
        if (myList.size() > 0) {
            //for ()
            Pair<Double, AbstractChromosome> winnerPair = myList.get(0);
            IclijConfigMapChromosome winnerChromosome = (IclijConfigMapChromosome) winnerPair.getValue();
            Map<String, Object> winnerMap = winnerChromosome.getMap();
            int adviser = (int) winnerMap.get(IclijConfigConstants.SIMULATEINVESTADVISER);
            Map<String, Object> winnerResultMap = winnerChromosome.getResultMap();
            Map<String, Object> aMap = (Map<String, Object>) winnerResultMap.get("0");
            String simtext = (String) aMap.get(EvolveConstants.SIMTEXT);
            //Map<String, Object> winnerMap = (Map<String, Object>) winnerResultMap.get("0");
            SimulateFilter[] filters = new SimulateFilter[10];
            SimulateFilter filter = new SimulateFilter(5, 0.2, true, true, 16);
            filters[0] = new SimulateFilter(5, 0.2, true, true, 16);
            filters[1] = new SimulateFilter(5, 0.2, true, true, 16);
            filters[2] = new SimulateFilter(5, 0.2, true, true, 16);
            filters[3] = new SimulateFilter(5, 0.2, true, true, 16);
            filters[4] = new SimulateFilter(5, 0.2, true, true, 16);
            filters[5] = new SimulateFilter(5, 0.2, true, true, 16);
            filters[6] = new SimulateFilter(5, 0.2, true, true, 16);
            filters[7] = new SimulateFilter(5, 0.2, true, true, 16);
            filters[8] = new SimulateFilter(5, 0.2, true, true, 16);
            filters[9] = new SimulateFilter(5, 0.2, true, true, 16);
            filter = filters[adviser];
            List<String> output = new ArrayList<>();
            List<Summary> summary = new ArrayList<>();
            
            output.add("Sim " + simtext);
            output.add("File: " + id);
            // config compare always true
            findCommon(myList, output);
            if (filter.getPopulationabove() > 0) {
                getPopulationAbove(myList, filter, output, summary);
            }
            if (filter.isAllabove()) {
                getAllAbove(winnerResultMap, output, summary);
            }
            if (filter.isStable()) {
                getStable(winnerResultMap, output, summary);
            }
            if (filter.getLucky() > 0) {
                getLucky(winnerResultMap, filter, output, summary);
                
            }
            if (filter.getShortrun() > 0) {
                getShortRun(winnerResultMap, filter, output, summary);
            }
            if (true /*findcluster equal value*/) {
                // use config compare
            }
            String mysummary = "";
            boolean success = true;
            for (Summary aSummary : summary) {
                success &= aSummary.success;
                mysummary += aSummary.success + " : " + aSummary.text + " ";
            }
            mysummary = "Summary : " + success + " " + mysummary;
            output.add(mysummary);
            print("Sim " + simtext, "File " + id, output);
        }
    }

    private void getShortRun(Map<String, Object> winnerResultMap, SimulateFilter filter, List<String> output, List<Summary> summary) {
        boolean notshort = true;
        Map<String, Object> aMap = (Map<String, Object>) winnerResultMap.get("0");
        List<StockHistory> history = (List<StockHistory>) aMap.get(SimConstants.HISTORY);
        if (history.size() < filter.getShortrun()) {
            notshort = false;
        }
        //output.add("");
        summary.add(new Summary(notshort, "Too short " + history.size()));
    }

    private void getLucky(Map<String, Object> winnerResultMap, SimulateFilter filter, List<String> output, List<Summary> summary) {
        boolean lucky = false;
        for (Entry<String, Object> entry : winnerResultMap.entrySet()) {
            Map<String, Object> aMap = (Map<String, Object>) entry.getValue();
            List<StockHistory> history = (List<StockHistory>) aMap.get(SimConstants.HISTORY);
            StockHistory last = history.get(history.size() - 1);
            double total = last.getCapital().amount + last.getSum().amount;
            List<SimulateStock> stockhistory = (List<SimulateStock>) aMap.get(SimConstants.STOCKHISTORY);
            Map<String, List<SimulateStock>> stockMap = new HashMap<>();
            for (SimulateStock aStock : stockhistory) {
                new MiscUtil().listGetterAdder(stockMap, aStock.getId(), aStock);
            }
            Map<String, Double> priceMap = new HashMap<>();
            for (Entry<String, List<SimulateStock>> entry2 : stockMap.entrySet()) {
                String id2 = entry2.getKey();
                List<SimulateStock> list = entry2.getValue();
                double sum = list.stream().map(e -> e.getCount()*(e.getSellprice() - e.getBuyprice())).reduce(0.0, Double::sum);
                priceMap.put(id2, sum);
            }
            OptionalDouble max = priceMap.values().stream().mapToDouble(e -> e).max();
            if (max.getAsDouble() / total > filter.getLucky()) {
                lucky = true;
                break;
            }
            output.add("Lucky " + max.getAsDouble() / total);
        }
        summary.add(new Summary(lucky, "Lucky"));
    }

    private void getStable(Map<String, Object> winnerResultMap, List<String> output, List<Summary> summary) {
        boolean stable = true;
        for (Entry<String, Object> entry : winnerResultMap.entrySet()) {
            Map<String, Object> aMap = (Map<String, Object>) entry.getValue();
            List<StockHistory> history = (List<StockHistory>) aMap.get(SimConstants.HISTORY);
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
            for (int i = 0; i < values.size() / 10; i++) {
                total += values.get(0);
            }
            if (total / lasttotal > 0.2) {
                stable = false;
                break;
            }
            output.add("Stable " + total / lasttotal);
        }
        summary.add(new Summary(stable, "Stable"));
    }

    private void getAllAbove(Map<String, Object> winnerResultMap, List<String> output, List<Summary> summary) {
        boolean above = true;
        List<Double> scores = new ArrayList<>();
        for (Entry<String, Object> entry : winnerResultMap.entrySet()) {
            Map<String, Object> aMap = (Map<String, Object>) entry.getValue();
            double score = (double) aMap.get(SimConstants.SCORE);
            scores.add(score);
            if (score < 1) {
                above = false;
                //break;
            }
        }
        //output.add("Score " + Collections.min(scores));
        output.add("Allabove " + Collections.min(scores) + " " + Collections.max(scores) + " " + above);
        summary.add(new Summary(above, "Score " + Collections.min(scores)));
    }

    private void getPopulationAbove(List<Pair<Double, AbstractChromosome>> myList, SimulateFilter filter,
            List<String> output, List<Summary> summary) {
        int max = Math.min(filter.getPopulationabove(), myList.size());
        boolean above = true;
        for (int i = 0; i < max; i++) {
            Pair<Double, AbstractChromosome> aPair = myList.get(i);
            if (aPair.getLeft() < 1) {
                above = false;
                break;
            }
        }
        //output.add("Populationabove " + max + " " + above);
        summary.add(new Summary(above, "Populationabove " + max));
    }

    private void findCommon(List<Pair<Double, AbstractChromosome>> myList, List<String> output) {
        if (true/*filter.*/) {
            Map<Double, List<AbstractChromosome>> chromosomeMap = new LinkedHashMap<>();
            for (Pair<Double, AbstractChromosome> aPair : myList) {
                new MiscUtil().listGetterAdder(chromosomeMap, aPair.getLeft(), aPair.getRight());
            }
            for (Entry<Double, List<AbstractChromosome>> entry : chromosomeMap.entrySet()) {
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
                output.add(entry.getKey() + " " + aList.size() + " "+ aMap);
            }
        }
    }

    private Map<String, List<Pair<Double, AbstractChromosome>>> convert(String param) {
        List<Pair<Double, AbstractChromosome>> myList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Map<String, List<Map<Double, AbstractChromosome>>> res0 = null;
        try {
            res0 = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, IclijConfigMapChromosome>>>>(){});
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
        String id = res0.keySet().iterator().next();
        List<Map<Double, AbstractChromosome>> res = res0.get(id);
        //List<Map> list = JsonUtil.convert(param, List.class);
        SimulateStock s;
        for (Map<Double, AbstractChromosome> map : res) {
            for (Entry<Double, AbstractChromosome> entry : map.entrySet()) {
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
                    Map<String, Object> newMap = new HashMap<>();
                    newMap.put(SimConstants.HISTORY, history);
                    newMap.put(SimConstants.STOCKHISTORY, stockhistory);
                    newMap.put(SimConstants.SCORE, ascore);
                    newMap.put(SimConstants.STARTDATE, startdate);
                    newMap.put(SimConstants.ENDDATE, enddate);
                    resultMap.put(entry3.getKey(), newMap);
                }
                chromosome.setResultMap(resultMap);
            }
        }
        //list.size();
        Map<String, List<Pair<Double, AbstractChromosome>>> map = new HashMap<>();
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
