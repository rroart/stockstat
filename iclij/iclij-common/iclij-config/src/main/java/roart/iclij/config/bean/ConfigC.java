package roart.iclij.config.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import roart.common.config.ConfigMaps;
import roart.iclij.config.IclijConfig;

@Configuration
public class ConfigC {

    @Bean
    public ConfigMaps getConfigMaps() {
        return IclijConfig.instanceC();
    }
}
