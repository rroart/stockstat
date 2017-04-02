package roart.db;

import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.config.MyPropertyConfig;
import roart.model.MetaItem;
import roart.model.StockItem;
import roart.util.Constants;
import scala.Tuple2;
import scala.collection.JavaConversions;

import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.io.StringReader;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
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

	private static SparkSession spark;
	private static Properties prop;

	public DbSpark() {
		try {
			String sparkmaster = "spark://127.0.0.1:7077";
			//sparkmaster = MyPropertyConfig.instance().sparkMaster;
			SparkConf sparkconf = new SparkConf();
			String master = sparkmaster;
			sparkconf.setMaster(master);
			sparkconf.setAppName("stockstat");
			// it does not work well with default snappy
			sparkconf.set("spark.io.compression.codec", "lzf");
			sparkconf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
			sparkconf.set("spark.driver.extraClassPath", "/home/roart/.m2/repository/postgresql/postgresql/9.1-901-1.jdbc4/postgresql-9.1-901-1.jdbc4.jar" );
			sparkconf.set("spark.executor.extraClassPath", "/home/roart/.m2/repository/postgresql/postgresql/9.1-901-1.jdbc4/postgresql-9.1-901-1.jdbc4.jar" );
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

			spark = SparkSession
					.builder()
					.master(sparkmaster)
					.appName("Aether")
					.config(sparkconf)
					.getOrCreate();

			//JavaSparkContext jsc = new JavaSparkContext(sparkconf);
			//SQLContext sqlContext = new SQLContext(jsc);
			//Dataset<Row> df = sqlContext.sql("select * from meta");
			// spark2:            SparkSession spark = SparkSession.builder().master("local[*]").appName("Stockstat Spark").getOrCreate();

			// spark2: Dataset<Row> df = spark.read().jdbc("jdbc:postgresql://stockstat:password@localhost:5432/stockstat", "meta", null);
			prop = new java.util.Properties();
			prop.setProperty("driver", "org.postgresql.Driver");
		} catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
		}

	}

	public static List<StockItem> getAll(String market) throws Exception {
		long time0 = System.currentTimeMillis();
		List<StockItem> retList = new ArrayList<>();
		Dataset<Row> allstocks = spark.read().jdbc("jdbc:postgresql://localhost:5432/stockstat?user=stockstat&password=password", "stock", prop);
		Dataset<Row> allstocksMarket = allstocks.filter(allstocks.col("marketid").equalTo(market));
		for (Row row : allstocksMarket.collectAsList()) {
			String dbid = row.getAs("dbid");
			String marketid = row.getAs("marketid");
			String id = row.getAs("id");
			String name = row.getAs("name");
			Date date = row.getAs("date");
			Double indexvalue = row.getAs("indexvalue");
			Double price = row.getAs("price");
			String currency = row.getAs("currency");
			Double period1 = row.getAs("period1");
			Double period2 = row.getAs("period2");
			Double period3 = row.getAs("period3");
			Double period4 = row.getAs("period4");
			Double period5 = row.getAs("period5");
			Double period6 = row.getAs("period6");
			retList.add(new StockItem(dbid, marketid, id, name, date, indexvalue, price, currency, period1, period2, period3, period4, period5, period6));			
		}
		log.info("time0 " + (System.currentTimeMillis() - time0));
		{
			Map<String, List<Double>> listMap;
		}
		return retList;
	}

	public static MetaItem getMarket(String market) {
		Dataset<Row> allmetas = spark.read().jdbc("jdbc:postgresql://localhost:5432/stockstat?user=stockstat&password=password", "meta", prop);
		allmetas.show();
		for (Row row : allmetas.collectAsList()) {
			String marketid = row.getAs("marketid");
			if (market.equals(marketid)) {
				String period1 = row.getAs("period1");
				String period2 = row.getAs("period2");
				String period3 = row.getAs("period3");
				String period4 = row.getAs("period4");
				String period5 = row.getAs("period5");
				String period6 = row.getAs("period6");
				return new MetaItem(marketid, period1, period2, period3, period4, period5, period6);
			}
		}
		return null;
	}

}

