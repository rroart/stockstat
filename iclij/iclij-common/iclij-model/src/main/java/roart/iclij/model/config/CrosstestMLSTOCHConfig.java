package roart.iclij.model.config;



import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class CrosstestMLSTOCHConfig extends ActionComponentConfigML {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getCrosstestMLSTOCHEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getCrosstestMLSTOCHMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.CROSSTESTMLSTOCH);
    }

}
