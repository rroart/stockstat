package roart.iclij.config.bean;

import org.springframework.context.annotation.Bean;

import roart.common.config.ConfigMaps;
import roart.iclij.config.IclijConfig;

public class ConfigI {

    @Bean
    public ConfigMaps getConfigMaps() {
        return IclijConfig.instanceI();
    }
}
