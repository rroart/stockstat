package roart.component;

import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.config.EvolutionConfig;

public class DatasetComponentMLCCI extends ComponentMLCCI {
    @Override
    public EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata) {
        String localDataset = componentdata.getInput().getConfig().getDatasetMLCCIEvolutionConfig();
        return JsonUtil.convert(localDataset, EvolutionConfig.class);
    }

    @Override
    public String getLocalMLConfig(ComponentData componentdata) {
        return componentdata.getInput().getConfig().getDatasetMLCCIMLConfig();
    }

}
