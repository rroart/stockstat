package roart.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import roart.eureka.util.EurekaUtil;

@SpringBootApplication
@EnableDiscoveryClient
public class WebApplication implements CommandLineRunner {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebApplication.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        EurekaUtil.initEurekaClient();
    }

}

