package roart.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.model.IncDecItem;
import roart.model.MemoryItem;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;

public class ComponentPredictor extends Component {

    @Override
    public void enable(MyMyConfig conf) {
        conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.TRUE);                
    }

    @Override
    public void disable(MyMyConfig conf) {
        conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);        
    }

    @Override
    public void handle(ControlService srv, MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap) {
        // TODO Auto-generated method stub
        System.out.println("Component not impl " + this.getClass().getName());

    }
    @Override
    public void improve(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap) {
        // TODO Auto-generated method stub
        System.out.println("Component not impl " + this.getClass().getName());
        
    }

    public List<MemoryItem> calculatePredictor(String market, int futuredays, Date baseDate, Date futureDate,
            String categoryTitle, Map<String, List<Double>> resultMap,
            Map<String, List<List<Double>>> categoryValueMap, Integer usedsec, boolean doSave, boolean doPrint) throws Exception {
        List<MemoryItem> memoryList = new ArrayList<>();
        long total = 0;
        long goodInc = 0;
        long goodDec = 0;
        for (String key : categoryValueMap.keySet()) {
            List<List<Double>> resultList = categoryValueMap.get(key);
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() -1 - futuredays);
                List<Double> predFutureList = resultMap.get(key);
                if (predFutureList == null) {
                    continue;
                }
                Double predFuture = predFutureList.get(0);
                if (valFuture != null && valNow != null && predFuture != null) {
                    total++;
                    if (valFuture > valNow && predFuture > valNow) {
                        goodInc++;
                    }
                    if (valFuture < valNow && predFuture < valNow) {
                        goodDec++;
                    }
                }
            }
        }
        //System.out.println("tot " + total + " " + goodInc + " " + goodDec);
        MemoryItem incMemory = new MemoryItem();
        incMemory.setMarket(market);
        incMemory.setRecord(new Date());
        incMemory.setDate(baseDate);
        incMemory.setUsedsec(usedsec);
        incMemory.setFuturedays(futuredays);
        incMemory.setFuturedate(futureDate);
        incMemory.setComponent(PipelineConstants.PREDICTORSLSTM);
        incMemory.setSubcomponent("inc");
        incMemory.setCategory(categoryTitle);
        incMemory.setPositives(goodInc);
        incMemory.setSize(total);
        incMemory.setConfidence((double) goodInc / total);
        if (doSave) {
            incMemory.save();
        }
        MemoryItem decMemory = new MemoryItem();
        decMemory.setMarket(market);
        decMemory.setRecord(new Date());
        decMemory.setDate(baseDate);
        decMemory.setUsedsec(usedsec);
        decMemory.setFuturedays(futuredays);
        decMemory.setFuturedate(futureDate);
        decMemory.setComponent(PipelineConstants.PREDICTORSLSTM);
        decMemory.setSubcomponent("dec");
        decMemory.setCategory(categoryTitle);
        decMemory.setPositives(goodDec);
        decMemory.setSize(total);
        decMemory.setConfidence((double) goodDec / total);
        if (doSave) {
            decMemory.save();
        }
        if (doPrint) {
        System.out.println(incMemory);
        System.out.println(decMemory);
        }
        memoryList.add(incMemory);
        memoryList.add(decMemory);
        return memoryList;
    }
}

