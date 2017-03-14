import java.sql.Timestamp
import java.sql.Date
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Row
//import org.apache.spark.sql.SparkSession
//val spark: SparkSession = SparkSession.builder.master("local[*]").appName("My Spark Application").getOrCreate
val prop = new java.util.Properties
prop.setProperty("driver", "org.postgresql.Driver")
//val df = spark.read.jdbc("jdbc:postgresql://stockstat:password@localhost:5432/stockstat", "meta", prop)
//val df = sqlContext.sql("select * from meta")
//val df = sqlContext.read.jdbc("jdbc:postgresql://stockstat:password@localhost:5432/stockstat", "meta", prop)

val pricetype = -1
val indextype = -2
val metaperiods = 6
val periods = 6

val VALUE = 1
val MACD = 2
val RSI = 3

val mydateformat = "yyyy.MM.dd"

val allmetas = sqlContext.read.jdbc("jdbc:postgresql://localhost:5432/stockstat?user=stockstat&password=password", "meta", prop)
val allstocks = sqlContext.read.jdbc("jdbc:postgresql://localhost:5432/stockstat?user=stockstat&password=password", "stock", prop)

def getstockdate(stocklist: List[String], mydate : String) : Int = {
if (mydate == 0) {
return 0
}
stocklist.indexOf(mydate)
}

def getdforderperiod (df: DataFrame, period : Int) : DataFrame = {
    var ds = df
    if (period == 1) {
        ds = df.sort($"period1")
    }
    if (period == 2) {
        ds = df.sort($"period2")
    }
    if (period == 3) {
        ds = df.sort($"period3")
    }
    if (period == 4) {
        ds = df.sort($"period4")
    }
    if (period == 5) {
        ds = df.sort($"period5")
    }
    if (period == 6) {
        ds = df.sort($"period6")
    }
    if (period == 7) {
        ds = df.sort($"price")
    }
    if (period == 8) {
        ds = df.sort($"indexvalue")
    }
    ds
}

def getdforderperiodreverse (df: DataFrame, period : Int) : DataFrame = {
    var ds = df
    if (period == 1) {
        ds = df.sort($"period1".desc)
    }
    if (period == 2) {
        ds = df.sort($"period2".desc)
    }
    if (period == 3) {
        ds = df.sort($"period3".desc)
    }
    if (period == 4) {
        ds = df.sort($"period4".desc)
    }
    if (period == 5) {
        ds = df.sort($"period5".desc)
    }
    if (period == 6) {
        ds = df.sort($"period6".desc)
    }
    if (period == 7) {
        ds = df.sort($"price".desc)
    }
    if (period == 8) {
        ds = df.sort($"indexvalue".desc)
    }
    ds
}

def getonedfperiod (df: DataFrame, period : Int) : DataFrame = {
    var ds = df
    if (period == 1) {
        ds = df.select($"period1")
    }
    if (period == 2) {
        ds = df.select($"period2")
    }
    if (period == 3) {
        ds = df.select($"period3")
    }
    if (period == 4) {
        ds = df.select($"period4")
    }
    if (period == 5) {
        ds = df.select($"period5")
    }
    if (period == 6) {
        ds = df.select($"period6")
    }
    if (period == 7) {
        ds = df.select($"price")
    }
    if (period == 8) {
        ds = df.select($"indexvalue")
    }
    ds
}

def getmaxonedfperiod (df: DataFrame, period : Int) : Double = {
    var ds = -1.0
    try {
    if (period == 1) {
        ds = df.agg(max($"period1")).first.getDouble(0)
    }
    if (period == 2) {
        ds = df.agg(max($"period2")).first.getDouble(0)
    }
    if (period == 3) {
        ds = df.agg(max($"period3")).first.getDouble(0)
    }
    if (period == 4) {
        ds = df.agg(max($"period4")).first.getDouble(0)
    }
    if (period == 5) {
        ds = df.agg(max($"period5")).first.getDouble(0)
    }
    if (period == 6) {
        ds = df.agg(max($"period6")).first.getDouble(0)
    }
    if (period == 7) {
        ds = df.agg(max($"price")).first.getDouble(0)
    }
    if (period == 8) {
        ds = df.agg(max($"indexvalue")).first.getDouble(0)
    }
    } catch { case ex : NullPointerException => { }
    }
    ds
}

def getperiodmap(list1 : DataFrame, list2 : DataFrame) : Map[String, Int] = {
    val map = Map
    val df1 = list1
    val df2 = list2
    val l1 = df1.select("id").collect.flatMap(_.toSeq)
    val l2 = df2.select("id").collect.flatMap(_.toSeq)
    val common = l2.intersect(l1)
    common.map(x => (x.toString -> (l2.indexOf(x) - l1.indexOf(x)))).toMap
}

def getlistmove(datedstocklists : List[DataFrame], listid : Map[String, DataFrame], listdate : Map[String, DataFrame], count : Int, tableintervaldays : Int, stocklistperiod : Array[Array[DataFrame]]) : Array[Array[Map[String, Int]]] = {
    val periodmaps = Array.ofDim[Map[String, Int]](periods, count - 1)
    for (j <- 1 to count) {
        for (i <- 1 to periods) {
            val hasperiod = stocklistperiod(i)(j) != null
            if (hasperiod) {
                if (j > 1) {
                    val df1 = stocklistperiod(i)(j - 1)
                    val df2 = stocklistperiod(i)(j)
                    val map = getperiodmap(df1, df2)
                    periodmaps(i)(j - 1) = map
                }
            } else {
                                        //cat("no period day ", j, " period ", i)
            }
        }
    }
    periodmaps
}

def getlistsorted(datedstocklists: List[DataFrame], listid : Map[String, DataFrame], listdate : Map[String, DataFrame], count : Int, tableintervaldays : Int, wantrise : Boolean = true, reverse : Boolean = false) : Array[Array[DataFrame]] = {
    val stocklistperiod = Array.ofDim[DataFrame](periods, count)
    for (j <- 1 to count) {
        for (i <- 1 to periods) {
            val df = datedstocklists(j)
            var hasperiod = false
// TODO get better val
            hasperiod = getmaxonedfperiod(df, i) != -1
            if (hasperiod) {
                var ds : DataFrame = null;
                if (reverse) {
                    ds = getdforderperiodreverse(df, i)
                } else {
                    ds = getdforderperiod(df, i)
                }
                //tmp = ds
                stocklistperiod(i)(j) = ds
            } else {
                                        //cat("no period day ", j, " period ", i)
            }
        }
    }
    stocklistperiod
}

//val marketids = df.select("marketid").distinct.collect.flatMap(_.toSeq)
//val mymap = marketids.map(marketid => (marketid -> df.where($"marketid" <=> marketid))).toMap

// Timestamp
def getstocksdatemap(df : DataFrame) : Map[String, DataFrame] = {
val dt = new java.text.SimpleDateFormat(mydateformat)
val dates = df.select("date").distinct.collect.flatMap(_.toSeq)
val mymap = dates.map(date => (dt.format(date) -> df.where($"date" <=> date))).toMap
mymap
}

def getstocksidmap(df : DataFrame) : Map[String, DataFrame] = {
val ids = df.select("id").distinct.collect.flatMap(_.toSeq)
val mymap = ids.map(id => (id.toString -> df.where($"id" <=> id))).toMap
mymap
}

def getmarketmeta(df: DataFrame, marketid: String) : DataFrame = {
df.filter($"marketid" === marketid)
}

def getstockmarket(df: DataFrame, marketid: String) : DataFrame = {
df.filter($"marketid" === marketid)
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
return null
//println("should not be here")
}

def getperiodtexts(market: String) : List[String] = {
val periodtext: List[String] = List("Period1", "Period2", "Period3", "Period4", "Period5", "Period6")
val mymeta = getmarketmeta(allmetas, market)
if (mymeta.count > 0) {
for (i <- 1 to periods) {
val txt = getperiodtext(mymeta.first, i)
if (txt != null) {
periodtext.updated(i - 1, txt)
}
}
}
periodtext
}

def getdatedstocklists(listdate : Map[String, DataFrame], mydate : Int, days : Int, tableintervaldays : Int) : List[DataFrame] = {
                                        //    str(mydate)
    val dt = new java.text.SimpleDateFormat(mydateformat)
//    val mydate = dt.format(adate)          
    val datelist = collection.mutable.SortedSet(listdate.keySet.toList: _*).toList
    var offset = mydate
    var dateindex : Int = 0
    if (mydate != 0) {
        dateindex = listdate.size - offset
    } else {
        dateindex = listdate.size
    }
    var index = dateindex - offset
    var date = datelist(index)
                                        //index <- length(listdate)
// TODO fix list handling
    val datedstocklists = List[DataFrame](listdate(date))

    for (j <- 1 to days) {
        index = index - tableintervaldays
        date = datelist(index)
        datedstocklists :+ listdate(date)
    }
    datedstocklists
}

def getdatedstocklists1(listdate : Map[String, DataFrame], adate : String, days : Int, tableintervaldays : Int) : List[DataFrame] = {
    val dt = new java.text.SimpleDateFormat(mydateformat)
//    val mydate = dt.format(adate)          
    val datelist = collection.mutable.SortedSet(listdate.keySet.toList: _*).toList
    var index = getstockdate(datelist, adate)
    getdatedstocklists(listdate, index, days, tableintervaldays)
}

def gettopgraph(market: String, mydate: String, days: Int, tablemoveintervaldays: Int, topbottom: Int, myperiodtexts2: String, sort: String, macddays: Int = 60, reverse: Boolean = false, wantrise: Boolean = false, wantmacd: Boolean = false, wantrsi: Boolean = false) : String = {
val periodtexts = getperiodtexts(market)
val myperiodtexts : List[String] = List(myperiodtexts2)
for(i <- 1 to myperiodtexts.size) {
val periodtext = myperiodtexts(i - 1)
val period = periodtexts.indexOf(periodtext)
val stocks = getstockmarket(allstocks, market)
val listdate = getstocksdatemap(stocks)
val listid = getstocksidmap(stocks)

val datedstocklists = getdatedstocklists1(listdate, mydate, days, tablemoveintervaldays)
val stocklistperiod = getlistsorted(datedstocklists, listid, listdate, days, tablemoveintervaldays, reverse=reverse)
if (wantrise) {
val periodmaps = getlistmove(datedstocklists, listid, listdate, days, tablemoveintervaldays, stocklistperiod)
}
val dflist = List
var headskiprsi = 0
if (mydate != null) {
//headskiprsi = mydate
}
val headskipmacd = 0
if (mydate != null) {
//headskipmacd = mydate
}
}
return "test"
}
