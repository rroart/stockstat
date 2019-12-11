package roart.component;

import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class EvolveComponentMLRSI extends ComponentMLRSI {
    @Override
    public EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata) {
        String localEvolve = componentdata.getInput().getConfig().getEvolveMLRSIEvolutionConfig();
        return JsonUtil.convert(localEvolve, EvolutionConfig.class);
    }

    @Override
    public String getLocalMLConfig(ComponentData componentdata) {
        return componentdata.getInput().getConfig().getEvolveMLRSIMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.EVOLVEMLRSI);
    }

}
