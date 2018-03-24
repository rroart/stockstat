package roart.controller;

import java.io.IOException;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.action.Action;
import roart.action.MainAction;
import roart.config.IclijXMLConfig;
import roart.queue.MyExecutors;
import roart.service.ControlService;
import roart.util.EurekaUtil;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class IclijController implements CommandLineRunner {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(IclijController.class, args);
	}

	@Override
	public void run(String... args) throws InterruptedException, JsonParseException, JsonMappingException, IOException {	    
	    EurekaUtil.initEurekaClient();
            MyExecutors.init();
            if (IclijXMLConfig.getConfigInstance().wantsAutorun()) {        
                Action action = new MainAction();
                action.goal(null);
            }
	}

	@Bean(name = "OBJECT_MAPPER_BEAN")
	public ObjectMapper jsonObjectMapper() {
	    return Jackson2ObjectMapperBuilder.json()
	            .serializationInclusion(JsonInclude.Include.NON_NULL)
	            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
	            .modules(new JavaTimeModule())
	            .build();
	}

}
