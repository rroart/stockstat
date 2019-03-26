package roart.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.component.model.ComponentData;
import roart.component.model.RecommenderData;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.result.model.ResultItem;
import roart.service.model.ProfitData;

public abstract class ComponentNoML extends Component {

    @Override
    protected Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param) {
        if (evolve) {
            String confStr = param.getInput().getConfig().getEvolveIndicatorrecommenderEvolutionConfig();
            if (confStr != null) {
                param.getService().conf.getConfigValueMap().put(ConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, confStr);
            }
            Map<String, Object> anUpdateMap = new HashMap<>();
            List<ResultItem> retlist = param.getService().getEvolveRecommender(true, new ArrayList<>(), anUpdateMap);
            if (param.getUpdateMap() != null) {
                param.getUpdateMap().putAll(anUpdateMap); 
            }
            return anUpdateMap;
        }
        return new HashMap<>();
    }


    @Override
    public EvolutionConfig getEvolutionConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata) {
        return null;
    }
}
