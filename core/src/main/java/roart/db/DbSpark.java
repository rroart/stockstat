package roart.db;

import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.config.MyPropertyConfig;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.model.MetaItem;
import roart.model.StockItem;
import roart.util.ArraysUtil;
import roart.util.Constants;
import roart.util.SparkUtil;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.mutable.WrappedArray;

import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.StringReader;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.StringType;
import org.apache.spark.sql.types.DoubleType;
import org.apache.spark.ml.Model;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.linalg.DenseVector;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSpark {

	private static Logger log = LoggerFactory.getLogger(DbSpark.class);

	private static SparkSession spark;
	private static Properties prop;

    //private static Model model;
	public DbSpark(MyMyConfig conf) {
	    
		try {
			String sparkmaster = conf.getDbSparkMaster();
			//sparkmaster = MyPropertyConfig.instance().sparkMaster;
			spark = SparkUtil.createSparkSession(sparkmaster, "Stockstat DB");
			prop = new java.util.Properties();
			prop.setProperty("driver", "org.postgresql.Driver");
			System.out.println("spark conf fin");
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
		    //allstocks.select("date").distinct().sort("date").show();
		    //allstocks.where(allstocks.col("marketid").equalTo("nordhist")).where(allstocks.col("id").equalTo("F00000M1AH")).show();
		    //allstocks.or
		    //spark.create
			Map<String, List<Double>> listMap;
		}
		return retList;
	}

	public static MetaItem getMarket(String market) {
		Dataset<Row> allmetas = spark.read().jdbc("jdbc:postgresql://localhost:5432/stockstat?user=stockstat&password=password", "meta", prop);
		//allmetas.show();
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

    public static Map<String, Object[]> doCalculations(Map<String, List<Double>> listMap, Indicator ind) {
        if (spark == null) {
            return null;
        }
        long time0 = System.currentTimeMillis();
       //System.out.println("running spark");
        List<Row> rowList = new ArrayList<>();
        for (String id : listMap.keySet()) {
            List<Double> values = listMap.get(id);
            Row row = RowFactory.create(id, values.toArray());
            rowList.add(row);
        }
        StructType schema = DataTypes
                .createStructType(new StructField[] {
                        DataTypes.createStructField("id", DataTypes.StringType, false),
                        DataTypes.createStructField("values", DataTypes.createArrayType(DataTypes.DoubleType), false)});
        
        Dataset<Row> df = spark.createDataFrame(rowList, schema);
        //df.show();
        Map<String, Object[]> m = df.collectAsList().stream().collect(Collectors.toMap(x -> x.getAs("id"), x -> (Object[])ind.calculate(x.getAs("values"))));
        //System.out.println("m size " + m.size());
        log.info("time calc " + (System.currentTimeMillis() - time0));
        return m;
    }

    public static Map<String, Object[]> doCalculationsArr(Map<String, Double[]> listMap, String key, Indicator ind,  boolean wantPercentizedPriceIndex) {
        if (spark == null) {
            return null;
        }
        long time0 = System.currentTimeMillis();
        System.out.println("running spark");
        List<Row> rowList = new ArrayList<>();
        for (String id : listMap.keySet()) {
            Double[] values = listMap.get(id);
            values = ArraysUtil.getArrayNonNullReverse(values);
            // TODO !!!
            if (wantPercentizedPriceIndex) {
           values = ArraysUtil.getPercentizedPriceIndex(values, key);
            }
            Row row = RowFactory.create(id, values);
            rowList.add(row);
        }
        StructType schema = DataTypes
                .createStructType(new StructField[] {
                        DataTypes.createStructField("id", DataTypes.StringType, false),
                        DataTypes.createStructField("values", DataTypes.createArrayType(DataTypes.DoubleType), false)});
        
        Dataset<Row> df = spark.createDataFrame(rowList, schema);
        //df.show();
        Map<String, Object[]> objMap = df.collectAsList().stream().collect(Collectors.toMap(x -> x.getAs("id"), x -> (Object[])ind.calculate((Double[])((WrappedArray)x.getAs("values")).array())));
        //System.out.println("m size " + m.size());
        log.info("time calc " + (System.currentTimeMillis() - time0));
       return objMap;
    }
    
    private static SparkSession getSparkSession() {
        return spark;
    }
}

