package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.PredictorParam;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.ControlService;

public class ComponentPredictor extends Component {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void enable(MyMyConfig conf) {
        conf.getConfigValueMap().put(ConfigConstants.PREDICTORS, Boolean.TRUE);                
    }

    @Override
    public void disable(MyMyConfig conf) {
        conf.getConfigValueMap().put(ConfigConstants.PREDICTORS, Boolean.FALSE);        
    }

    @Override
    public void handle(ControlService srv, MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap, IclijConfig config, Map<String, Object> updateMap) {
        log.info("Component not impl {}", this.getClass().getName());
    }
    
    @Override
    public Map<String, String> improve(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap) {
        log.info("Component not impl {}", this.getClass().getName());
        return new HashMap<>();        
    }

    public List<MemoryItem> calculatePredictor(PredictorParam param) throws Exception {
        List<MemoryItem> memoryList = new ArrayList<>();
        long total = 0;
        long goodInc = 0;
        long goodDec = 0;
        long totalInc = 0;
        long totalDec = 0;
        for (String key : param.getCategoryValueMap().keySet()) {
            List<List<Double>> resultList = param.getCategoryValueMap().get(key);
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - param.getFuturedays());
                List<Double> predFutureList = param.getResultMap().get(key);
                if (predFutureList == null) {
                    continue;
                }
                Double predFuture = predFutureList.get(0);
                if (valFuture != null && valNow != null && predFuture != null) {
                    total++;
                    if (predFuture > valNow) {
                        totalInc++;
                        if (valFuture > valNow) {
                            goodInc++;
                        }
                    }
                    if (predFuture < valNow) {
                        totalDec++;
                        if (valFuture < valNow) {
                            goodDec++;
                        }
                    }
                }
            }
        }
        //System.out.println("tot " + total + " " + goodInc + " " + goodDec);
        MemoryItem incMemory = new MemoryItem();
        incMemory.setMarket(param.getMarket());
        incMemory.setRecord(LocalDate.now());
        incMemory.setDate(param.getBaseDate());
        incMemory.setUsedsec(param.getUsedsec());
        incMemory.setFuturedays(param.getFuturedays());
        incMemory.setFuturedate(param.getFutureDate());
        incMemory.setComponent(PipelineConstants.PREDICTORSLSTM);
        incMemory.setSubcomponent("inc");
        incMemory.setCategory(param.getCategoryTitle());
        incMemory.setPositives(goodInc);
        incMemory.setSize(total);
        incMemory.setConfidence((double) goodInc / totalInc);
        if (param.isDoSave()) {
            incMemory.save();
        }
        MemoryItem decMemory = new MemoryItem();
        decMemory.setMarket(param.getMarket());
        decMemory.setRecord(LocalDate.now());
        decMemory.setDate(param.getBaseDate());
        decMemory.setUsedsec(param.getUsedsec());
        decMemory.setFuturedays(param.getFuturedays());
        decMemory.setFuturedate(param.getFutureDate());
        decMemory.setComponent(PipelineConstants.PREDICTORSLSTM);
        decMemory.setSubcomponent("dec");
        decMemory.setCategory(param.getCategoryTitle());
        decMemory.setPositives(goodDec);
        decMemory.setSize(total);
        decMemory.setConfidence((double) goodDec / totalDec);
        if (param.isDoSave()) {
            decMemory.save();
        }
        if (param.isDoPrint()) {
        System.out.println(incMemory);
        System.out.println(decMemory);
        }
        memoryList.add(incMemory);
        memoryList.add(decMemory);
        return memoryList;
    }
}

