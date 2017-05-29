package roart.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import roart.db.DbSpark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkUtil {

    private static Logger log = LoggerFactory.getLogger(SparkUtil.class);
    
    public static SparkSession createSparkSession(String sparkmaster, String appName) {
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

        SparkSession spark = SparkSession
                .builder()
                .master(sparkmaster)
                .appName(appName)
                .config(sparkconf)
                .getOrCreate();

        return spark;
        //JavaSparkContext jsc = new JavaSparkContext(sparkconf);
        //SQLContext sqlContext = new SQLContext(jsc);
        //Dataset<Row> df = sqlContext.sql("select * from meta");
        // spark2:            SparkSession spark = SparkSession.builder().master("local[*]").appName("Stockstat Spark").getOrCreate();

        // spark2: Dataset<Row> df = spark.read().jdbc("jdbc:postgresql://stockstat:password@localhost:5432/stockstat", "meta", null);
    }

    public static Dataset<Row> createDFfromMap(SparkSession spark, Map<double[], Double> listMap) {
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

    public static Dataset<Row> createDFfromMap2(SparkSession spark, Map<String, double[]> listMap) {
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

    public static Dataset<Row> createDF(SparkSession spark, List<double[]> arrList) {
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
}
