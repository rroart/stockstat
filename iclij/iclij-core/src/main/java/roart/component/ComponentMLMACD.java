package roart.component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import roart.model.IncDecItem;
import roart.model.MemoryItem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.action.FindProfitAction;
import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.model.ResultMeta;
import roart.pipeline.PipelineConstants;

public class ComponentMLMACD extends Component {
    @Override
    public void enable(MyMyConfig conf) {
        conf.configValueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.TRUE);        
        conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);        
    }

    @Override
    public void disable(MyMyConfig conf) {
        conf.configValueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.FALSE);        
        conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
    }

    @Override
    public void handle(MyMyConfig conf, Map<String, Map<String, Object>> resultMaps, List<Integer> positions, Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap, Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap) {
       //System.out.println(resultMaps.keySet());
        Map mlMACDMaps = (Map) resultMaps.get(PipelineConstants.MLMACD);
        //System.out.println("mlm " + mlMACDMaps.keySet());
        Integer category = (Integer) mlMACDMaps.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) mlMACDMaps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) mlMACDMaps.get(PipelineConstants.RESULT);
        if (resultMap == null) {
            return;
        }
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlMACDMaps.get(PipelineConstants.PROBABILITY);
        List<List> resultMetaArray = (List<List>) mlMACDMaps.get(PipelineConstants.RESULTMETAARRAY);
        //List<ResultMeta> resultMeta = (List<ResultMeta>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<Object> objectList = (List<Object>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());

        int resultIndex = 0;
        int count = 0;
        for (List meta : resultMetaArray) {
            int returnSize = (int) meta.get(2);

            if (positions.contains(count)) {
                Object[] keys = new Object[2];
                keys[0] = PipelineConstants.MLMACD;
                keys[1] = count;
                for (String key : categoryValueMap.keySet()) {
                    List<List<Double>> resultList = categoryValueMap.get(key);
                    List<Double> mainList = resultList.get(0);
                    if (mainList == null) {
                        continue;
                    }
                    List<Object> list = resultMap.get(key);
                    if (list == null) {
                        continue;
                    }
                    String tfpn = (String) list.get(resultIndex);
                    if (tfpn == null) {
                        continue;
                    }
                    boolean increase = false;
                    //System.out.println(okConfMap.keySet());
                    for (Object[] keyss : okConfMap.keySet()) {
                        if (keys[0].equals(keyss[0])) {
                            if (keys[1].equals(keyss[1])) {
                                keys = keyss;
                                break;
                            }
                        }
                    }
                    //System.out.println(okListMap.keySet());
                    if (tfpn.equals(FindProfitAction.TP) || tfpn.equals(FindProfitAction.FN)) {
                        increase = true;
                        IncDecItem incdec = mapAdder(buys, key, okConfMap.get(keys), okListMap.get(keys), nameMap);
                        incdec.setIncrease(increase);
                    }
                    if (tfpn.equals(FindProfitAction.TN) || tfpn.equals(FindProfitAction.FP)) {
                        increase = false;
                        IncDecItem incdec = mapAdder(sells, key, okConfMap.get(keys), okListMap.get(keys), nameMap);
                        incdec.setIncrease(increase);
                    }
                }                        
            }

            resultIndex += returnSize;
            count++;
        }

    }
    private static IncDecItem mapAdder(Map<String, IncDecItem> map, String key, Double add, List<MemoryItem> memoryList, Map<String, String> nameMap) {
        MemoryItem memory = memoryList.get(0);
        IncDecItem val = map.get(key);
        if (val == null) {
            val = new IncDecItem();
            val.setRecord(new Date());
            val.setId(key);
            val.setMarket(memory.getMarket());
            val.setDescription("");
            val.setName(nameMap.get(key));
            val.setScore(0.0);
            map.put(key, val);
        }
        val.setScore(val.getScore() + add);
        val.setDescription(val.getDescription() + memory.getSubcomponent() + ", ");
        return val;
    }
}

