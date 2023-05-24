package roart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.context.annotation.*;

import roart.common.config.ConfigMaps;
import roart.iclij.config.IclijConfig;

@Configuration
//@EnableAutoConfiguration(exclude={JerseyAutoConfiguration.class})
public class MyConfiguration {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Bean
    public ConfigMaps getConfigMaps() {
        return IclijConfig.instanceC();
    }
    
}
