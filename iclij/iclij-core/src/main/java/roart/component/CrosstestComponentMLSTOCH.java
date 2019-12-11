package roart.component;

import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class CrosstestComponentMLSTOCH extends ComponentMLSTOCH {
    @Override
    public EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata) {
        String localCrosstest = componentdata.getInput().getConfig().getCrosstestMLSTOCHEvolutionConfig();
        return JsonUtil.convert(localCrosstest, EvolutionConfig.class);
    }

    @Override
    public String getLocalMLConfig(ComponentData componentdata) {
        return componentdata.getInput().getConfig().getCrosstestMLSTOCHMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.CROSSTESTMLSTOCH);
    }

}
