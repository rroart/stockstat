package roart.spark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roart.controller.ServiceController;
import roart.iclij.config.IclijConfig;

@SpringBootTest(classes = ServiceController.class, properties = {"config=stockstat.xml"})
public class SparkIT {

    @Autowired
    public IclijConfig iclijConfig;    
    
    @Test
    public void test() {
        try {
        MLClassifySparkAccess a = new MLClassifySparkAccess(iclijConfig);
        a.clean();
        } catch (Exception e) {
            e.printStackTrace();
            }
    }
}
