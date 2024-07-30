package roart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.communication.integration.spring.Spring;
import roart.testdata.TestConfiguration;

@SpringJUnitConfig
@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties") 
@ComponentScan(basePackages = "roart.testdata")
@SpringBootTest(classes = TestConfiguration.class)
public class SpringTest2 {

    @Autowired
    private TestConfiguration config;
    
    @Test
    public void t() {
        Spring s = new Spring("SPRING", String.class, "myqueue", new ObjectMapper(), true, true, false, config.getServerSpring(), null);
        s.send("foo");
        String[] foo = s.receiveString();
        System.out.println("footwsr");
        System.out.println(foo[0]);
        //s.simpleRequest();
    }

}