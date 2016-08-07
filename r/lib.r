# rm(list=ls())
# install.packages("RPostgreSQL")
require("RPostgreSQL")
require("ggplot2")
#require("tabplot")
require("gridExtra")

pricetype <- -1
indextype <- -2
periods <- 6

# out of use
splitdate <- function(stocks) {
  list <- list()
  j <- 0
  dates <- unique(stocks$date)
  for (di in 1:length(dates)) {
    mydate <- dates[di];
    sublist <- subset(stocks, date == mydate)
    j <- j + 1
    list[j] <- list(sublist)
  }
  return (list)
}

# out of use
splitid <- function(stocks) {
  list <- list()
  j <- 0
  ids <- unique(stocks$id)
  for (ii in 1:length(ids)) {
    myid <- ids[ii];
    sublist <- subset(stocks, id = myid)
    j <- j + 1
    list[j] <- list(sublist)
  }
  return (list)
}

getdforderperiod <- function(df, period) {
	ds <- df
	if (period == 1) {
	  ds <- df[order(-df$period1),]
	}
	if (period == 2) {
	  ds <- df[order(-df$period2),]
	}
	if (period == 3) {
	  ds <- df[order(-df$period3),]
	}
	if (period == 4) {
	  ds <- df[order(-df$period4),]
	}
	if (period == 5) {
	  ds <- df[order(-df$period5),]
	}
	if (period == 6) {
	  ds <- df[order(-df$period6),]
	}
return (ds)
}

getlistanddiff <- function(datedstocklists, listid, listdate, count, mytableintervaldays) {
  periodmaps <- matrix(list(), nrow = periods, ncol = (count - 1))
  stocklistperiod <- matrix(list(), nrow = periods, ncol = count)
  for (j in 1:count) {
    for (i in 1:periods) {
      df <- data.frame(datedstocklists[j])
      hasperiod <- FALSE
      hasperiod <- !is.infinite(max(getonedfperiod(df, i), na.rm = TRUE))
      if (hasperiod) {
	ds <- getdforderperiod(df, i)
	tmp <- list(ds)
        stocklistperiod[i, j] <- tmp
	if (j > 1) {
	df1 <- stocklistperiod[i, j - 1]
	df2 <- tmp
	   tmplist <- getperiodmap(df1, df2)
	   periodmaps[i, j - 1] <- list(tmplist)
	}
      } else {
      	cat("no period day ", j, " period ", i)
      }
    }
  }
  return(list(periodmaps, stocklistperiod))
}

# out of use
getstockdate <- function(listdate, date) {
c <- 0
for (i in names(listdate)) {
c <- c + 1
if (date == i) {
return(c)
}
}
return (length(listdate))
}

getlistanddiffperiod <- function(datedstocklists, listid, listdate, count, mytableintervaldays, period) {
  periodmap <- list()
  stocklistperiod <- matrix(list(), nrow = periods, ncol = count)
  for (j in 1:count) {
      hasperiod <- FALSE
      # fix later
      hasperiod <- TRUE
      if (hasperiod) {
      	df <- data.frame(datedstocklists[j])
	ds <- getdforderperiod(df, i)
	print("")
	tmp <- list(ds)
        stocklistperiod[[1]][[j]] <- tmp
	if (j > 1) {
	df1 <- stocklistperiod[j - 1]
        stocklistperiod[i][j] <- list2
	df2 <- tmp
	   tmplist <- getperiodmap(df1, df2)
	}
      }
  }
  return(list(periodmap, stocklistperiod))
}

getperiodlist <- function(list1, list2) {
  c <- 0
  list <- list()
  df1 <- data.frame(list1[1])
  df2 <- data.frame(list2[1])
  for (j in 1:nrow(df2)) {
    c <- c + 1
    list[c] <- NA
      for (i in 1:nrow(df1)) {
      if (identical(df1[j, "id"], df2[i, "id"])) {
        list[c] <- i - j
      }
    }
  }
  return (list)
}


getperiodmap <- function(list1, list2) {
  list <- list()
  df1 <- data.frame(list1[1])
  df2 <- data.frame(list2[1])
  for (j in 1:nrow(df2)) {
    id <- df2[j, "id"]
    list[id] <- NA
      for (i in 1:nrow(df1)) {
      if (identical(df1[i, "id"], id)) {
        list[id] <- j - i
      }
    }
  }
  return (list)
}

mytop <- function(datedstocklists, stocklistperiod, periodmaps, period, max) {
list1 <- stocklistperiod
list2 <- periodmaps[[period]][[1]]
list11=stocklistperiod[[1]][1]
list12=stocklistperiod[[1]][2]
list13=stocklistperiod[[1]][3]
list14=stocklistperiod[[1]][4]
list15=stocklistperiod[[1]][5]
list21=list2[[1]]
list211=list21[1]
list22=list2[2]
list23=list2[3]
list24=list2[4]
list25=list2[5]
for (i in 1:max) {
print(sprintf("%-40s %12s %3.2f %3d %3.2f %3d\n", strtrim(list11[[1]]$name[i],38), as.POSIXct(list11[[1]]$date[i], origin="1970-01-01"), list11[[1]]$period1[i], list2[[1]][[i]], list12[[1]]$period1[i], list2[[2]][[i]]))
}
}

listperiod <- function(list, period, index) {
if (period == 1) {
return (list$period1[index])
}
if (period == 2) {
return (list$period2[index])
}
if (period == 3) {
return (list$period3[index])
}
if (period == 4) {
return (list$period4[index])
}
if (period == 5) {
return (list$period5[index])
}
if (period == 6) {
return (list$period6[index])
}
}

mytopperiod <- function(datedstocklists, stocklistperiod, periodmaps, period, max) {
list1 <- stocklistperiod
list2 <- periodmaps[period, 1][[1]]

list11 <- stocklistperiod[period, 1][[1]]
list12 <- stocklistperiod[period, 2][[1]]
for (i in 1:max) {
print(sprintf("%3d %-35s %12s %3.2f", i, strtrim(list12$name[i],33), as.POSIXct(list12$date[i], origin="1970-01-01"), listperiod(list12, period, i)))
}
for (i in 1:max) {
id <- list11$id[i]
print(sprintf("%3d %-35s %12s %3.2f %3d %s", i, strtrim(list11$name[i],33), as.POSIXct(list11$date[i], origin="1970-01-01"), listperiod(list11, period, i), list2[[id]], list11$id[[i]]))
}
}

mybottomperiod <- function(datedstocklists, stocklistperiod, periodmaps, period, max) {
list1 <- stocklistperiod
list2 <- periodmaps[period, 1][[1]]

list11 <- stocklistperiod[period, 1][[1]]
list12 <- stocklistperiod[period, 2][[1]]

len <- nrow(list12)
len <- len + 1

for (i in 1:max) {
print(sprintf("%3d %-35s %12s %3.2f", i, strtrim(list12$name[len - i],33), as.POSIXct(list12$date[len - i], origin="1970-01-01"), listperiod(list12, period, len - i)))
}

len <- nrow(list11)
len <- len + 1

for (i in 1:max) {
id <- list11$id[len - i]
print(sprintf("%3d %-35s %12s %3.2f %3d %s", i, strtrim(list11$name[len - i],33), as.POSIXct(list11$date[len - i], origin="1970-01-01"), listperiod(list11, period, len - i), list2[[id]], list11$id[[len - i]]))
}
}

gettopchart <- function(days, topbottom, stocklistperiod, period) {
mainlist <- stocklistperiod[period, 1][[1]]
oldlist <- stocklistperiod[period, days][[1]]
maindate <- mainlist$date[1]
olddate <- oldlist$date[1]
ls <- list()
names <- list()
c <- 0
for (i in 1:topbottom) {
l <- getelem(mainlist$id[i], days, stocklistperiod, period, topbottom)
c <- c + 1
ls[c] <- list(l)
names[c] <- mainlist$name[i]
}
displaychart(ls, names, topbottom, period, maindate, olddate)
}

getbottomchart <- function(days, topbottom, stocklistperiod, period) {
mainlist <- stocklistperiod[period, 1][[1]]
oldlist <- stocklistperiod[period, days][[1]]
maindate <- mainlist$date[1]
olddate <- oldlist$date[1]
ls <- list()
names <- list()
c <- 0
len <- nrow(mainlist)
print(len)
len <- len + 1
for (i in 1:topbottom) {
l <- getelem(mainlist$id[len - i], days, stocklistperiod, period, topbottom)
c <- c + 1
ls[c] <- list(l)
names[c] <- mainlist$name[len - i]
}
displaychart(ls, names, topbottom, period, maindate, olddate)
}

getchart <- function(days, stocklistperiod, period, ids) {
topbottom <- length(ids)
mainlist <- stocklistperiod[period, 1][[1]]
oldlist <- stocklistperiod[period, days][[1]]
maindate <- mainlist$date[1]
olddate <- oldlist$date[1]
ls <- list()
names <- list()
c <- 0
for (i in 1:topbottom) {
l <- getelem(ids[[i]], days, stocklistperiod, period, topbottom)
c <- c + 1
ls[c] <- list(l)
listdf <- getelemtup(ids[[i]], days, stocklistperiod, period, topbottom)
df <- data.frame(listdf[[1]])
names[c] <- df$name
}
displaychart(ls, names, topbottom, period, maindate, olddate)
if (topbottom == 2) {
c1 <- c(unlist(ls[1]))
c2 <- c(unlist(ls[2]))
print("here1")
t.test(c1,c2,paired=TRUE)
print("here2")
#t.test(c1,c1,paired=TRUE)
cor.test(c1, c2, method = c("pearson"))
str(c1)
str(c2)
}
}

getperiodtext <- function(meta, period) {
if (period == 1) {
return (meta$period1)
}
if (period == 2) {
return (meta$period2)
}
if (period == 3) {
return (meta$period3)
}
if (period == 4) {
return (meta$period4)
}
if (period == 5) {
return (meta$period5)
}
if (period == 6) {
return (meta$period6)
}
cat("should not be here")
}

displaychart <- function(ls, names, topbottom, period, maindate, olddate) {
g_range = range(0, ls, na.rm=TRUE)
print("g_range")
str(g_range)
for (i in 1:topbottom) {
if (i == 1) {
#str(l$id[[1]])
#str(l$name[[2]])
c = c(unlist(ls[1]))
str(c)
plot(c, type="o", ylim=g_range, axes=FALSE, ann=FALSE)
axis(1, at=1:days, lab=c(-(days-1):0))
axis(2, las=2)
grid(NULL,NULL)
box()
#l2 <- getc(l, period)
#str(l[[1]]$period1)
#str(l2)
} else {
#cat("count", i)
c = c(unlist(ls[i]))
#str(c)
lines(c, type="o")
}
periodtext <- period

if (period >= 0) {
newtext <- getperiodtext(mymeta, period)
if (!is.na(newtext)) {
periodtext <- newtext
}
}

title(main=sprintf("Period %s", periodtext))
title(xlab=sprintf("Time %s - %s", olddate, maindate))
title(ylab="Value")
n = c(unlist(names[1]))
legend(1, g_range[2], names, cex=0.8, pch=21:22, lty=1:2) 
}
#}
}

getrising <- function(days, periodmaps, stocklistperiod, period) {
retl <- list()
for (i in 1:(days - 1)) {
p <- periodmaps[period, i][[1]]
l <- stocklistperiod[period, i + 1]
df <- data.frame(l[[1]])
#str(i)
#str(period)
#str(df)
#str(nrow(df))
if (nrow(df) > 0) {
for (j in 1:nrow(df)) {
#str(j)
id <- df[j, "id"]
#cat("id",id)
if (is.null(retl[[id]])) {
retl[[id]] <- 0
}
if (!is.na(p[[id]])) {
retl[[id]] <- retl[[id]] + p[[id]]
}
}
} else {
cat("empty df for ",i)
}
}
return(list(sort(data.frame(retl), decreasing = TRUE)))
}

getc <- function(list, period) {
retl <- list[[1]]$period1
return (retl)
}

getdfperiod <- function(df, index, period) {
if (period == 1) {
return (df[index, "period1"])
}
if (period == 2) {
return (df[index, "period2"])
}
if (period == 3) {
return (df[index, "period3"])
}
if (period == 4) {
return (df[index, "period4"])
}
if (period == 5) {
return (df[index, "period5"])
}
if (period == 6) {
return (df[index, "period6"])
}
cat("should not be here")
}

getonedfperiod <- function(df, period) {
if (period == 1) {
return (df$period1)
}
if (period == 2) {
return (df$period2)
}
if (period == 3) {
return (df$period3)
}
if (period == 4) {
return (df$period4)
}
if (period == 5) {
return (df$period5)
}
if (period == 6) {
return (df$period6)
}
cat("should not be here")
}

getonedfspecial <- function(df, type) {
if (period == pricetype) {
return (df$price)
}
if (period == indextype) {
return (df$index)
}
cat("should not be here")
}

getonedfvalue <- function(df, type) {
if (type > 0) {
return(getonedfperiod(df, type))
}
if (type < 0) {
return(getonedfspecial(df, type))
}
cat("should not be here")
}

getelem3 <- function(id, days, datedstocklist, period, size) {
#    str("her")
#    str(id)
#    str(days)
#    str(period)
#    str(datedstocklist)
retl <- list()
c <- 0
for (i in days:1) {
c <- c + 1
retl[c] <- NA
l <- datedstocklist[[1]][i]
#str(l[[1]])
df <- data.frame(l[[1]])
#str(df)
                                        #cat("mylen ", nrow(df))
el <- df[which(df$id == id),]
#str(i)
if (nrow(el) == 1) {
retl[c] <- c(getonedfvalue(el, period))
} else {
print("err")
}
}
return(unlist(retl))
}

getelem <- function(id, days, stocklistperiod, period, size) {
retl <- list()
c <- 0
for (i in days:1) {
c <- c + 1
retl[c] <- NA
l <- stocklistperiod[period, i]
df <- data.frame(l[[1]])
#cat("mylen ", nrow(df))
el <- df[which(df$id == id),]
if (nrow(el) == 1) {
retl[c] <- c(getonedfperiod(el, period))
} else {
print("err")
}
}
return(unlist(retl))
}

getelemtup <- function(id, days, stocklistperiod, period, size) {
#cat("id",id)
retl <- list()
for (i in days:1) {
l <- stocklistperiod[period, i]
df <- data.frame(l[[1]])

el <- df[which(df$id == id),]
if (nrow(el) == 1) {
return(list(el))
} else {
print("err")
}

}
#TODO
return()
}

# out of use
listfiltertop <- function(list, listmain, size) {
retl <- list()
max <- max(size, length(listmain))
for (i in 1:max) {
id <- listmain$id[i]
for (j in 1:length(list)) {
if (identical(id, list[j]$id)) {
retl.add(list[j])
}
}
}
}

getdatedstocklists <- function(listdate, date, mytableintervaldays) {
datedstocklists <- list()
if (is.null(date)) {
dateindex <- match(date, names(listdate))
} else {
dateindex <- length(listdate)
}
str(dateindex)
index <- dateindex
#index <- length(listdate)
  c <- 0
  c <- c + 1
  datedstocklists[c] <- listdate[index]

  for (j in 1:count) {
    index <- index - mytableintervaldays
    c <- c + 1
    datedstocklists[c] <- listdate[index]
  }
  return(datedstocklists)
}

getcontentgraph <- function(con, date, ids, periodtext) {
    markets <- list()
    for (id in ids) {
#        str(id)
        markets[id[1]] <- id[1]
    }
    marketdatamap <- list()
    for (market in names(markets)) {
        stocks <- getmarket(con, market)
        listdate <- split(stocks, stocks$date)
                                        #listid <- split(stocks, stocks$id)
        periodtexts <- getperiodtexts(market)
        datedstocklists <- getdatedstocklists(listdate, date, mytableintervaldays)
        marketdatamap[market] <- list(list(stocks, periodtexts, datedstocklists))
                                        #for (j in 1:count) {
                                        #stocks <- datedstocklist[j]
                                        #df <- data.frame(stocks[[1]])
                                        #el <- df[which(df$id == id),]
                                        #}
    }
    perioddatamap <- list()
    for (market in names(markets)) {
        marketdata <- marketdatamap[market]
        periodtexts <- marketdata[[1]][2]
        for (i in 1:periods) {
            text <- periodtexts[[1]][[i]]
            pair <- list(market, i)
            pairkey <- paste(1, market)
#            str(text)
            if (is.null(perioddatamap[[text]])) {
#                str("new")
                perioddata <- list()
                perioddata[["text"]] <- list()
                perioddatamap[text] <- perioddata
            }
            perioddata <- perioddatamap[[text]]
            pairs <- perioddata[["text"]]
            pairs[[pairkey]] <- pair
            perioddata[["text"]] <- pairs
            perioddatamap[[text]] <- perioddata
        }
    }
    retl <- list()
    #perioddata <- perioddatamap[periodtext]
    #pairs <- perioddata["text"]
    #str("bla")
    #str(perioddatamap);
    #str("bla2")
    for (text in names(perioddatamap)) {
        if (text == periodtext) {
#        str(text)
        perioddata <- perioddatamap[[text]]
        pairs <- perioddata[["text"]]
        pair <- pairs[[pairkey]]
        market <- pair[[1]]
        period <- pair[[2]]
#        str("mark")
#        str(market)
#        str(period)
        marketdata <- marketdatamap[market]
            datedstocklists <- marketdata[[1]][3]
            ls <- list()
            names <- list()
            c <- 0
        for (i in 1:length(ids)) {
            idpair <- ids[[i]]
            idmarket <- idpair[1]
            id <- idpair[2]
 #           str("for")
            cat(market, idmarket, id)
            if (market == idmarket) {
                cat("per", text, " ", id, " ", period, " ")
                c <- c + 1
                l <- getelem3(id, days, datedstocklists, period, topbottom)
                ls[c] <- list(l)
                names[c] <- "test"
#                str(l)
            }
        }
        }
    }
    maindate <- "1"
    olddate <- "2"
    displaychart(ls, names, 5, period, maindate, olddate)
}

getperiodtexts <- function(market) {
periodtext = list("Period1", "Period2", "Period3", "Period4", "Period5", "Period6")
meta <- dbGetQuery(con, "select * from meta")
mymeta <- subset(meta, marketid == market)
if (nrow(mymeta) > 0) {
for (i in 1:periods) {
if (!is.na(getperiodtext(mymeta, i))) {
periodtext[i] = getperiodtext(mymeta, i)
}
}
}
return(periodtext)
}

getmarket <- function(con, market) {
query <- paste("select * from stock where marketid = '", market, "'", sep = "")
return(dbGetQuery(con, query))
}

# create a connection
# save the password that we can "hide" it as best as we can by collapsing it
pw <- {
  "password"
  }

if (exists("drv")) {
cons <- dbListConnections(drv)
for (con in cons) {
print(con)
dbDisconnect(con)
}
#dbUnloadDriver(drv)
}

# loads the PostgreSQL driver
if (!exists("drv")) {
drv <- dbDriver("PostgreSQL")
}
# creates a connection to the postgres database
# note that "con" will be used later in each connection to the database
if (!exists("con")) {
con <- dbConnect(drv, dbname = "stockstat",
                 host = "localhost", port = 5432,
		                  user = "stockstat", password = pw)
				  rm(pw) # removes the password
}
#on.exit(dbDisconnect(con))
#on.exit(dbUnloadDriver(drv), add = TRUE)

# check for the cartable
dbExistsTable(con, "stockstat")
dbExistsTable(con, "stock")
# TRUE

if (!exists("marketid")) {
marketid <- "morncat"
}

data <- dbGetQuery(con, "select * from stock")
meta <- dbGetQuery(con, "select * from meta")
mymeta <- subset(meta, marketid == mymarketid)
data_3 <- getmarket(con, marketid)
names(data_3)
s <- subset(data_3, "id" == "EUCA000749")

for (i in 1:nrow(data_3)) {
#print(data_3[i,"date"])
#return()
}

#for (i in data_3) {
#print(i["date"])
#return
#}

listid2 <- splitid(data_3)
listdate2 <- splitdate(data_3)
listdate <- split(data_3, data_3$date)
listid <- split(data_3, data_3$id)

#l <- listdate[[104]]
if (!exists("days")) {
days <- 10
}
if (!exists("topbottom")) {
topbottom <- 5
}
count <- days
if (!exists("mytableintervaldays")) {
mytableintervaldays <- 5
}
#date <- "2016-05-02"

datedstocklists <- getdatedstocklists(listdate, date, mytableintervaldays)

if (!exists("period")) {
period <- 3
}

#alist <- getlistanddiff(datedstocklists, listid, listdate, days, mytableintervaldays)
#periodmaps <- alist[[1]]
#stocklistperiod <- alist[[2]]
#mybottomperiod(datedstocklists, stocklistperiod, periodmaps, period, topbottom)
#mytopperiod(datedstocklists, stocklistperiod, periodmaps, period, topbottom)

#gettopchart(days, topbottom, stocklistperiod, period)
#getbottomchart(days, topbottom, stocklistperiod, period)
#rise <- getrising(days, periodmaps, stocklistperiod, period)
#risetopids <- head(names(rise[[1]]))

getcontentgraph(con, date, ids, "1y")

# close the connection
dbDisconnect(con)
dbUnloadDriver(drv)
#rm(list = ls())
rm(con)
rm(drv)
print("ending")
#return

