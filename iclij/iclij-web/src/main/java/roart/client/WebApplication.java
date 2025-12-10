package roart.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
importtools.jackson.datatype.javatime.JSR310Module;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import roart.eureka.util.EurekaUtil;

@SpringBootApplication
@EnableDiscoveryClient
public class WebApplication implements CommandLineRunner {

        // this is not running
    @Value("${spring.profiles.active:}")
    private String activeProfile;
    
        public static void main(String[] args) throws Exception {
                SpringApplication.run(WebApplication.class, args);
        }

        @Override
        public void run(String... args) throws InterruptedException {
            System.out.println("Using profile " + activeProfile);
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

