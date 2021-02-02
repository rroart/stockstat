package roart.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import roart.common.util.JsonUtil;
import roart.common.util.MathUtil;
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
        Map<String, Object> myMap = convert(param);
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
        if (myList.size() > 0) {
            //for ()
            Pair<Double, AbstractChromosome> winnerPair = myList.get(0);
            IclijConfigMapChromosome winnerChromosome = (IclijConfigMapChromosome) winnerPair.getValue();
            int adviser = getAdviser(winnerChromosome);
            String simtext = (String) myMap.get(EvolveConstants.TITLETEXT); // getSimtext(winnerChromosome);
            SimulateFilter[] filters = new SimulateFilter[10];
            SimulateFilter filter = new SimulateFilter(5, 0.8, 0.8, true, 16, true);
            filters[0] = new SimulateFilter(5, 0.2, 0.2, true, 16, true);
            filters[1] = new SimulateFilter(5, 0.2, 0.2, true, 16, true);
            filters[2] = new SimulateFilter(5, 0.2, 0.2, true, 16, true);
            filters[3] = new SimulateFilter(5, 0.2, 0.2, true, 16, true);
            filters[4] = new SimulateFilter(5, 0.2, 0.2, true, 16, true);
            filters[5] = new SimulateFilter(5, 0.2, 0.2, true, 16, true);
            filters[6] = new SimulateFilter(5, 0.2, 0.2, true, 16, true);
            filters[7] = new SimulateFilter(5, 0.2, 0.2, true, 16, true);
            filters[8] = new SimulateFilter(5, 0.2, 0.2, true, 16, true);
            filters[9] = new SimulateFilter(5, 0.2, 0.2, true, 16, true);
            //filter = filters[adviser];
            List<String> output = new ArrayList<>();

            //output.add("Sim " + simtext);
            //output.add("File: " + id);
            // config compare always true
            if (filter.getPopulationabove() > 0) {
                getPopulationAbove(myList, filter, output);
                output.add("");
            }
            Map<Double, List<AbstractChromosome>> chromosomeMap = groupCommon(myList, output);
            getCommon(chromosomeMap, output);
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
                Map<String, Object> map = chromosome.getMap();
                Map<String, Object> resultMap = chromosome.getResultMap();
                if (filter.isUseclusters()) {
                }
                Double keyScore = score;
                if (filter.isAllabove()) {
                    keyScore = getAllAbove(resultMap, output, summary);
                }
                if (filter.getStable() > 0) {
                    getStable(resultMap, filter, output, summary);
                }
                if (filter.getLucky() > 0) {
                    getLucky(resultMap, filter, output, summary);

                }
                if (filter.getShortrun() > 0) {
                    getShortRun(resultMap, filter, output, summary);
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
                mysummary = "Summary : " + success + " Score " + MathUtil.round(score, 2) + " " + mysummary;
                output.add(mysummary);
                output.add("");
                if (success) {
                    summaries.add(new ImmutablePair<>(keyScore, mysummary));
                }
            }
            Double maxScore = summaries.stream().mapToDouble(e -> e.getKey()).max().orElse(0);
            List<Pair<Double, String>> maxSummaries = summaries.stream().filter(e -> e.getKey().equals(maxScore)).collect(Collectors.toList());
            for (Pair<Double, String> aSummary : maxSummaries) {
                output.add("Max " + MathUtil.round(aSummary.getKey(), 2) + " " + aSummary.getValue());
            }
            print("sim " + simtext, "File " + id, output);
        }
    }

    private int getAdviser(IclijConfigMapChromosome chromosome) {
        Map<String, Object> map = chromosome.getMap();
        int adviser = (int) map.get(IclijConfigConstants.SIMULATEINVESTADVISER);
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
            List<Pair<String, Double>> list = new ArrayList<>();
            for (Entry<String, Double> anEntry : priceMap.entrySet()) {
                list.add(new ImmutablePair<>(anEntry.getKey(), anEntry.getValue()));
            }
            Comparator<Pair> comparator = new Comparator<>() {
                @Override
                public int compare(Pair o1, Pair o2) {
                    return Double.valueOf((Double)o1.getRight()).compareTo(Double.valueOf((Double)o2.getRight()));
                }
            };
            Collections.sort(list, comparator);
            Collections.reverse(list);
            int cnt = 3;
            for (Pair<String, Double> anEntry : list) {
                output.add(anEntry.getKey() + " " + MathUtil.round(anEntry.getValue(), 2) + " " + MathUtil.round(anEntry.getValue() / total, 2));
                cnt--;
                if (cnt <= 0) {
                    break;
                }
            }
            double max = priceMap.values().stream().mapToDouble(e -> e).max().orElse(0);
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

    private void getCommon(Map<Double, List<AbstractChromosome>> chromosomeMap, List<String> output) {
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
        }
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
