package roart.controller;

import java.util.ArrayList;
import java.util.HashMap;

import roart.iclij.config.IclijConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.model.io.IO;
//import roart.webcore.util.ServiceUtil;

public class TestUtils2 {

    private IclijConfig iclijConfig;
    private IO io;

    public TestUtils2(IclijConfig iconf, IO io) {
        this.iclijConfig = iconf;
        this.io = io;
    }

    /*
    public IclijServiceResult getVerify(IclijServiceParam param)
            throws Exception {
        return ServiceUtil.getVerify(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    public IclijServiceResult getFindProfitMarket(IclijServiceParam param)
            throws Exception {
        return ServiceUtil.getFindProfit(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    public IclijServiceResult getImproveAboveBelowMarket(IclijServiceParam param)
            throws Exception {
        return ServiceUtil.getImproveAboveBelow(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    public IclijServiceResult getImproveProfitMarket(IclijServiceParam param)
            throws Exception {
        return ServiceUtil.getImproveProfit(new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io, iclijConfig);
    }
*/
}
