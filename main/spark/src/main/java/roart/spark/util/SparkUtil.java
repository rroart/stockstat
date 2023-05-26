package roart.spark.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkUtil {

    private static Logger log = LoggerFactory.getLogger(SparkUtil.class);

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

    public static Dataset<Row> createDFfromMap(SparkSession spark, Map<double[], Double> listMap) {
        if (spark == null) {
            return null;
        }
        List<Row> rowList = new ArrayList<>();
        for (Entry<double[], Double> entry : listMap.entrySet()) {
            double[] array = entry.getKey();
            Double label = entry.getValue();
            if (label == null) {
                log.error("no label");
            }
            Row row = RowFactory.create(label, Vectors.dense(array));
            rowList.add(row);
        }
        List<Row> data = rowList;
        StructType schema = new StructType(new StructField[]{
                new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("features", new VectorUDT(), false, Metadata.empty())
        });
        return spark.createDataFrame(data, schema);
    }

    public static Dataset<Row> createDFfromMap2(SparkSession spark, Map<String, double[]> listMap) {
        if (spark == null) {
            return null;
        }
        List<Row> rowList = new ArrayList<>();
        for (Entry<String, double[]> entry : listMap.entrySet()) {
            String id = entry.getKey();
            double[] array = entry.getValue();
            Row row = RowFactory.create(id, Vectors.dense(array));
            rowList.add(row);
        }
        List<Row> data = rowList;
        StructType schema = new StructType(new StructField[]{
                new StructField("id", DataTypes.StringType, false, Metadata.empty()),
                new StructField("features", new VectorUDT(), false, Metadata.empty())
        });
        return spark.createDataFrame(data, schema);
    }

    public static Dataset<Row> createDF(SparkSession spark, List<double[]> arrList) {
        if (spark == null) {
            return null;
        }
        List<Row> rowList = new ArrayList<>();
        for (double[] array : arrList) {
            Row row = RowFactory.create(Vectors.dense(array));
            rowList.add(row);
        }
        List<Row> data = rowList;
        StructType schema = new StructType(new StructField[]{
                new StructField("features", new VectorUDT(), false, Metadata.empty())
        });
        return spark.createDataFrame(data, schema);
    }
}
