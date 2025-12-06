package roart.spark;

import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import roart.iclij.config.IclijConfig;
import org.springframework.context.annotation.ComponentScan;
import roart.iclij.config.bean.ConfigC;
import roart.spark.util.SparkUtil;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
@SpringBootTest(classes = { IclijConfig.class, ConfigC.class }) //(classes = ServiceController.class, properties = {"config=stockstat.xml"})
public class SparkIT {

    @Autowired
    public IclijConfig iclijConfig;    
    
    @Test
    public void test0() {
        try {
            String sparkmaster = iclijConfig.getMLSparkMaster();
            Integer timeout = iclijConfig.getMLSparkTimeout();
            SparkSession spark = SparkUtil.createSparkSession(sparkmaster, "Stockstat ML", timeout);
            //System.out.println(spark.logName());
        } catch (Exception e) {
            e.printStackTrace();
            }
    }

    //@Test
    public void test() {
        try {
            MLClassifySparkDS b = new MLClassifySparkDS(null);
        MLClassifySparkDS a = new MLClassifySparkDS(iclijConfig);
        a.clean();
        } catch (Exception e) {
            e.printStackTrace();
            }
    }
}
