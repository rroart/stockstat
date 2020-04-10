package roart.iclij.model.config;




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class ImproveProfitMLCCIConfig extends ActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getImproveProfitMLCCIEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getImproveProfitMLCCIMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.IMPROVEPROFITMLCCI);
    }

}
