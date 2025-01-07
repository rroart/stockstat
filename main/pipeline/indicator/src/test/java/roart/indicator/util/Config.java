package roart.indicator.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import roart.common.config.ConfigMaps;
import roart.iclij.config.IclijConfig;

@Configuration
public class Config {

    @Bean
    public ConfigMaps getConfigMaps() {
        return IclijConfig.instanceC();
    }
 }
