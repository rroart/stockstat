package roart.component;

import java.util.List;
import java.util.Map;

import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.model.IncDecItem;
import roart.model.MemoryItem;
import roart.pipeline.PipelineConstants;

public class ComponentRecommender extends Component {
    @Override
    public void enable(MyMyConfig conf) {
        conf.configValueMap.put(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, Boolean.TRUE);                
    }

    @Override
    public void disable(MyMyConfig conf) {
        conf.configValueMap.put(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, Boolean.FALSE);        
    }

    @Override
    public void handle(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap) {
        // TODO Auto-generated method stub
        System.out.println("Component not impl " + this.getClass().getName());

    }
}

