package roart.controller;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import roart.action.Action;
import roart.action.MainAction;
import roart.util.EurekaUtil;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

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
	    Action action = new MainAction();
	    action.goal();
	}

}
