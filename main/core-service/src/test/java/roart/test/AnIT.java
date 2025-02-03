package roart.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import roart.core.util.SvgUtil;
import roart.iclij.config.IclijConfig;
import org.springframework.context.annotation.ComponentScan;
import roart.iclij.config.bean.ConfigC;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
@SpringBootTest(classes = { IclijConfig.class, ConfigC.class }) //(classes = ServiceController.class, properties = {"config=stockstat.xml"})
public class AnIT {

    @Autowired
    public IclijConfig iclijConfig;    
    
    @Test
    public void test0() {
        try {
            String sparkmaster = iclijConfig.getMLSparkMaster();
            Integer timeout = iclijConfig.getMLSparkTimeout();
            Object o = new SvgUtil();
        } catch (Exception e) {
            e.printStackTrace();
            }
    }
}
