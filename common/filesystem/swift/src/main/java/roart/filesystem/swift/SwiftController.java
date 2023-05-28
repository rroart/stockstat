package roart.filesystem.swift;

import roart.iclij.config.IclijConfig;
import roart.common.config.ConfigData;
import roart.filesystem.FileSystemAbstractController;
import roart.filesystem.FileSystemOperations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class SwiftController extends FileSystemAbstractController {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SwiftController.class, args);
	}

	@Override
	protected FileSystemOperations createOperations(String nodename, String configid, ConfigData nodeConf) {
		return new Swift(nodename, configid, nodeConf);
	}
}
