package roart.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import roart.eureka.util.EurekaUtil;
import roart.parse.SocketUtil;

@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class IclijController implements CommandLineRunner {

    @Value("${spring.profiles.active:}")
    private String activeProfile;
    
	public static void main(String[] args) throws Exception {
		SpringApplication.run(IclijController.class, args);
	}

	@Override
	public void run(String... args) throws InterruptedException {
            System.out.println("Using profile " + activeProfile);
        SocketUtil.mylisten();
	}

}
