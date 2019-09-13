package roart.service;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.action.FindProfitAction;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentMLIndicator;
import roart.component.ComponentMLMACD;
import roart.component.ComponentRecommender;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.component.model.MLIndicatorData;
import roart.component.model.MLMACDData;
import roart.config.Market;
import roart.iclij.model.MemoryItem;
import roart.result.model.ResultMeta;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public class MLService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void doMLMACD(ComponentInput componentInput) throws Exception {
        doMLMACD(componentInput, new HashMap<>());
    }

    public List<MemoryItem> doMLMACD(ComponentInput input, Map<String, Object> configValueMap) throws Exception {
        ControlService srv = new ControlService();
        //srv.getConfig();
        //ComponentInput input = new ComponentInput(market, TimeUtil.convertDate(aDate), null, 0, doSave, true);
        ComponentData param = new ComponentData(input);
        param.setService(srv);
        srv.conf.setMarket(input.getMarket());
        srv.conf.getConfigValueMap().putAll(configValueMap);
        //param.setFutureDate(TimeUtil.convertDate(aDate));

        return doMLMACD(param);
    }

    public List<MemoryItem> doMLMACD(ComponentData componentparam) throws Exception {
        long time0 = System.currentTimeMillis();
        Market market = new FindProfitAction().findMarket(componentparam);
        ProfitData profitdata = new ProfitData();
        
        Component component = new ComponentMLMACD();
        ComponentData componentData = component.handle(market, componentparam, profitdata, new ArrayList<>(), false, new HashMap<>());
        componentData.setUsedsec(time0);
        return component.calculateMemory(componentData);

    }
    
    public void doMLIndicator(ComponentInput componentInput) throws Exception {
        doMLIndicator(componentInput, new HashMap<>());
    }

    public List<MemoryItem> doMLIndicator(ComponentInput input, Map<String, Object> configValueMap) throws Exception {
        ControlService srv = new ControlService();
        //srv.getConfig();
        //ComponentInput input = new ComponentInput(market, TimeUtil.convertDate(aDate), null, 0, doSave, true);
        ComponentData param = new ComponentData(input);
        param.setService(srv);
        srv.conf.setMarket(input.getMarket());
        srv.conf.getConfigValueMap().putAll(configValueMap);
        //param.setFutureDate(TimeUtil.convertDate(aDate));

        return doMLIndicator(param);
    }

    public List<MemoryItem> doMLIndicator(ComponentData componentparam) throws Exception {
        long time0 = System.currentTimeMillis();
        Market market = new FindProfitAction().findMarket(componentparam);
        ProfitData profitdata = new ProfitData();

        Component component = new ComponentMLIndicator();
        ComponentData componentData = component.handle(market, componentparam, profitdata, new ArrayList<>(), false, new HashMap<>());
        componentData.setUsedsec(time0);
        return component.calculateMemory(componentData);
    }

}
