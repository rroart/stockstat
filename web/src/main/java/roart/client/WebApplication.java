package roart.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import roart.eureka.util.EurekaUtil;

@SpringBootApplication
@EnableDiscoveryClient
public class WebApplication implements CommandLineRunner {

    @Value("${spring.profiles.active:}")
    private String activeProfile;
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebApplication.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        System.out.println("Using profile " + activeProfile );
        EurekaUtil.initEurekaClient(activeProfile);
    }

}

