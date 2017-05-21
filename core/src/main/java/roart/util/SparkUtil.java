package roart.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
