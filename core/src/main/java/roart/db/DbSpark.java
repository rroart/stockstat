package roart.db;

import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.config.MyPropertyConfig;
import roart.indicator.Indicator;
import roart.model.MetaItem;
import roart.model.StockItem;
import roart.util.Constants;
import scala.Tuple2;
import scala.collection.JavaConversions;

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
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.ml.Model;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSpark {

	private static Logger log = LoggerFactory.getLogger(DbSpark.class);

	private static SparkSession spark;
	private static Properties prop;

    //private static Model model;
	private static Map<String, Model> modelMap = new HashMap<>();
    private static Map<String, Double> accuracyMap = new HashMap<>();
    
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

    public static Map<String, Double[]> doCalculations(Map<String, List<Double>> listMap, Indicator ind) {
        if (spark == null) {
            return null;
        }
        System.out.println("running spark");
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
        Map<String, Double[]> m = df.collectAsList().stream().collect(Collectors.toMap(x -> x.getAs("id"), x -> (Double[])ind.calculate(x.getAs("values"))));
        //System.out.println("m size " + m.size());
        return m;
    }

    public static Dataset<Row> createDFfromMap(Map<double[], Double> listMap) {
        if (spark == null) {
            return null;
        }
        List<Row> rowList = new ArrayList<>();
        int i = 0;
        for (double[] array : listMap.keySet()) {
            //log.info("arrsize " + array.length);
             Double label = listMap.get(array);
             if (label == null) {
                 log.error("no label");
             }
            Row row = RowFactory.create(label, Vectors.dense(array));
            rowList.add(row);
            //if (i++ > 1) break;
        }
        List<Row> data = rowList;
        StructType schema = new StructType(new StructField[]{
                new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("features", new VectorUDT(), false, Metadata.empty())
        });
        Dataset<Row> dataFrame = spark.createDataFrame(data, schema);
        return dataFrame;
    }
    
    public static Dataset<Row> createDFfromMap2(Map<String, double[]> listMap) {
        if (spark == null) {
            return null;
        }
        List<Row> rowList = new ArrayList<>();
        int i = 0;
        for (String id : listMap.keySet()) {
            //log.info("arrsize " + array.length);
             double[] array = listMap.get(id);
            Row row = RowFactory.create(id, Vectors.dense(array));
            rowList.add(row);
            //if (i++ > 1) break;
        }
        List<Row> data = rowList;
        StructType schema = new StructType(new StructField[]{
                new StructField("id", DataTypes.StringType, false, Metadata.empty()),
                new StructField("features", new VectorUDT(), false, Metadata.empty())
        });
        Dataset<Row> dataFrame = spark.createDataFrame(data, schema);
        return dataFrame;
    }
    
    public static Dataset<Row> createDF(List<double[]> arrList) {
        if (spark == null) {
            return null;
        }
        List<Row> rowList = new ArrayList<>();
        for (double[] array : arrList) {
             //String label = listMap.get(array);
            Row row = RowFactory.create(Vectors.dense(array));
            rowList.add(row);
        }
        List<Row> data = rowList;
        StructType schema = new StructType(new StructField[]{
                //new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("features", new VectorUDT(), false, Metadata.empty())
        });
        Dataset<Row> dataFrame = spark.createDataFrame(data, schema);
        return dataFrame;
    }
    
    public static Map<String, Double[]> classify(Map<String, double[]> map, String modelStr, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        //List<double[]> arrList = new ArrayList<>();
        //arrList.add(array);
        Map<String, Double[]> retMap = new HashMap<>();
        Dataset<Row> data = createDFfromMap2(map);
        try {
            /*
            List<Row> jrdd = Arrays.asList(
                    RowFactory.create(content));

            String schemaString = "sentence";

            // Generate the schema based on the string of schema
            List<StructField> fields = new ArrayList<>();
            for (String fieldName : schemaString.split(" ")) {
                StructField field = DataTypes.createStructField(fieldName, DataTypes.StringType, true);
                fields.add(field);
            }
            StructType schema = DataTypes.createStructType(fields);
             */
            //Dataset<Row> sentenceDF = spark.createDataFrame(jrdd, schema);
            Model model = modelMap.get(modelStr+period+mapname);
            if (model == null) {
                return retMap;
            }
            Dataset<Row> resultDF = model.transform(data);

            for (Row row : resultDF.collectAsList()) {
                String id = row.getAs("id");
                Double predict = row.getAs("prediction");
                Double prob = null;
                try {
                    DenseVector probvector = row.getAs("probability");
                    double[] probarray = probvector.values();
                    prob = probarray[predict.intValue()];
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            //Map<Double, String> label = conf.labelsMap.get(language);
            //String cat = label.get(predict);
             //  log.info(" cat " + predict);
            //MachineLearningClassifyResult result = new MachineLearningClassifyResult();
            //result.result = cat;
                Double[] retVal = new Double[2];
                retVal[0] = predict;
                retVal[1] = prob;
                String label = shortMap.get(predict);
                retMap.put(id, retVal);
            }
            log.info("classify done");
            return retMap;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }

    private final static int CATS = 4;
    
    public static void learntest(Map<double[], Double> map, String modelStr, int size, String period, String mapname, int outcomes) {
        try {
            if (spark == null) {
                return;
            }
            if (map.isEmpty()) {
                return;
            }
        Dataset<Row> data = createDFfromMap(map);
        Dataset<Row>[] splits = data.randomSplit(new double[]{0.6, 0.4}, 1234);
        Dataset<Row> train = splits[0];
        Dataset<Row> test = splits[1];
        log.info("data size " + map.size());
        Model model = null;
        if ("LogisticRegression".equals(modelStr)) {
            LogisticRegression reg = new LogisticRegression();
            //reg.setLabelCol("label");
            reg.setMaxIter(5);
            reg.setRegParam(0.01);
            model = reg.fit(train);
        }
        if ("MultilayerPerceptronClassifier".equals(modelStr)) {
            //int[] layers = new int[]{size + 1, size + 1, size + 1, CATS + 1};
            //int[] layers = new int[]{size, size + 2, size + 4, CATS + 1};
            int[] layers = new int[]{size, outcomes + 1, outcomes + 1, outcomes + 1};
            //int[] layers = new int[]{size, 15, 15, outcomes + 1};
            //int[] layers = new int[]{size, size, size, outcomes + 1};
             MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
                    .setLayers(layers)
                    .setBlockSize(128)
                    .setSeed(1234L)
                    .setMaxIter(100);
            model = trainer.fit(train);
        }
        modelMap.put(modelStr+period+mapname, model);
                    // compute accuracy on the test set                                         
                    Dataset<Row> result = model.transform(test);
                    //result.schema().toString();
                    //result.show();
                    Dataset<Row> predictionAndLabels = result.select("prediction", "label");
                    //predictionAndLabels.show();
                    MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                      .setMetricName("accuracy");
                    double eval = evaluator.evaluate(predictionAndLabels);
                    log.info("Test set accuracy for " + mapname + " " + modelStr + " " + period + " = " + eval);
                    accuracyMap.put(modelStr+period+mapname, eval);

       
    } catch (Exception e) {
        log.error("Exception", e);
    }
    }
    public static double eval(String modelStr, String period, String mapname) {
        return accuracyMap.get(modelStr+period+mapname);
    }
}

