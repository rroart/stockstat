package roart.iclij.model.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import roart.common.ml.MLMaps;
import roart.common.ml.MLMapsML;
import roart.common.util.JsonUtil;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;

public abstract class ActionComponentConfigML extends ActionComponentConfig {

    @Override
    public Map<String, EvolveMLConfig> getMLConfig(Market market, IclijConfig config, String mlmarket) {
        System.out.println(config.getEvolveMLMLConfig());
        String localMl = getLocalMLConfig(config);
        String ml = config.getEvolveMLMLConfig();
        MLConfigs marketMlConfig = market.getMlconfig();
        MLConfigs mlConfig = JsonUtil.convert(ml, MLConfigs.class);
        MLConfigs localMLConfig = JsonUtil.convert(localMl, MLConfigs.class);
        // special
        mlConfig.merge(localMLConfig);
        mlConfig.merge(marketMlConfig);
        Map<String, EvolveMLConfig> mlConfigMap = getMLConfigs(mlConfig);
        return mlConfigMap;
    }
    
    @Override
    public List<String> getSubComponents(Market market, IclijConfig config, String mlmarket) {
        List<String> subComponents = new ArrayList<>();
        Map<String, Pair<String, String>> revMap = getMLMaps().getMapRev();
        Map<String, EvolveMLConfig> mlConfigs = getMLConfig(market, config, mlmarket);
        for (Entry<String, EvolveMLConfig> entry : mlConfigs.entrySet()) {
            EvolveMLConfig mlConfig = entry.getValue();
            if (mlConfig.getEnable()) {
                String key = entry.getKey();
                Pair<String, String> subComponent = revMap.get(key);
                subComponents.add(subComponent.getLeft() + " " + subComponent.getRight());
            } else {
                int jj = 0;
            }
        }
        return subComponents;
    }
    
     public MLMaps getMLMaps() {
        return new MLMapsML();
    }

     private Map<String, EvolveMLConfig> getMLConfigs(MLConfigs mlConfig) {
         return mlConfig.getAll();
     }
     
}
