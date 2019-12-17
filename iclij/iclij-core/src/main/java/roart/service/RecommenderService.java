package roart.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.FindProfitAction;
import roart.common.config.ConfigConstants;
import roart.common.constants.RecommendConstants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentRecommender;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.component.model.RecommenderData;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.MemoryItem;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public class RecommenderService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void doRecommender(ComponentInput componentInput) throws Exception {
        doRecommender(componentInput, new ArrayList<>());
    }

    public List<MemoryItem> doRecommender(ComponentInput componentInput, List<String> disableList) throws Exception {
        ControlService srv = new ControlService();
        //srv.getConfig();
        //srv.conf.getConfigValueMap().putAll(configValueMap);
        ComponentData param = new ComponentData(componentInput);
        param.setService(srv);
        srv.conf.setMarket(componentInput.getMarket());
        param.setBaseDate(componentInput.getEnddate());
        return doRecommender(param, disableList);
    }

    public List<MemoryItem> doRecommender(ComponentData componentparam, List<String> disableList) throws Exception {
        long time0 = System.currentTimeMillis();
        Market market = new FindProfitAction().findMarket(componentparam);
        ProfitData profitdata = new ProfitData();

        Component component = new ComponentRecommender();
        ComponentData componentData = component.handle(new FindProfitAction(), market, componentparam, profitdata, new ArrayList<>(), false, new HashMap<>());
        componentData.setUsedsec(time0);
        return component.calculateMemory(componentData, null);
    }

}
