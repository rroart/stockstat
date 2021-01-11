package roart.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
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

public class Evolve {

    public void method(String param) {
        List<Pair<Double, AbstractChromosome>> myList = convert(param);
        myList.size();
    }

    private List<Pair<Double, AbstractChromosome>> convert(String param) {
        //Map<String, Object> map = JsonUtil.convert(param, Map.class);
        //map.keySet();
        List<Pair<Double, AbstractChromosome>> myList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //List<Map<Double, AbstractChromosome>> res = null;
        List<LinkedHashMap<Double, NeuralNetChromosome2>> res = null;
        try {
            //res = mapper.readValue(param, new TypeReference<List<LinkedHashMap<Double, NeuralNetChromosome2>>>(){});
            res = mapper.readValue(param, new TypeReference<List<LinkedHashMap<Double, NeuralNetChromosome2>>>(){});
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
        return myList;
    }
}
