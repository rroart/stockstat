import org.apache.spark.sql.SparkSession
val spark: SparkSession = SparkSession.builder.master("local[*]").appName("My Spark Application").getOrCreate
val prop = new java.util.Properties
prop.setProperty("driver", "org.postgresql.Driver")
val df = spark.read.jdbc("jdbc:postgresql://stockstat:password@localhost:5432/stockstat", "meta", prop)