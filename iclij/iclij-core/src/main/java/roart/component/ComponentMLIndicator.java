package roart.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.action.FindProfitAction;
import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.model.IncDecItem;
import roart.model.MemoryItem;
import roart.model.ResultMeta;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.util.ServiceUtil;

public class ComponentMLIndicator extends Component {
    private static String INC = "Inc";
    private static String DEC = "Dec";
    
    @Override
    public void enable(MyMyConfig conf) {
        conf.configValueMap.put(PipelineConstants.MLINDICATOR, Boolean.TRUE);                
    }

    @Override
    public void disable(MyMyConfig conf) {
        conf.configValueMap.put(PipelineConstants.MLINDICATOR, Boolean.FALSE);        
    }

    @Override
    public void handle(ControlService srv, MyMyConfig conf, Map<String, Map<String, Object>> resultMaps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap) {
        // TODO Auto-generated method stub
        resultMaps = srv.getContent();
        Map mlMACDMaps = (Map) resultMaps.get(PipelineConstants.MLINDICATOR);
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
                keys[0] = PipelineConstants.MLINDICATOR;
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
                    Set<Object[]> keyset = okConfMap.keySet();
                    keys = ComponentMLMACD.getRealKeys(keys, keyset);
                    //System.out.println(okListMap.keySet());
                    if (tfpn.equals(INC)) {
                        increase = true;
                        IncDecItem incdec = ComponentMLMACD.mapAdder(buys, key, okConfMap.get(keys), okListMap.get(keys), nameMap);
                        incdec.setIncrease(increase);
                    }
                    if (tfpn.equals(DEC)) {
                        increase = false;
                        IncDecItem incdec = ComponentMLMACD.mapAdder(sells, key, okConfMap.get(keys), okListMap.get(keys), nameMap);
                        incdec.setIncrease(increase);
                    }
                }                        
            }

            resultIndex += returnSize;
            count++;
        }

    }
    @Override
    public void improve(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> badConfMap,
            Map<Object[], List<MemoryItem>> badListMap, Map<String, String> nameMap) {
        // TODO Auto-generated method stub
        List<String> permList = new ArrayList<>();
        String market = badListMap.values().iterator().next().get(0).getMarket();
        ControlService srv = new ControlService();
        srv.getConfig();            
        permList.add(ConfigConstants.AGGREGATORSINDICATORMACD);
        permList.add(ConfigConstants.AGGREGATORSINDICATORRSI);
        int size = permList.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.println("Doing " + i + " " + j);
                if (j != 0) {
                    srv.conf.configValueMap.put(permList.get(i), Boolean.TRUE);
                } else {
                    srv.conf.configValueMap.put(permList.get(i), Boolean.FALSE);
                }
                try {
                    List<Double> newConfidenceList = new ArrayList<>();
                    List<MemoryItem> memories = ServiceUtil.doMLIndicator(srv, market, 0, null, false, false);
                    for(MemoryItem memory : memories) {
                        newConfidenceList.add(memory.getConfidence());
                        //System.out.println(memory);
                    }
                    System.out.println("New confidences " + newConfidenceList);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}

