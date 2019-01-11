package roart.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.eureka.util.EurekaUtil;

@SpringBootApplication
@EnableDiscoveryClient
public class WebApplication implements CommandLineRunner {

        // this is not running
    
        public static void main(String[] args) throws Exception {
                SpringApplication.run(WebApplication.class, args);
        }

        @Override
        public void run(String... args) throws InterruptedException {
            EurekaUtil.initEurekaClient();
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

