import org.apache.spark.sql.SparkSession
import java.sql.Timestamp
import java.sql.Date
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import scala.collection.mutable.MutableList
import scala.collection.mutable.ListBuffer
import com.tictactec.ta.lib.Core
import com.tictactec.ta.lib.MInteger
//import org.apache.spark.sql.SparkSession
//val spark: SparkSession = SparkSession.builder.master("local[*]").appName("My Spark Application").getOrCreate
val prop = new java.util.Properties
prop.setProperty("driver", "org.postgresql.Driver")
//val df = spark.read.jdbc("jdbc:postgresql://stockstat:password@localhost:5432/stockstat", "meta", prop)

val pricetype = -1
val indextype = -2
val metaperiods = 6
val periods = 6

val VALUE = 1
val MACD = 2
val RSI = 3

val mydateformat = "yyyy.MM.dd"

val weekendfilter = true

val allmetas = spark.read.jdbc("jdbc:postgresql://localhost:5432/stockstat?user=stockstat&password=password", "meta", prop)
var allstocks = spark.read.jdbc("jdbc:postgresql://localhost:5432/stockstat?user=stockstat&password=password", "stock", prop)
if (weekendfilter) {
  allstocks = allstocks.filter("dayofweek(date) > 1 and dayofweek(date) < 7")
}

def getstockdate(stocklist: List[String], mydate : String) : Int = {
if (mydate == null) {
return 0
}
stocklist.indexOf(mydate)
}

def getdforderperiod (df: Dataset[Row], period : Int) : Dataset[Row] = {
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

def getdforderperiodreverse (df: Dataset[Row], period : Int) : Dataset[Row] = {
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

def getonedfperiod (df: Dataset[Row], period : Int) : Dataset[Row] = {
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

def getmaxonedfperiod (df: Dataset[Row], period : Int) : Double = {
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

def getperiodmap(list1 : Dataset[Row], list2 : Dataset[Row]) : Map[String, Int] = {
    val map = Map
    val df1 = list1
    val df2 = list2
    val l1 = df1.select("id").collect.flatMap(_.toSeq)
    val l2 = df2.select("id").collect.flatMap(_.toSeq)
    val common = l2.intersect(l1)
    common.map(x => (x.toString -> (l2.indexOf(x) - l1.indexOf(x)))).toMap
}

def getlistmove(datedstocklists : List[Dataset[Row]], listid : Map[String, Dataset[Row]], listdate : Map[String, Dataset[Row]], count : Int, tableintervaldays : Int, stocklistperiod : Array[Array[Dataset[Row]]]) : Array[Array[Map[String, Int]]] = {
    val periodmaps = Array.ofDim[Map[String, Int]](periods, count - 1)
    for (j <- 0 to count - 1) {
        for (i <- 0 to periods - 1) {
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

def getlistsorted(datedstocklists: List[Dataset[Row]], listid : Map[String, Dataset[Row]], listdate : Map[String, Dataset[Row]], count : Int, tableintervaldays : Int, wantrise : Boolean = true, reverse : Boolean = false) : Array[Array[Dataset[Row]]] = {
    val stocklistperiod = Array.ofDim[Dataset[Row]](periods, count)
    for (j <- 0 to count - 1) {
        for (i <- 0 to periods - 1) {
            val df = datedstocklists(j)
            var hasperiod = false
// TODO get better val
            hasperiod = getmaxonedfperiod(df, i) != -1
            if (hasperiod) {
                var ds : Dataset[Row] = null;
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
def getstocksdatemap(df : Dataset[Row]) : Map[String, Dataset[Row]] = {
val dt = new java.text.SimpleDateFormat(mydateformat)
val dates = df.select("date").distinct.collect.flatMap(_.toSeq)
val mymap = dates.map(date => (dt.format(date) -> df.where($"date" <=> date))).toMap
mymap
}

def getstocksidmap(df : Dataset[Row]) : Map[String, Dataset[Row]] = {
val ids = df.select("id").distinct.collect.flatMap(_.toSeq)
val mymap = ids.map(id => (id.toString -> df.where($"id" <=> id))).toMap
mymap
}

def getmarketmeta(df: Dataset[Row], marketid: String) : Dataset[Row] = {
df.filter($"marketid" === marketid)
}

def getstockmarket(df: Dataset[Row], marketid: String) : Dataset[Row] = {
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
var periodtext: List[String] = List("Period1", "Period2", "Period3", "Period4", "Period5", "Period6")
val mymeta = getmarketmeta(allmetas, market)
if (mymeta.count > 0) {
for (i <- 0 to periods - 1) {
val txt = getperiodtext(mymeta.first, i)
println("c"+txt)
if (txt != null) {
periodtext = periodtext.updated(i, txt)
}
}
}
periodtext
}

def getdatedstocklists(listdate : Map[String, Dataset[Row]], mydate : Int, days : Int, tableintervaldays : Int) : List[Dataset[Row]] = {
                                        //    str(mydate)
    val dt = new java.text.SimpleDateFormat(mydateformat)
//    val mydate = dt.format(adate)          
    val datelist = collection.mutable.SortedSet(listdate.keySet.toList: _*).toList
    var offset = mydate
    var dateindex : Int = 0
    if (mydate != 0) {
        dateindex = listdate.size - offset - 1
    } else {
        dateindex = listdate.size - 1
    }
    var index = dateindex - offset
println("" + index + " " + datelist.size + " " + offset)
    var date = datelist(index)
println("d0 " + date)
                                        //index <- length(listdate)
// TODO fix list handling
    val datedstocklists = ListBuffer[Dataset[Row]](listdate(date))
println("d0 " + datedstocklists)

    for (j <- 0 to days - 1) {
        index = index - tableintervaldays
println("" + index + " " + datelist.size)
        date = datelist(index)
println("d1 " + date)
println("" + index + " " + datelist.size)
println("d2 " + listdate(date))
        datedstocklists :+ listdate(date)
    }
println("d3 " + datedstocklists)
    datedstocklists.toList
}

def getdatedstocklists1(listdate : Map[String, Dataset[Row]], adate : String, days : Int, tableintervaldays : Int) : List[Dataset[Row]] = {
    val dt = new java.text.SimpleDateFormat(mydateformat)
//    val mydate = dt.format(adate)          
    val datelist = collection.mutable.SortedSet(listdate.keySet.toList: _*).toList
    var index = getstockdate(datelist, adate)
    getdatedstocklists(listdate, index, days, tableintervaldays)
}

def listperiodRow(row : Row, period : Int) : Double = {
    if (period < 7) {
return row.getAs("period"+period)
    }
    if (period == 7) {
return row.getAs("price")
    }
    if (period == 8) {
return row.getAs("indexvalue")
    }
return 0.0
}

def mytopperiod2(dflist : List[Dataset[Row]], period : Int, max : Int, days : Int, wantrise : Boolean = false, wantmacd : Boolean = false, wantrsi : Boolean = false) {
    val dt = new java.text.SimpleDateFormat(mydateformat)

    for (j <- 0 to days - 1) {
        val df = dflist(j)
/*
        if (max < nrow(df)) {
            //max <- nrow(df)
        }
	*/
println("e0")
val alist = df.limit(max).collectAsList
        for (i <- 0 to max - 1) {
/*
            rsi <- NA
            if (wantrsi) {
                rsi <- df$rsic[[i]]
            }
            macd <- NA
            if (wantmacd) {
                macd <- df$momc[[i]]
            }
            rise <- NA
            if (wantrise) {
                rise <- df$risec[[i]]
            }
  */         
val id : String = alist.get(i).getAs("id") 
val name0 : String = alist.get(i).getAs("name")
val mymax = math.min(33, name0.size)
val name = name0.substring(0, mymax)
val date0 : Timestamp = alist.get(i).getAs("date")
val date = dt.format(date0)
val per = listperiodRow(alist.get(i), period)
val rise = 0
val macd = 0
val rsi = 0
println(f"$j%3d $name%-35s $date%12s $per%3.2f $rise%3d $macd%3.2f $rsi%3.2f $id%s")
        }
                                        //        str(df$id[[1]])
    }
}


def gettopgraph(market: String, mydate: String, days: Int, tablemoveintervaldays: Int, topbottom: Int, myperiodtexts2: String, sort: String, macddays: Int = 60, reverse: Boolean = false, wantrise: Boolean = false, wantmacd: Boolean = false, wantrsi: Boolean = false) : String = {
 val periodtexts = getperiodtexts(market)
 val myperiodtexts : List[String] = List(myperiodtexts2)
 for(i <- 0 to myperiodtexts.size - 1) {
  val periodtext = myperiodtexts(i)
println("p " + periodtext + " " + periodtexts)
  val period = periodtexts.indexOf(periodtext)
  val stocks = getstockmarket(allstocks, market)
  val listdate = getstocksdatemap(stocks)
  val listid = getstocksidmap(stocks)

  val datedstocklists = getdatedstocklists1(listdate, mydate, days, tablemoveintervaldays)
  val stocklistperiod = getlistsorted(datedstocklists, listid, listdate, days, tablemoveintervaldays, reverse=reverse)
  //var periodmaps = Array[Array[Map[String, Int]]]
  if (wantrise) {
   //periodmaps = getlistmove(datedstocklists, listid, listdate, days, tablemoveintervaldays, stocklistperiod)
  }
println("d")
  var dflist = ListBuffer[Dataset[Row]](stocklistperiod(period)(0))
println("d")
  val datelist = collection.mutable.SortedSet(listdate.keySet.toList: _*).toList
println("d")

  var index = getstockdate(datelist, mydate)

  var headskiprsi = 0
  if (mydate != null) {
   headskiprsi = index
  }
  var headskipmacd = 0
  if (mydate != null) {
   headskipmacd = index
  }
println("d")
  for (j <- 0 to days - 1) {
println("d1 " + j)
   val df = stocklistperiod(period)(j)
   if (wantrise) {
    var list2 = List
    if (j < days) {
     //list2 = periodmaps(period)(j)
    }
    var riselist = List
   }
println("d4")
   dflist :+ df
println("d5")
  }
  mytopperiod2(dflist.toList, period, topbottom, days, wantrise=wantrise, wantmacd=wantmacd, wantrsi=wantrsi)
 }

 "test"
}
