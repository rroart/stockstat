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
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
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
            SparkSession spark = SparkSession.builder().master("local[*]").appName("Stockstat Spark").getOrCreate();
            
            //SQLContext sqlContext = new SQLContext(jsc);
            Dataset<Row> df = spark.read().jdbc("jdbc:postgresql://stockstat:password@localhost:5432/stockstat", "meta", null);
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

