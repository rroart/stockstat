package roart.iclij.model.config; 




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class ImproveFilterMLMultiConfig extends ActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getImproveFilterMLMultiEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getImproveFilterMLMultiMLConfig();
    }
    
    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.IMPROVEFILTERMLMULTI);
    }

}
