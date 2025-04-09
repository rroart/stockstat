package roart.iclij.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import roart.common.config.ConfigConstants;

public class ConfigUtils {

    public List<String> getIndicators() {
        return List.of(ConfigConstants.INDICATORSATR,               
                ConfigConstants.INDICATORSCCI,            
                ConfigConstants.INDICATORSMACD,      
                ConfigConstants.INDICATORSRSI,          
                ConfigConstants.INDICATORSSTOCH,       
                ConfigConstants.INDICATORSSTOCHRSI);                
    }

    public Map<String, Boolean> getIndicators(Boolean bool) {
        return getIndicators()
                .stream()
                .collect(Collectors.toMap( 
                        value -> value, value -> bool));
    }

    public List<String> getMLComponentConfigList() {
        List<String> list = new ArrayList<>();
        list.addAll(getComponentMLATRConfigList());
        list.addAll(getComponentMLCCIConfigList());
        list.addAll(getComponentMLMACDConfigList());
        list.addAll(getComponentMLRSIConfigList());
        list.addAll(getComponentMLStochConfigList());
        list.addAll(getComponentMLMultiConfigList());
        list.addAll(getComponentMLMultiOHLCConfigList());
        list.addAll(getComponentMLIndicatorConfigList());
        list.addAll(getComponentMLIndicatorOHLCConfigList());
        list.addAll(getComponentPredictorConfigList());
        return list;
    }
    
    public List<String> getComponentMLATRConfigList() {
        List<String> confList = new ArrayList<>();
        confList.add(ConfigConstants.AGGREGATORSMLATRDAYSAFTERLIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLATRDAYSBEFORELIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLATRBUYLIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLATRSELLLIMIT);
        //confList.add(ConfigConstants.AGGREGATORSMLATRTHRESHOLD);
        return confList;
    }

    public List<String> getComponentMLCCIConfigList() {
        List<String> confList = new ArrayList<>();
        confList.add(ConfigConstants.AGGREGATORSMLCCIDAYSAFTERLIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLCCIDAYSBEFORELIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLCCIBUYLIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLCCISELLLIMIT);
        //confList.add(ConfigConstants.AGGREGATORSMLCCITHRESHOLD);
        return confList;
    }

    public List<String> getComponentMLMACDConfigList() {
        List<String> confList = new ArrayList<>();
        confList.add(ConfigConstants.AGGREGATORSMLMACDDAYSAFTERZERO);
        confList.add(ConfigConstants.AGGREGATORSMLMACDDAYSBEFOREZERO);
        confList.add(ConfigConstants.AGGREGATORSMLMACDHISTOGRAMML);
        confList.add(ConfigConstants.AGGREGATORSMLMACDMACDML);
        confList.add(ConfigConstants.AGGREGATORSMLMACDSIGNALML);
        //confList.add(ConfigConstants.AGGREGATORSMLMACDTHRESHOLD);
        return confList;
    }

    public List<String> getComponentMLRSIConfigList() {
        List<String> confList = new ArrayList<>();
        confList.add(ConfigConstants.AGGREGATORSMLRSIDAYSAFTERLIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSIDAYSBEFORELIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSIBUYRSILIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSIBUYSRSILIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSISELLRSILIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSISELLSRSILIMIT);
        return confList;
    }

    public List<String> getComponentMLStochConfigList() {
        List<String> confList = new ArrayList<>();
        confList.add(ConfigConstants.AGGREGATORSMLSTOCHDAYSAFTERLIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLSTOCHDAYSBEFORELIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLSTOCHBUYLIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLSTOCHSELLLIMIT);
        //confList.add(ConfigConstants.AGGREGATORSMLSTOCHTHRESHOLD);
        return confList;
    }

    public List<String> getComponentMLMultiConfigList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSMLMULTIMACD);
        list.add(ConfigConstants.AGGREGATORSMLMULTIRSI);
        list.add(ConfigConstants.AGGREGATORSMLMULTISTOCHRSI);
        //list.add(ConfigConstants.AGGREGATORSMLMACDDAYSBEFOREZERO);
        //list.add(ConfigConstants.AGGREGATORSMLMACDDAYSAFTERZERO);
        //list.add(ConfigConstants.AGGREGATORSMLRSIDAYSBEFORELIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLRSIDAYSAFTERLIMIT);
        list.add(ConfigConstants.AGGREGATORSMLMULTIDAYSBEFORELIMIT);
        list.add(ConfigConstants.AGGREGATORSMLMULTIDAYSAFTERLIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLMULTITHRESHOLD);
        return list;
    }

    public List<String> getComponentMLMultiOHLCConfigList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSMLMULTIATR);
        list.add(ConfigConstants.AGGREGATORSMLMULTICCI);
        list.add(ConfigConstants.AGGREGATORSMLMULTISTOCH);
        //list.add(ConfigConstants.AGGREGATORSMLATRDAYSBEFORELIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLATRDAYSAFTERLIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLCCIDAYSBEFORELIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLCCIDAYSAFTERLIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLSTOCHDAYSBEFORELIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLSTOCHDAYSAFTERLIMIT);
        return list;
    }

    public List<String> getComponentMLIndicatorConfigList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSINDICATORMACD);
        list.add(ConfigConstants.AGGREGATORSINDICATORRSI);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASDELTAS);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASMACD);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASRSI);
        list.add(ConfigConstants.AGGREGATORSINDICATORINTERVALDAYS);
        list.add(ConfigConstants.AGGREGATORSINDICATORFUTUREDAYS);
        list.add(ConfigConstants.AGGREGATORSINDICATORTHRESHOLD);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASLIST);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASBITS);
        return list;
    }
    
    public List<String> getComponentMLIndicatorOHLCConfigList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSINDICATORATR);
        list.add(ConfigConstants.AGGREGATORSINDICATORCCI);
        list.add(ConfigConstants.AGGREGATORSINDICATORSTOCH);
        list.add(ConfigConstants.AGGREGATORSINDICATORSTOCHRSI);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASATR);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASCCI);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASSTOCH);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASSTOCHRSI);
        return list;
    }
    
    public List<String> getComponentPredictorConfigList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.MACHINELEARNINGPREDICTORSDAYS);
        list.add(ConfigConstants.MACHINELEARNINGPREDICTORSFUTUREDAYS);
        //list.add(ConfigConstants.MACHINELEARNINGPREDICTORSTHRESHOLD);
        return list;
    }
}
