package roart.db;

import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.util.Constants;
import scala.Tuple2;
import scala.collection.JavaConversions;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.io.StringReader;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.DataFrame;
// spark2: import org.apache.spark.sql.SparkSession;
// spark2: import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSpark {

    private static Logger log = LoggerFactory.getLogger(DbSpark.class);
    
    public DbSpark() {
	try {
	    String sparkmaster = "spark://127.0.0.1:7077";
	    SparkConf sparkconf = new SparkConf();
 	    String master = sparkmaster;
	    sparkconf.setMaster(master);
	    sparkconf.setAppName("stockstat");
	    // it does not work well with default snappy
	    sparkconf.set("spark.io.compression.codec", "lzf");
	    sparkconf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
	    //sparkconf.set("spark.kryo.registrator", "org.apache.mahout.sparkbindings.io.MahoutKryoRegistrator");
	    String userDir = System.getProperty("user.dir");
	    log.info("user.dir " + userDir);
	    String[] jars = {
		                    "file:" + userDir + "/target/stockstat-web-0.4-SNAPSHOT/WEB-INF/lib/mahout-spark_2.10-0.12.0.jar",
				                        "file:" + userDir + "/target/stockstat-web-0.4-SNAPSHOT/WEB-INF/lib/mahout-hdfs-0.12.0.jar",
				                        "file:" + userDir + "/target/stockstat-web-0.4-SNAPSHOT/WEB-INF/lib/mahout-math-0.12.0.jar",
				                        "file:" + userDir + "/target/stockstat-web-0.4-SNAPSHOT/WEB-INF/lib/guava-16.0.1.jar",
				                        "file:" + userDir + "/target/stockstat-web-0.4-SNAPSHOT/WEB-INF/lib/fastutil-7.0.11.jar",
				                        "file:" + userDir + "/target/stockstat-web-0.4-SNAPSHOT/WEB-INF/lib/mahout-math-scala_2.10-0.12.0.jar",
				                        "file:" + userDir + "/target/stockstat-web-0.4-SNAPSHOT.jar"
	    };
	    // first try without sparkconf.setJars(jars);
	    
	    JavaSparkContext jsc = new JavaSparkContext(sparkconf);
            SQLContext sqlContext = new SQLContext(jsc);
	    DataFrame df = sqlContext.sql("select * from meta");
	    // spark2:            SparkSession spark = SparkSession.builder().master("local[*]").appName("Stockstat Spark").getOrCreate();
            
            // spark2: Dataset<Row> df = spark.read().jdbc("jdbc:postgresql://stockstat:password@localhost:5432/stockstat", "meta", null);
	} catch (Exception e) {
	    log.error(Constants.EXCEPTION, e);
	}

    }

    public static String classify(String content, String language) {
	try {
	    return null;
	} catch (Exception e) {
	    log.error(Constants.EXCEPTION, e);
	}
	return null;
    }

}

