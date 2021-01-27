package roart.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.util.JsonUtil;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.NeuralNetChromosome2;
import roart.iclij.evolution.marketfilter.chromosome.impl.AboveBelowChromosome;
import roart.iclij.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;

public class Evolve {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public void method(String param) {
        
        TypeReference ref = new TypeReference<List<LinkedHashMap<Double, AbstractChromosome>>>(){};
        Map<String, Object> myMap = convert(param, new TypeReference<List<LinkedHashMap<Double, NeuralNetChromosome2>>>(){});
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
        myList.size();
    }

    public void method2(String param) {
        Map<String, Object> myMap = convert(param, new TypeReference<List<LinkedHashMap<Double, ConfigMapChromosome2>>>(){});
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
        myList.size();
    }

    public void method3(String param) {
        Map<String, Object> myMap = convert(param, new TypeReference<List<LinkedHashMap<Double, MarketFilterChromosome2>>>(){});
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
        myList.size();
    }

    public void method4(String param) {
        Map<String, Object> myMap = convert(param, new TypeReference<List<LinkedHashMap<Double, AboveBelowChromosome>>>(){});
        String id = (String) myMap.get(EvolveConstants.ID);
        List<Pair<Double, AbstractChromosome>> myList = (List<Pair<Double, AbstractChromosome>>) myMap.get(id);
        myList.size();
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
            System.out.println(entry.getKey() + " " + entry.getValue().getClass().getName());
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value.getClass() == String.class) {
                //map.put(key, value);
                System.out.println("kv"+key+value);
            } else {
                //Class myclass = chromosomeClass;
                //System.out.println("xxx" + chromosomeClass.getName());
                //String s = ((List<LinkedHashMap<String, T>>)value).get(0).keySet().iterator().next();
                //System.out.println("yyy" + ((List<LinkedHashMap<String, T>>)value).get(0).get(s).getClass().getName());
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
                System.out.println(entry.getValue());
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

    private Map<String, List<Pair<Double, AbstractChromosome>>> convert2(String param) {
        //Map<String, Object> map = JsonUtil.convert(param, Map.class);
        //map.keySet();
        List<Pair<Double, AbstractChromosome>> myList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //List<Map<Double, AbstractChromosome>> res = null;
        Map<String, List<LinkedHashMap<Double, ConfigMapChromosome2>>> res0 = null;
        try {
            //res = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>>>(){});
            res0 = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, ConfigMapChromosome2>>>>(){});
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
        List<LinkedHashMap<Double, ConfigMapChromosome2>> res = res0.get(id);
        //for (Map<Double, AbstractChromosome> map : res) {
        for (LinkedHashMap<Double, ConfigMapChromosome2> map : res) {
            //for (Entry<Double, AbstractChromosome> entry : map.entrySet()) {
            for (Entry<Double, ConfigMapChromosome2> entry : map.entrySet()) {
                Double score = entry.getKey();
                AbstractChromosome chromosome = entry.getValue();
                System.out.println(chromosome);
                System.out.println(chromosome.getClass().getName());
                
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
            log.error(Constants.EXCEPTION, e);
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            log.error(Constants.EXCEPTION, e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(Constants.EXCEPTION, e);
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
            log.error(Constants.EXCEPTION, e);
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            log.error(Constants.EXCEPTION, e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(Constants.EXCEPTION, e);
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
