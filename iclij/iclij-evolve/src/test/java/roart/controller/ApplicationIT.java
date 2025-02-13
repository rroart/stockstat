package roart.controller;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationIT {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Test
    public void test() throws Exception {
        log.info("Start");
        Thread.sleep(10000);
        log.info("End");
    }
}
