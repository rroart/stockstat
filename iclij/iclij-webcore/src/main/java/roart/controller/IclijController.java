package roart.controller;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.eureka.util.EurekaUtil;
import roart.executor.MyExecutors;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.service.ControlService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.RestController;

@ComponentScan(basePackages = "roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository")
@EnableJdbcRepositories("roart.common.springdata.repository")
@EnableDiscoveryClient
@SpringBootApplication
public class IclijController implements CommandLineRunner {

    @Value("${spring.profiles.active:}")
    private String activeProfile;
    
	public static void main(String[] args) throws Exception {
		SpringApplication.run(IclijController.class, args);
	}

	@Override
	public void run(String... args) throws InterruptedException, JsonParseException, JsonMappingException, IOException {	    
	    System.out.println("Using profile " + activeProfile);
	    try {
	        MyExecutors.initThreads("dev".equals(activeProfile));
            MyExecutors.init(new double[] { IclijXMLConfig.getConfigInstance().mpServerCpu() } );
	    } catch (Exception e) {
                e.printStackTrace();
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
