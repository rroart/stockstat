package roart.db.spark;

import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.common.util.ArraysUtil;
import roart.model.StockItem;
import roart.pipeline.common.Calculatable;
import roart.spark.SparkSessionUtil;
import scala.collection.mutable.ArraySeq;

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
            Integer timeout = conf.getMLSparkTimeout();
            spark = SparkSessionUtil.createSparkSession(sparkmaster, "Stockstat DB", timeout);
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
        log.info("spark size {}", allstocks.count());
        Dataset<Row> allstocksMarket = allstocks.filter(allstocks.col("marketid").equalTo(market));
        log.info("spark size {}", allstocksMarket.count());
        for (Row row : allstocksMarket.collectAsList()) {
            String dbid = row.getAs("dbid");
            String marketid = row.getAs("marketid");
            String id = row.getAs("id");
            String isin = row.getAs("isin");
            String name = row.getAs("name");
            Date date = row.getAs("date");
            Double indexvalue = row.getAs("indexvalue");
            Double indexvaluelow = row.getAs("indexvaluelow");
            Double indexvaluehigh = row.getAs("indexvaluehigh");
            Double indexvalueopen = row.getAs("indexvalueopen");
            Double price = row.getAs("price");
            Double pricelow = row.getAs("pricelow");
            Double pricehigh = row.getAs("pricehigh");
            Double priceopen = row.getAs("priceopen");
            Long volume = row.getAs("volume");
            String currency = row.getAs("currency");
            Double period1 = row.getAs("period1");
            Double period2 = row.getAs("period2");
            Double period3 = row.getAs("period3");
            Double period4 = row.getAs("period4");
            Double period5 = row.getAs("period5");
            Double period6 = row.getAs("period6");
            Double period7 = row.getAs("period7");
            Double period8 = row.getAs("period8");
            Double period9 = row.getAs("period9");
            retList.add(new StockItem(dbid, marketid, id, isin, name, date, indexvalue, indexvaluelow, indexvaluehigh, indexvalueopen, price, pricelow, pricehigh, priceopen, volume, currency, period1, period2, period3, period4, period5, period6, period7, period8, period9));			
        }
        log.info("spark size {}", retList.size());
        log.info("time0 " + (System.currentTimeMillis() - time0));
        log.info("spark size {}", allstocks.count());
        log.info("spark size {}", allstocksMarket.count());
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
                String period7 = row.getAs("period7");
                String period8 = row.getAs("period8");
                String period9 = row.getAs("period9");
                String priority = row.getAs("priority");
                String reset = row.getAs("reset");
                boolean lhc = row.getAs("lhc");
                return new MetaItem(marketid, period1, period2, period3, period4, period5, period6, period7, period8, period9, priority, reset, lhc);
            }
        }
        return null;
    }

    public static Map<String, Object[]> doCalculations(Map<String, List<Double>> listMap, Calculatable ind) {
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
                        DataTypes.createStructField("values", DataTypes.createArrayType(DataTypes.createArrayType(DataTypes.DoubleType)), false)});

        Dataset<Row> df = spark.createDataFrame(rowList, schema);
        //df.show();
        Map<String, Object[]> m = df.collectAsList().stream().collect(Collectors.toMap(x -> x.getAs("id"), x -> (Object[])ind.calculate((Double[][])((ArraySeq)x.getAs("values")).array())));
        //System.out.println("m size " + m.size());
        log.info("time calc " + (System.currentTimeMillis() - time0));
        return m;
    }

    @Deprecated
    public static Map<String, Object[]> doCalculationsArr(Map<String, Double[]> listMap, String key, Calculatable ind,  boolean wantPercentizedPriceIndex) {
        if (spark == null) {
            return null;
        }
        long time0 = System.currentTimeMillis();
        System.out.println("running spark");
        List<Row> rowList = new ArrayList<>();
        for (String id : listMap.keySet()) {
            Double[] values = listMap.get(id);
            //values = ArraysUtil.getArrayNonNullReverse(values);
            if (wantPercentizedPriceIndex) {
                values = ArraysUtil.getPercentizedPriceIndex(values);
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
        Map<String, Object[]> objMap = null; //df.collectAsList().stream().collect(Collectors.toMap(x -> x.getAs("id"), x -> (Object[])ind.calculate((Double[])((ArraySeq)x.getAs("values")).array())));
        //System.out.println("m size " + m.size());
        log.info("time calc " + (System.currentTimeMillis() - time0));
        return objMap;
    }

    public static Map<String, Object[]> doCalculationsArrNonNull(Map<String, double[][]> listMap, String key, Calculatable ind,  boolean wantPercentizedPriceIndex) {
        if (spark == null) {
            return null;
        }
        long time0 = System.currentTimeMillis();
        System.out.println("running spark");
        List<Row> rowList = new ArrayList<>();
        for (String id : listMap.keySet()) {
            double[][] values = listMap.get(id);
            if (values[0].length == 0) {
                continue;
            }
            //values = ArraysUtil.getArrayNonNullReverse(values);

            /*
            if (wantPercentizedPriceIndex && values.length > 0 && values[0].length > 0) {
                double first = values[0][0];
                for(int i = 0; i < values.length; i ++) {
                    values[i] = ArraysUtil.getPercentizedPriceIndex(values[i], key, ind.getCategory(), first);
                }
            }
            */
            if ("0P0000RVOF".equals(id)) {              
                log.info("braz " + Arrays.toString(values));                
            }
            Row row = RowFactory.create(id, values);
            rowList.add(row);
        }
        StructType schema = DataTypes
                .createStructType(new StructField[] {
                        DataTypes.createStructField("id", DataTypes.StringType, false),
                        DataTypes.createStructField("values", DataTypes.createArrayType(DataTypes.createArrayType(DataTypes.DoubleType)), false)});
        //DataTypes.createStructField("values", DataTypes.createArrayType(DataTypes.NullType), false)});

        Dataset<Row> df = spark.createDataFrame(rowList, schema);
        //df.show();
        Map<String, Object[]> objMap = df.collectAsList().stream().collect(Collectors.toMap(x -> x.getAs("id"), x -> (Object[])ind.calculate((scala.collection.Seq[])((ArraySeq)x.getAs("values")).array())));
        //System.out.println("m size " + m.size());
        log.info("time calc " + (System.currentTimeMillis() - time0));
        return objMap;
    }

    private static SparkSession getSparkSession() {
        return spark;
    }
}

