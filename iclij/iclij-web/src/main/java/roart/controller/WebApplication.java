package roart.controller;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

@SpringBootApplication
@EnableDiscoveryClient
public class WebApplication {

        public static void main(String[] args) throws Exception {
                SpringApplication.run(WebApplication.class, args);
        }

        /*
        @Bean(name = "OBJECT_MAPPER_BEAN")
        public ObjectMapper jsonObjectMapper() {
            return Jackson2ObjectMapperBuilder.json()
                    .serializationInclusion(JsonInclude.Include.NON_NULL) // Don’t include null values
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //ISODate
                    .modules(new JSR310Module())
                    .build();
        }
*/
}

