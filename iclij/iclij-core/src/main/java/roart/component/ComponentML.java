package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.constants.ResultMetaConstants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.NeuralNetTensorflowConfig;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentMLData;
import roart.component.model.MLMACDData;
import roart.constants.IclijConstants;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.util.MLUtil;
import roart.iclij.util.MiscUtil;
import roart.result.model.ResultItem;
import roart.result.model.ResultMeta;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public abstract class ComponentML extends Component {

    @Override
    protected Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param, String subcomponent, Map<String, Object> scoreMap, String mlmarket, Parameters parameters) {
        // special
        //String localMl = param.getInput().getConfig().getFindProfitMLIndicatorMLConfig();
        Map<String, EvolveMLConfig> mlConfigMap = getConfig().getMLConfig(market, param.getInput().getConfig(), mlmarket);
        // part special
        // if (param.getInput().getConfig().wantEvolveML()) {
        if (evolve) {
            EvolutionConfig evolveConfig = getEvolutionConfig(param);
            String newConfStr = JsonUtil.convert(evolveConfig);
            param.getService().conf.getConfigValueMap().put(ConfigConstants.EVOLVEMLEVOLUTIONCONFIG, newConfStr);

            // We do not need this with the other subcomp settings?
            //Map<String, Object> evolveMap = setnns(param.getService().conf, param.getInput().getConfig(), mlConfigMap, true);
            //param.getService().conf.getConfigValueMap().putAll(evolveMap);
            Map<String, Object> anUpdateMap = new HashMap<>();
            Map<String, Object> aScoreMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            param.getService().conf.setdate(TimeUtil.convertDate(param.getFutureDate()));
            List<ResultItem> retlist = param.getService().getEvolveML(true, param.getDisableList(), pipeline, param.getService().conf, anUpdateMap, aScoreMap, resultMap);
            mlSaves(mlConfigMap, param, anUpdateMap, subcomponent, parameters);
            if (param.getUpdateMap() != null) {
                param.getUpdateMap().putAll(anUpdateMap); 
            }
            if (scoreMap != null) {
                scoreMap.putAll(aScoreMap);
            }
            param.setResultMap(resultMap);
            return new HashMap<>(); //evolveMap;
        }
        return new HashMap<>();
        //Map<String, Object> i = setnns(param.getService().conf, param.getInput().getConfig(), mlConfigMap, false);
    }

    private void mlSaves(Map<String, EvolveMLConfig> mlConfigMap, ComponentData param, Map<String, Object> anUpdateMap, String subcomponent, Parameters parameters) {
        //for (Entry<String, Object> entry : anUpdateMap.entrySet()) {
            String key = IclijConstants.ALL;
            String nnconfigString = JsonUtil.convert(anUpdateMap);
            if (anUpdateMap.size() == 1) {
            	key = anUpdateMap.keySet().iterator().next();
            	nnconfigString = (String) anUpdateMap.get(key);
            }
            //Map<String, String> mapToConfig = MLConfigs.getMapToConfig();
            /*
            NeuralNetConfigs nnConfigs = null;
            try {
                if (getPipeline().equals(PipelineConstants.PREDICTORSLSTM)) {
                    nnConfigs = new NeuralNetConfigs();
                    ObjectMapper mapper = new ObjectMapper();
                    TensorflowPredictorLSTMConfig nnConfig = mapper.readValue(nnconfigString, TensorflowPredictorLSTMConfig.class);
                    nnConfigs.setTensorflowConfig(new NeuralNetTensorflowConfig(null, null, null, null, null, null, null, null, nnConfig));
                    //nnConfigs.getTensorflowConfig().setTensorflowPredictorLSTMConfig(nnConfig);                
                } else {
                    ObjectMapper mapper = new ObjectMapper();
                    nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);            
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            for (Entry<String, EvolveMLConfig> entry2 : mlConfigMap.entrySet()) {
                String key2 = entry2.getKey();
                EvolveMLConfig config = entry2.getValue();
                if (!config.getSave()) {
                    if (nnConfigs.getAndSet(key2) != null) {
                        log.error("Key {} not null", key2);
                    }
                    nnConfigs.set(key2, null);
                }
            }
            */
            String value = nnconfigString;
            /*
            try {
                if (getPipeline().equals(PipelineConstants.PREDICTORSLSTM)) {
                    TensorflowPredictorLSTMConfig nnConfig = nnConfigs.getTensorflowConfig().getTensorflowPredictorLSTMConfig();
                    value = JsonUtil.convert(nnConfig);
                } else {
                    value = JsonUtil.convert(nnConfigs);
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            */
            if (value == null) {
                log.error("Config value null");
                return;
            }
            ConfigItem configItem = new ConfigItem();
            configItem.setAction(param.getAction());
            configItem.setComponent(getPipeline());
            configItem.setDate(param.getBaseDate());
            configItem.setId(key);
            configItem.setMarket(param.getMarket());
            configItem.setRecord(LocalDate.now());
            configItem.setSubcomponent(subcomponent);
            configItem.setParameters(JsonUtil.convert(parameters));
            configItem.setValue(nnconfigString);
            try {
                configItem.save();
            } catch (Exception e) {
                log.info(Constants.EXCEPTION, e);
            }
        //}
    }

    @Override
    protected Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market, String action, Boolean buy, String subcomponent, String mlmarket, MarketActionData actionData, Parameters parameters) throws Exception {
        Map<String, EvolveMLConfig> mlConfigMap = getConfig().getMLConfig(market, param.getInput().getConfig(), mlmarket);
        return mlLoads(mlConfigMap, param, anUpdateMap, market, action, buy, subcomponent, mlmarket, actionData, parameters);
    }

    protected Map<String, Object> mlLoads(Map<String, EvolveMLConfig> mlConfigMap, ComponentData param, Map<String, Object> anUpdateMap, Market market, String action, Boolean buy, String subcomponent, String mlmarket, MarketActionData actionData, Parameters parameters) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (mlConfigMap == null) {
            return map;
        }
        for (Entry<String, EvolveMLConfig> entry : mlConfigMap.entrySet()) {
            String key = entry.getKey();
            EvolveMLConfig config = entry.getValue();
            if (config.getLoad()) {
                String marketName = market.getConfig().getMarket();
                String component = getPipeline();
                if (mlmarket != null) {
                    marketName = mlmarket;
                }
                Map<String, Object> configMap  = new MiscUtil().loadConfig(param.getService(), param.getInput(), market, marketName, action, component, false, buy, subcomponent, actionData, parameters);
                map.putAll(configMap);
            }
        }
        return map;
    }

    @Override
    public EvolutionConfig getEvolutionConfig(ComponentData param) {
        String confStr = param.getInput().getConfig().getEvolveMLEvolutionConfig();
        EvolutionConfig evolveConfig = JsonUtil.convert(confStr, EvolutionConfig.class);
        EvolutionConfig localEvolveConfig = JsonUtil.convert(getConfig().getLocalEvolutionConfig(param.getInput().getConfig()), EvolutionConfig.class);
        evolveConfig.merge(localEvolveConfig);
        return evolveConfig;
    }

    @Override
    public void handleMLMeta(ComponentData componentparam, Map<String, List<Object>> mlMaps) {
        if (mlMaps == null) {
            return;
        }
        ComponentMLData param = (ComponentMLData) componentparam;
        List<List> resultMetaArray = (List) mlMaps.get(PipelineConstants.RESULTMETAARRAY);
        param.setResultMetaArray(resultMetaArray);
        //List<ResultMeta> resultMeta = (List<ResultMeta>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<Object> objectList = (List<Object>) mlMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        param.setResultMeta(resultMeta);
    }

    public static Map<String, Object> setnns(MyMyConfig conf, IclijConfig config, Map<String, EvolveMLConfig> mlConfigMap, boolean useEvolve) {
        Map<String, Object> returnmap = new HashMap<>();
        Map<String, String> map = config.getConv();
        for (Entry<String, EvolveMLConfig> entry : mlConfigMap.entrySet()) {
            String key = entry.getKey();
            EvolveMLConfig value = entry.getValue();
            boolean myenable = Boolean.TRUE.equals(value.getEnable());
            if (useEvolve) {
                boolean evolve = Boolean.TRUE.equals(value.getEvolve());
                myenable = myenable && evolve;
            }
            //System.out.println(conf.getConfigValueMap().keySet());
            Object o = conf.getValueOrDefault(key);
            boolean enable = (boolean) conf.getValueOrDefault(key);
            String otherKey = map.get(key);
            returnmap.put(key, myenable);
            if (otherKey != null) {
                returnmap.put(otherKey, enable);
            }
        }
        return returnmap;
    }

    @Override
    protected EvolutionConfig getImproveEvolutionConfig(IclijConfig config) {
        /*
        ObjectMapper mapper = new ObjectMapper();
        EvolutionConfig evolutionConfig = null;
        try {
            evolutionConfig = mapper.readValue(conf.getTestMLEvolutionConfig(), EvolutionConfig.class);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
         */
        String evolveString = config.getEvolveMLEvolutionConfig();
        return JsonUtil.convert(evolveString, EvolutionConfig.class);
    }

    @Override
    public boolean wantEvolve(IclijConfig config) {
        return config.wantEvolveML();
    }

    @Override
    public boolean wantImproveEvolve() {
        return false;
    }

    @Override
    public List<String>[] enableDisable(ComponentData param, Memories positions, Boolean above) {
        /*
        if (positions == null || positions.isEmpty()) {
            return new ArrayList[] { new ArrayList<String>(), new ArrayList<String>() };
        }
        */
        List[] list = new ArrayList[2];
        List<String> enable = new ArrayList<>();
        Map<Pair<String, String>, String> map = getMap();
        Map<Pair<String, String>, String> mapPersist = getMapPersist();
        ComponentMLData paramML = (ComponentMLData) param;
        List<List> resultMetas = paramML.getResultMetaArray();
        int count = 0;
        if (resultMetas != null) {
            for (List meta : resultMetas) {
                boolean emptyMeta = meta.get(ResultMetaConstants.MLNAME) == null;
                
                if (emptyMeta) {
                    count++;                
                    continue;
                }
                
                if (positions == null) {
                    int jj = 0;
                }
                
                Pair<String, String> paircount = new MiscUtil().getComponentPair(meta);

                Pair<String, String> pair = new MiscUtil().getSubComponentPair(meta);
                String cnf = map.get(pair);
                String cnfPersist = mapPersist.get(pair);

                if (positions == null || positions.containsBelow(getPipeline(), paircount, above, null, false)) {
                    if (cnf == null) {
                        continue;
                    }
                    if (!enable.contains(cnf)) {
                        enable.add(cnf);
                    }
                    if (!enable.contains(cnfPersist)) {
                        enable.add(cnfPersist);
                    }
                    map.remove(pair);
                }
                count++;        
            }
        } else {
            int jj = 0;
        }
        list[0] = enable;
        list[1] = new ArrayList<String>(map.values());
        return list;
    }

    @Override
    protected void subenable(Map<String, Object> valueMap, String subcomponent) {
        String[] pairArray = subcomponent.split(" ");
        Pair<String, String> pair = new ImmutablePair(pairArray[0], pairArray[1]);
        Map<Pair<String, String>, String> map = getMap();
        String key = map.get(pair);
        /*
        if (!map.containsKey(key)) {
            log.error("Key not found {}", key);
            return;
        }
        */
        // in case it does not exist with the predictor
        if (key == null) {
            return;
        }
        valueMap.put(key, Boolean.TRUE);
        String mlKey = new NeuralNetConfigs().getAnotherConfigMap().get(key);
        valueMap.put(mlKey, Boolean.TRUE);
    }

    @Override
    protected void subdisable(Map<String, Object> valueMap, String subcomponent) {
        String[] pair = subcomponent.split(" ");
        String ml = pair[0];
        Map<String, String> map = new MLUtil().getMlMap();
        if (!map.containsKey(ml)) {
            log.error("Key not found {}", ml);
            return;
        }
        // disable all major MLs except our
        // BUT: we are not predictor
        //valueMap.put(map.get(ml), Boolean.TRUE);
        //map.remove(ml);
        for (Entry<String, String> entry : map.entrySet()) {
            String disableKey = entry.getValue();
            valueMap.put(disableKey, Boolean.FALSE);
        }
        Map<String, String> mapPredictor = new MLUtil().getMlMap();
        for (Entry<String, String> entry : mapPredictor.entrySet()) {
            String disableKey = entry.getValue();
            valueMap.put(disableKey, Boolean.FALSE);
        }
        // disable all for the others
        for (String otherKey : getOtherList()) {
            valueMap.put(otherKey, Boolean.FALSE);
        }
        // disable all for our major
        Map<Pair<String, String>, String> fullMap = getMap();
        for (Entry<Pair<String, String>, String> entry : fullMap.entrySet()) {
            Pair<String, String> aPair = entry.getKey();
            if (true || ml.equals(aPair.getLeft())) {
                valueMap.put(entry.getValue(), Boolean.FALSE);
            }
        }
    }

    protected String withComma(Object obj) {
        String str = (String) obj;
        if (str == null || str.length() == 0) {
            return "";
        } else {
            return ", " + str;
        }
    }
    
    private Map<String, String> getShortMap() {
        Map<String, String> map = new HashMap<>();
        map.put(MLConstants.SPARK, MLConstants.SP);
        map.put(MLConstants.TENSORFLOW, MLConstants.TF);
        map.put(MLConstants.PYTORCH, MLConstants.PT);
        return map;
    }
    
    public String getShort(String str) {
        String shortStr = getShortMap().get(str);
        if (shortStr == null) {
            return str;
        } else {
            return shortStr;
        }
    }

    /**
     * 
     * @return Map of pair of (major minor) to config key
     */
    
    protected Map<Pair<String, String>, String> getMap() {
        return getConfig().getMLMaps().getMap();
    }

    /**
     * 
     * @return List of config keys for the other type of ML (ordinary/predictor)
     */
    
    protected List<String> getOtherList() {
        return getConfig().getMLMaps().getOtherList();
    }

    protected Map<Pair<String, String>, String> getMapPersist() {
        return getConfig().getMLMaps().getMapPersist();
    }

    protected MLMetricsItem search(List<MLMetricsItem> mlTests, List meta) {
        Pair<String, String> pair = new MiscUtil().getComponentPair(meta);
        if (mlTests == null) {
            MLMetricsItem test = new MLMetricsItem();
            test.setComponent(getPipeline());
            test.setSubcomponent(pair.getLeft());
            test.setLocalcomponent(pair.getRight());
            test.setTestAccuracy(1.0);
            return test;
        }
        for (MLMetricsItem aTest : mlTests) {
            if (!aTest.getComponent().equals(getPipeline())) {
                continue;
            }
            if (aTest.getSubcomponent().equals(pair.getLeft())) {
                if (aTest.getLocalcomponent() == null || aTest.getLocalcomponent().equals(pair.getRight())) {
                    return aTest;
                }
            }
        }
        return null;
    }

}
