package roart.iclij.model.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.common.ml.MLMaps;
import roart.common.ml.MLMapsNoML;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;

public abstract class ActionComponentConfigNoML extends ActionComponentConfig {

    @Override
    public List<String> getSubComponents(Market market, IclijConfig config, String mlmarket, String actionML) {
        List<String> list = new ArrayList<>();
        list.add(null);
        //list.add("");
        return list;
    }

    @Override
    public Map<String, EvolveMLConfig> getMLConfig(Market market, IclijConfig config, String mlmarket, String actionML) {
        return null;
    }

    public MLMaps getMLMaps() {
        return new MLMapsNoML();
    }

    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return null;
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return null;
    }

    @Override
    public int getPriority(IclijConfig config) {
        return 0;
    }
}
