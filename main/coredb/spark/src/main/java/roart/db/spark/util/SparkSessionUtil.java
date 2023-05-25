package roart.db.spark.util;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkSessionUtil {

    private static Logger log = LoggerFactory.getLogger(SparkSessionUtil.class);

    public static SparkSession createSparkSession(String sparkmaster, String appName, Integer timeout) {
        String myAppName = "stockstat";
        SparkConf sparkconf = new SparkConf();
        String master = sparkmaster;
        sparkconf.setMaster(master);
        sparkconf.setAppName(myAppName);
        // it does not work well with default snappy
        if (timeout != null) {
            sparkconf.set("spark.network.timeout", "" + timeout);
        }
        sparkconf.set("spark.io.compression.codec", "lzf");
        sparkconf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        String sparkDriverHost = System.getProperty("SPARK_DRIVER_HOST");
        if (sparkDriverHost != null) {
            sparkconf.set("spark.driver.host", sparkDriverHost);
        }
        String userDir = System.getProperty("user.dir");
        log.info("user.dir " + userDir);
        //SparkSession i = new SparkSession();
        return SparkSession
                .builder()
                .master(sparkmaster)
                .appName(myAppName)
                .config(sparkconf)
                .getOrCreate();
    }
}
