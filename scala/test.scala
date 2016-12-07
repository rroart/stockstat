import java.sql.Timestamp
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Row
//import org.apache.spark.sql.SparkSession
//val spark: SparkSession = SparkSession.builder.master("local[*]").appName("My Spark Application").getOrCreate
val prop = new java.util.Properties
prop.setProperty("driver", "org.postgresql.Driver")
//val df = spark.read.jdbc("jdbc:postgresql://stockstat:password@localhost:5432/stockstat", "meta", prop)
//val df = sqlContext.sql("select * from meta")
//val df = sqlContext.read.jdbc("jdbc:postgresql://stockstat:password@localhost:5432/stockstat", "meta", prop)

val periods = 6

val allmetas = sqlContext.read.jdbc("jdbc:postgresql://localhost:5432/stockstat?user=stockstat&password=password", "meta", prop)
val allstocks = sqlContext.read.jdbc("jdbc:postgresql://localhost:5432/stockstat?user=stockstat&password=password", "stock", prop)

//val marketids = df.select("marketid").distinct.collect.flatMap(_.toSeq)
//val mymap = marketids.map(marketid => (marketid -> df.where($"marketid" <=> marketid))).toMap

// Timestamp
def getstocksdatemap(df : DataFrame) : Map[Any, DataFrame] = {
val dates = df.select("date").distinct.collect.flatMap(_.toSeq);
val mymap = dates.map(date => (date -> df.where($"date" <=> date))).toMap;
return mymap;
}

def getstocksidmap(df : DataFrame) : Map[Any, DataFrame] = {
val ids = df.select("id").distinct.collect.flatMap(_.toSeq);
val mymap = ids.map(id => (id -> df.where($"id" <=> id))).toMap;
return mymap;
}

def getmarketmeta(df: DataFrame, marketid: String) : DataFrame = {
    return df.filter($"marketid" === marketid);
}

def getstockmarket(df: DataFrame, marketid: String) : DataFrame = {
    return df.filter($"marketid" === marketid);
}
    
def getperiodtext(meta: Row, period: Int) : String = {
if (period == 1) {
return (meta.getAs[String]("period1"))
}
if (period == 2) {
return (meta.getAs[String]("period2"))
}
if (period == 3) {
return (meta.getAs[String]("period3"))
}
if (period == 4) {
return (meta.getAs[String]("period4"))
}
if (period == 5) {
return (meta.getAs[String]("period5"))
}
if (period == 6) {
return (meta.getAs[String]("period6"))
}
return null;
//println("should not be here")
}

def getperiodtexts(market: String) : List[String] = {
val periodtext: List[String] = List("Period1", "Period2", "Period3", "Period4", "Period5", "Period6");
val mymeta = getmarketmeta(allmetas, market);
if (mymeta.count > 0) {
for (i <- 1 to periods) {
val txt = getperiodtext(mymeta.first, i);
if (txt != null) {
periodtext.updated(i - 1, txt);
}
}
}
return periodtext;
}

def gettopgraph(market: String, mydate: String, days: Int, tablemoveintervaldays: Int, topbottom: Int, myperiodtexts2: String, sort: String, macddays: Int = 60, reverse: Boolean = false, wantrise: Boolean = false, wantmacd: Boolean = false, wantrsi: Boolean = false) : String = {
val periodtexts = getperiodtexts(market);
val myperiodtexts : List[String] = List(myperiodtexts2);
for(i <- 1 to myperiodtexts.size) {
val periodtext = myperiodtexts(i - 1)
val period = periodtexts.indexOf(periodtext)
val stocks = getstockmarket(allstocks, market)
val listdate = getstocksdatemap(stocks);
val listid = getstocksidmap(stocks);

val datedstocklists = getdatedstocklists(listdate, mydate, days, tablemoveintervaldays);
val stocklistperiod = getlistsorted(datedstocklists, listid, listdate, days, tablemoveintervaldays, reverse=reverse);
if (wantrise) {
val periodmaps = getlistmove(datedstocklists, listid, listdate, days, tablemoveintervaldays, stocklistperiod);
}
val dflist = List;
val headskiprsi = 0;
if (mydate != null) {
headskiprsi = mydate;
}
val headskipmacd = 0;
												        if (!is.null(mydate)) {
													            headskipmacd <- mydate
														            }
															    
}
return "test";
}