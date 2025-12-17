package roart.controller;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import roart.common.constants.Constants;
import roart.eureka.util.EurekaUtil;
import roart.executor.MyExecutors;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.service.ControlService;
import roart.model.io.IO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@EnableJdbcRepositories("roart.common.springdata.repository")
@EnableDiscoveryClient
@SpringBootApplication
public class IclijController implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iclijConfig;
    
    @Autowired
    private IO io;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(IclijController.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException, StreamReadException, DatabindException, IOException {	    
        log.info("Using profile {}", activeProfile);
        IclijConfig instance = iclijConfig;
        try {
            MyExecutors.initThreads("dev".equals(activeProfile));
            MyExecutors.init(new double[] { instance.mpServerCpu() } );
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Bean(name = "OBJECT_MAPPER_BEAN")
    public ObjectMapper jsonObjectMapper() {
	return JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }

}
