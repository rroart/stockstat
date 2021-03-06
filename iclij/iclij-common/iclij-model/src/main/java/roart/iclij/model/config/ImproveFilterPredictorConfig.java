package roart.iclij.model.config;




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class ImproveFilterPredictorConfig extends ActionComponentConfigMLPredictor {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getImproveFilterPredictorEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getImproveFilterPredictorMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.IMPROVEFILTERPREDICTOR);
    }

}
