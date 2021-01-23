package roart.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.common.util.JsonUtil;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.NeuralNetChromosome2;
import roart.iclij.evolution.marketfilter.chromosome.impl.AboveBelowChromosome;
import roart.iclij.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;

public class Evolve {

    public void method(String param) {
        Map<String, List<Pair<Double, AbstractChromosome>>> myMap = convert(param);
        String id = myMap.keySet().iterator().next();
        List<Pair<Double, AbstractChromosome>> myList = myMap.get(id);
        myList.size();
    }

    public void method2(String param) {
        Map<String, List<Pair<Double, AbstractChromosome>>> myMap = convert2(param);
        String id = myMap.keySet().iterator().next();
        List<Pair<Double, AbstractChromosome>> myList = myMap.get(id);
        myList.size();
    }

    public void method3(String param) {
        Map<String, List<Pair<Double, AbstractChromosome>>> myMap = convert3(param);
        String id = myMap.keySet().iterator().next();
        List<Pair<Double, AbstractChromosome>> myList = myMap.get(id);
        myList.size();
    }

    public void method4(String param) {
        Map<String, List<Pair<Double, AbstractChromosome>>> myMap = convert4(param);
        String id = myMap.keySet().iterator().next();
        List<Pair<Double, AbstractChromosome>> myList = myMap.get(id);
        myList.size();
    }

    private Map<String, List<Pair<Double, AbstractChromosome>>> convert(String param) {
        //Map<String, Object> map = JsonUtil.convert(param, Map.class);
        //map.keySet();
        List<Pair<Double, AbstractChromosome>> myList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //List<Map<Double, AbstractChromosome>> res = null;
        Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>> res0 = null;
        try {
            //res = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>>>(){});
            res0 = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>>>(){});
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String id = res0.keySet().iterator().next();
        List<LinkedHashMap<Double, NeuralNetChromosome2>> res = res0.get(id);
        //for (Map<Double, AbstractChromosome> map : res) {
        for (LinkedHashMap<Double, NeuralNetChromosome2> map : res) {
            //for (Entry<Double, AbstractChromosome> entry : map.entrySet()) {
            for (Entry<Double, NeuralNetChromosome2> entry : map.entrySet()) {
                Double score = entry.getKey();
                AbstractChromosome chromosome = entry.getValue();
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
        Map<String, List<Pair<Double, AbstractChromosome>>> map = new HashMap<>();
        map.put(id, myList);
        return map;
    }

    private Map<String, List<Pair<Double, AbstractChromosome>>> convert2(String param) {
        //Map<String, Object> map = JsonUtil.convert(param, Map.class);
        //map.keySet();
        List<Pair<Double, AbstractChromosome>> myList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //List<Map<Double, AbstractChromosome>> res = null;
        Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>> res0 = null;
        try {
            //res = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>>>(){});
            res0 = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, ConfigMapChromosome2>>>>(){});
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String id = res0.keySet().iterator().next();
        List<LinkedHashMap<Double, NeuralNetChromosome2>> res = res0.get(id);
        //for (Map<Double, AbstractChromosome> map : res) {
        for (LinkedHashMap<Double, NeuralNetChromosome2> map : res) {
            //for (Entry<Double, AbstractChromosome> entry : map.entrySet()) {
            for (Entry<Double, NeuralNetChromosome2> entry : map.entrySet()) {
                Double score = entry.getKey();
                AbstractChromosome chromosome = entry.getValue();
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
        Map<String, List<Pair<Double, AbstractChromosome>>> map = new HashMap<>();
        map.put(id, myList);
        return map;
    }
    private Map<String, List<Pair<Double, AbstractChromosome>>> convert3(String param) {
        //Map<String, Object> map = JsonUtil.convert(param, Map.class);
        //map.keySet();
        List<Pair<Double, AbstractChromosome>> myList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //List<Map<Double, AbstractChromosome>> res = null;
        Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>> res0 = null;
        try {
            //res = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>>>(){});
            res0 = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, MarketFilterChromosome2>>>>(){});
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String id = res0.keySet().iterator().next();
        List<LinkedHashMap<Double, NeuralNetChromosome2>> res = res0.get(id);
        //for (Map<Double, AbstractChromosome> map : res) {
        for (LinkedHashMap<Double, NeuralNetChromosome2> map : res) {
            //for (Entry<Double, AbstractChromosome> entry : map.entrySet()) {
            for (Entry<Double, NeuralNetChromosome2> entry : map.entrySet()) {
                Double score = entry.getKey();
                AbstractChromosome chromosome = entry.getValue();
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
        Map<String, List<Pair<Double, AbstractChromosome>>> map = new HashMap<>();
        map.put(id, myList);
        return map;
    }
    private Map<String, List<Pair<Double, AbstractChromosome>>> convert4(String param) {
        //Map<String, Object> map = JsonUtil.convert(param, Map.class);
        //map.keySet();
        List<Pair<Double, AbstractChromosome>> myList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //List<Map<Double, AbstractChromosome>> res = null;
        Map<String, List<LinkedHashMap<Double, AboveBelowChromosome>>> res0 = null;
        try {
            //res = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, AboveBelowChromosome>>>>(){});
            res0 = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, AboveBelowChromosome>>>>(){});
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String id = res0.keySet().iterator().next();
        List<LinkedHashMap<Double, AboveBelowChromosome>> res = res0.get(id);
        //for (Map<Double, AbstractChromosome> map : res) {
        for (LinkedHashMap<Double, AboveBelowChromosome> map : res) {
            //for (Entry<Double, AbstractChromosome> entry : map.entrySet()) {
            for (Entry<Double, AboveBelowChromosome> entry : map.entrySet()) {
                Double score = entry.getKey();
                AbstractChromosome chromosome = entry.getValue();
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
        Map<String, List<Pair<Double, AbstractChromosome>>> map = new HashMap<>();
        map.put(id, myList);
        return map;
    }
}
