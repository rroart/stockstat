package roart.iclij.config;

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

}
