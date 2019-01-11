package roart.controller;

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

	public static void main(String[] args) throws Exception {
		SpringApplication.run(IclijController.class, args);
	}

	@Override
	public void run(String... args) throws InterruptedException {
        EurekaUtil.initEurekaClient();
        SocketUtil.mylisten();
	}

}
