package roart.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.constants.ServiceConstants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.util.JsonUtil;
import roart.common.util.MathUtil;
import roart.db.IclijDbDao;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.NeuralNetChromosome2;
import roart.iclij.evolution.marketfilter.chromosome.impl.AboveBelowChromosome;
import roart.iclij.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.util.MiscUtil;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class Evolve {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public void method(String param) {
        
        List<String> output = new ArrayList<>();
        TypeReference ref = new TypeReference<List<LinkedHashMap<Double, AbstractChromosome>>>(){};
        Map<String, Object> myMap = convert(param, new TypeReference<List<LinkedHashMap<Double, NeuralNetChromosome2>>>(){});
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
        for (Pair<Double, AbstractChromosome> my : myList) {
            //System.out.println(my.getKey() + " " + my.getRight());
        }
        //if (true) return;
        Map<String, Object> aconf = (Map) myMap.get(EvolveConstants.DEFAULT);
        System.out.println("aconf" + aconf);
        //if (true) return;
        String title = (String) myMap.get(EvolveConstants.TITLETEXT);
        String[] parts = title.split(" ");
        String market = parts[1];
        String component = parts[2];
        String myclass = parts[3];
        NeuralNetConfig conf = new NeuralNetConfigs().getClass(myclass);
        Pair<String, String> subcomponent = new NeuralNetConfigs().getSubcomponent(myclass);
        List<MLMetricsItem> mltests = null;
        try {
            mltests = IclijDbDao.getAllMLMetrics(market, null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        Map<Pair<String, String>, List<MLMetricsItem>> met = getMLMetrics(mltests, null);
        //System.out.println("co" + component);
        //System.out.println("su" + subcomponent);
        //System.out.println("met" + met.keySet());
        List<MLMetricsItem> blbl = met.get(new ImmutablePair(component, subcomponent.getLeft() + " " + subcomponent.getRight()));
        double avg = 0;
        if (blbl != null) {
            avg = blbl.stream().map(MLMetricsItem::getTestAccuracy).mapToDouble(e -> e).average().orElse(0);
        }
        double newer = myList.get(0).getLeft();
        boolean better = avg < newer;
        myList.add(new ImmutablePair(avg, conf + " (default)"));
        Comparator<Pair> comparator = new Comparator<>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                return Double.valueOf((Double)o1.getLeft()).compareTo(Double.valueOf((Double)o2.getLeft()));
            }
        };
        Collections.sort(myList, comparator);
        Collections.reverse(myList);
        for (Pair<Double, AbstractChromosome> my : myList) {
            output.add(my.getKey() + " " + my.getRight());
        }
        output.add("");
        output.add("Summary: " + better + " " + MathUtil.round(avg, 2) + " vs " + newer);
        print(ServiceConstants.EVOLVEFILTEREVOLVE + " " + title, null, output);
    }

    // dup
    protected Map<Pair<String, String>, List<MLMetricsItem>> getMLMetrics(List<MLMetricsItem> mltests, Double confidence) {
        List<MLMetricsItem> returnedMLMetrics = new ArrayList<>();
        for (MLMetricsItem test : mltests) {
            addNewest(returnedMLMetrics, test, 0.0);
        }
        Map<Pair<String, String>, List<MLMetricsItem>> moreReturnedMLMetrics = new HashMap<>();
        for (MLMetricsItem metric : returnedMLMetrics) {
            Pair key = new ImmutablePair(metric.getComponent(), metric.getSubcomponent());
            new MiscUtil().listGetterAdder(moreReturnedMLMetrics, key, metric);  
        }
        return moreReturnedMLMetrics;
    }

    // dup
    private void addNewest(List<MLMetricsItem> mlTests, MLMetricsItem test, Double confidence) {
        if (test.getTestAccuracy() == null || test.getTestAccuracy() < confidence) {
            return;
        }
        if (test.getThreshold() == null || test.getThreshold() != 1.0) {
            return;
        }
        MLMetricsItem replace = null;
        for (MLMetricsItem aTest : mlTests) {
            Boolean moregeneralthan = aTest.moreGeneralThan(test);
            // we don't need this anymore
            if (false && moregeneralthan != null && moregeneralthan) {
                replace = aTest;
                break;
            }
            Boolean olderthan = aTest.olderThan(test);
            if (olderthan != null && olderthan) {
                replace = aTest;
                break;
            }
        }
        if (replace != null) {
            int index = mlTests.indexOf(replace);
            mlTests.set(index, test);
            return;
        }
        mlTests.add(test);
    }

    public void method2(String param) {
        List<String> output = new ArrayList<>();
        Map<String, Object> myMap = convert(param, new TypeReference<List<LinkedHashMap<Double, ConfigMapChromosome2>>>(){});
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
        for (Pair<Double, AbstractChromosome> my : myList) {
            System.out.println(my.getKey() + " " + my.getRight());
        }
        Map<String, Object> aconf = (Map) myMap.get(EvolveConstants.DEFAULT);
        System.out.println("aconf" + aconf);
        String title = (String) myMap.get(EvolveConstants.TITLETEXT);
        System.out.println(title);
        String[] parts = title.split(" ");
        String market = parts[1];
        String component = parts[2];
        String subcomponent = parts[3];
        String subsubcomponent = parts[4];
        
        
        
        ConfigMapChromosome2 firstChromosome = (ConfigMapChromosome2) myList.get(0).getRight();

        Map<String, Object> aMap = firstChromosome.getMap();
        aMap.remove(ConfigConstants.MACHINELEARNINGMLDYNAMIC);
        aMap.remove(ConfigConstants.MACHINELEARNINGMLCLASSIFY);
        aMap.remove(ConfigConstants.MACHINELEARNINGMLLEARN);
        aMap.remove(ConfigConstants.MACHINELEARNINGMLCROSS);
        aMap.remove(ConfigConstants.MISCMYTABLEDAYS);
        aMap.remove(ConfigConstants.MISCMYDAYS);
        MapDifference<String, Object> diff = Maps.difference(aMap, aconf);
        aMap = diff.entriesInCommon();
        Set<String> keys = aMap.keySet();
        //Map<String, Object> anotherMap = new HashMap<>(firstChromosome.getMap());
        //anotherMap.keySet().removeAll(keys);

        Pair<String, String> subComponent = new ImmutablePair<>(subcomponent, subsubcomponent);
        List<MLMetricsItem> mltests = null;
        try {
            mltests = IclijDbDao.getAllMLMetrics(market, null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        Map<Pair<String, String>, List<MLMetricsItem>> met = getMLMetrics(mltests, null);
        //System.out.println("co" + component);
        //System.out.println("su" + subcomponent);
        //System.out.println("met" + met.keySet());
        List<MLMetricsItem> blbl = met.get(new ImmutablePair(component, subComponent.getLeft() + " " + subComponent.getRight()));
        double avg = 0;
        if (blbl != null) {
            avg = blbl.stream().map(MLMetricsItem::getTestAccuracy).mapToDouble(e -> e).average().orElse(0);
        }
        double newer = myList.get(0).getLeft();
        boolean better = avg < newer;
        myList.add(new ImmutablePair(avg, aconf + " (default)"));
        Comparator<Pair> comparator = new Comparator<>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                return Double.valueOf((Double)o1.getLeft()).compareTo(Double.valueOf((Double)o2.getLeft()));
            }
        };
        Collections.sort(myList, comparator);
        Collections.reverse(myList);
        
        List<String> list2 = new ArrayList<>();
        list2 = new ArrayList<>(firstChromosome.getMap().keySet());
        list2.removeAll(keys);
        output.add("Common");
        output.add(newer + " " + aMap);
        output.add("" + list2);
        output.add("");
   
        

        
        for (Pair<Double, AbstractChromosome> my : myList) {
            output.add(my.getKey() + " " + my.getRight());
        }
        output.add("");
        output.add("Summary: " + better + " " + MathUtil.round(avg, 2) + " vs " + newer);
        print(ServiceConstants.EVOLVEFILTEREVOLVE + " " + title, null, output);
    }

    public void method3(String param) {
        Map<String, Object> myMap = convert(param, new TypeReference<List<LinkedHashMap<Double, MarketFilterChromosome2>>>(){});
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
    }

    public void method4(String param) {
        Map<String, Object> myMap = convert(param, new TypeReference<List<LinkedHashMap<Double, AboveBelowChromosome>>>(){});
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
     }

    private Map<String, Object> convert(String param, TypeReference typeref) {
        Map<String, Object> map = new HashMap<>();
        //Map<String, Object> map = JsonUtil.convert(param, Map.class);
        //map.keySet();
        List<Pair<Double, AbstractChromosome>> myList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //List<Map<Double, AbstractChromosome>> res = null;
        Map<String, Object /*List<LinkedHashMap<Double, NeuralNetChromosome2>>*/> res0 = null;
        try {
            //res = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>>>(){});
            //res0 = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>>>(){});
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
            System.out.println(value.getClass().getName());
            if (value.getClass() == String.class) {
                //map.put(key, value);
                //System.out.println("kv"+key+value);
            } else if (value.getClass() == LinkedHashMap.class) {
                TypeReference typeref2 = new TypeReference<Map<String, Object>>(){};
                value = mapper.convertValue(value, typeref2);
            } else {
                //Class myclass = chromosomeClass;
                //System.out.println("xxx" + chromosomeClass.getName());
                //String s = ((List<LinkedHashMap<String, T>>)value).get(0).keySet().iterator().next();
                //System.out.println("yyy" + ((List<LinkedHashMap<String, T>>)value).get(0).get(s).getClass().getName());
                //System.out.println("val"+ value);
                value = mapper.convertValue(value, typeref);
                //Double d = ((List<LinkedHashMap<Double, T>>)value).get(0).keySet().iterator().next();
                //System.out.println("zzz" + ((List<LinkedHashMap<Double, T>>)value).get(0).get(d).getClass().getName());
            }
            map.put(key, value);
        }
        //Object bl = res0.get("1611321055358.txt");
        //List l = (List) bl;
        //System.out.println(l.get(0).getClass().getName());
        String id = (String) map.get(EvolveConstants.ID);
        List<LinkedHashMap<Double, AbstractChromosome>> res = (List<LinkedHashMap<Double, AbstractChromosome>>) map.get(id);
        //for (Map<Double, AbstractChromosome> map : res) {
        for (LinkedHashMap<Double, AbstractChromosome> aMap : res) {
            //for (Entry<Double, AbstractChromosome> entry : map.entrySet()) {
            for (Entry<Double, AbstractChromosome> entry : aMap.entrySet()) {
                Double score = entry.getKey();
                //System.out.println(entry.getValue());
                AbstractChromosome chromosome = (AbstractChromosome) entry.getValue();
                Pair<Double, AbstractChromosome> pair = new ImmutablePair<>(score, chromosome);
                myList.add(pair);
            }
        }
       /*
        List<LinkedHashMap> list = JsonUtil.convert(param, List.class);
        for (LinkedHashMap<Double, LinkedHashMap> map : list) {
            for (Entry<Double, LinkedHashMap> entry : map.entrySet()) {
                LinkedHashMap<String, LinkedHashMap> map2 = entry.getValue();
                for (Entry<String, LinkedHashMap> entry2 : map2.entrySet()) {
                    LinkedHashMap<Integer, LinkedHashMap> map3 = entry2.getValue();
                    for (Entry<Integer, LinkedHashMap> entry3 : map3.entrySet()) {
                        LinkedHashMap<String, Object> map4 = entry3.getValue();
                        for (Entry<String, Object> entry4 : map4.entrySet()) {
                            System.out.println(entry4);
                        }
                    }
                }
            }
        }
        */
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
    
}
