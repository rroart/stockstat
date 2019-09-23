package roart.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.action.FindProfitAction;
import roart.common.config.ConfigConstants;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentPredictor;
import roart.component.ComponentRecommender;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.component.model.PredictorData;
import roart.config.Market;
import roart.iclij.model.MemoryItem;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PredictorService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void doPredict(ComponentInput componentInput) throws Exception {
        doPredict(componentInput, new HashMap<>());
    }

    public List<MemoryItem> doPredict(ComponentInput componentInput, Map<String, Object> configValueMap) throws Exception {
        ControlService srv = new ControlService();
        ComponentData param = new ComponentData(componentInput);
        param.setBaseDate(componentInput.getEnddate());
        param.setService(srv);
        srv.conf.setMarket(componentInput.getMarket());
        srv.conf.getConfigValueMap().putAll(configValueMap);
        return doPredict(param);
    }

    public List<MemoryItem> doPredict(ComponentData componentparam) throws Exception {
        long time0 = System.currentTimeMillis();

        Market market = new FindProfitAction().findMarket(componentparam);
        ProfitData profitdata = new ProfitData();

        Component component = new ComponentPredictor();
        ComponentData componentData = component.handle(new FindProfitAction(), market, componentparam, profitdata, new ArrayList<>(), false, new HashMap<>());
        componentData.setUsedsec(time0);
        return component.calculateMemory(componentData);

    }

}
