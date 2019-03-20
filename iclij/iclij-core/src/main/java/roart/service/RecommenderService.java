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
        srv.getConfig();
        srv.conf.setMarket(componentInput.getMarket());
        //srv.conf.getConfigValueMap().putAll(configValueMap);
        ComponentData param = new ComponentData(componentInput);
        param.setService(srv);
        param.setBaseDate(componentInput.getEnddate());
        return doRecommender(param, disableList);
    }

    public List<MemoryItem> doRecommender(ComponentData componentparam, List<String> disableList) throws Exception {
        long time0 = System.currentTimeMillis();
        Market market = FindProfitAction.findMarket(componentparam);
        ProfitData profitdata = new ProfitData();

        Component component = new ComponentRecommender();
        ComponentData componentData = component.handle(market, componentparam, profitdata, new ArrayList<>(), false);
        componentData.setUsedsec(time0);
        return component.calculate2(componentData);

        /*
        RecommenderData param = new RecommenderData(componentparam);
        int futuredays = (int) param.getService().conf.getTestIndicatorRecommenderComplexFutureDays();
        param.setFuturedays(futuredays);
        param.setDates(param.getService(), futuredays, param.getLoopoffset(), TimeUtil.convertDate2(param.getFutureDate()));
    
        param.getService().conf.setdate(TimeUtil.convertDate(param.getBaseDate()));
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        if (instance.wantEvolveRecommender()) {
            param.getService().getEvolveRecommender(true, disableList);
        }
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map recommendMaps = (Map) param.getResultMap(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, setValueMap);
        if (recommendMaps == null) {
            return null;
        }
        param.setCategory(recommendMaps);
        Map<String, Map<String, List<Double>>> resultMap = (Map<String, Map<String, List<Double>>>) recommendMaps.get(PipelineConstants.RESULT);
        //System.out.println("m4 " + resultMap.keySet());
        if (resultMap == null) {
            return null;
        }
        Map<String, List<Double>> recommendBuySell = resultMap.get(RecommendConstants.COMPLEX);
        param.setRecommendBuySell(recommendBuySell);
        
        param.getAndSetCategoryValueMap();
        //System.out.println("k2 " + categoryValueMap.keySet());
        param.setUsedsec(time0);
        */
    }

}
