package roart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {

    public static void main(String[] args) throws Exception {
	//SpringApplication.run(EurekaApplication.class, args);
	new SpringApplicationBuilder(EurekaApplication.class).web(true).run(args);
    }
}
