# rm(list=ls())
# install.packages("RPostgreSQL")
require("RPostgreSQL")
require("ggplot2")
#require("tabplot")
require("gridExtra")

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

getlistanddiff <- function(datedstocklists, listid, listdate, count, mytableintervaldays) {
  periodmaps <- matrix(list(), nrow = 5, ncol = (count - 1))
#  stocklistperiod <- matrix(data.frame(), nrow = 5, ncol = count)
  stocklistperiod <- matrix(list(), nrow = 5, ncol = count)
  stocklistperiod2 <- matrix(1:10, nrow = 5, ncol = count )
#  stocklistperiod <- matrix(list(), nrow = 5, ncol = count)
#  stocklistperiod <- matrix(list(), 5, count)
  for (j in 1:count) {
    print(j);
    for (i in 1:5) {
      hasperiod <- FALSE
      # fix later
      hasperiod <- TRUE
      if (hasperiod) {
      	df <- data.frame(datedstocklists[j])
	ds <- df
	if (i == 1) {
	  ds <- df[order(df$period1),]
	}
	if (i == 2) {
	  ds <- df[order(df$period2),]
	}
	if (i == 3) {
	  ds <- df[order(df$period3),]
	}
	if (i == 4) {
	  ds <- df[order(df$period4),]
	}
	if (i == 5) {
	  ds <- df[order(df$period5),]
	}
	cat("herenr ", (nrow(ds)))
	print("")
	tmp <- list(ds)
#	tmp <- ds
	cat(i,j)
#	print("here2");
#	str(tmp)
#        stocklistperiod2[i][j] <- 100 + j*10 + i
        stocklistperiod2[i + 5*(j-1)] <- 100 + j*10 + i
	print(100 + j*10 + i)
#        stocklistperiod[i][j] <- tmp
	stocklistperiod[i + (j-1) * 5] <- tmp
	if (!identical(stocklistperiod[i][j], tmp)) {
	print("diff")
	}
	if (!identical(stocklistperiod[ + (j-1) * 5], tmp)) {
	print("diff2")
	}
#	  print("here2.5");
#        str(stocklistperiod[i][j])
#	print("here2.6")
#	str(stocklistperiod)
#        list2 <- list(ds)
	if (j > 1) {
#	list1 <- stocklistperiod[i][j - 1]
	df1 <- stocklistperiod[i][j - 1]
#        stocklistperiod[i][j] <- list2
#	list2 <- stocklistperiod[i][j]
#	df2 <- stocklistperiod[i][j]
	df2 <- stocklistperiod[i + (j-1)*5]
	df2 <- tmp
#	cat(i,j)
#	print("here3");
#	str(list2)
#	   cat("here", i);
#	   periodmaps[i][j - 1] <- getperiodlist(list1, list2)
#print("bla1")
#str(df1)
#print("bla2")
#str(df2)
#print("bla1")
#print(nrow(data.frame(df1)))
#print("bla2")
#print(nrow(df2))
	   tmplist <- getperiodmap(df1, df2)
	   print("tmplist")
#	   str(tmplist)
	   periodmaps[i][j - 1] <- list(tmplist)
	}
      }
    }
  }
  str(stocklistperiod)
  str(stocklistperiod2)
#  str(periodmaps)
#  str(periodmaps[1][1])
  return(list(periodmaps, stocklistperiod))
}

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
#  stocklistperiod3 <- matrix(list(), nrow = 5, ncol = count)
  stocklistperiod <- list()
#  stocklistperiod2 <- matrix(1:10, nrow = 5, ncol = count )
#  stocklistperiod <- matrix(list(), nrow = 5, ncol = count)
#  stocklistperiod <- matrix(list(), 5, count)
  for (j in 1:count) {
    print(j);
      hasperiod <- FALSE
      # fix later
      hasperiod <- TRUE
      if (hasperiod) {
      	df <- data.frame(datedstocklists[j])
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
	cat("herenr ", (nrow(ds)))
	print("")
	tmp <- list(ds)
#	tmp <- ds
	cat(period,",",j)
#	print("here2");
#	str(tmp)
#        stocklistperiod3[i][j] <- list(tmp)
#        stocklistperiod3[i][j] <- tmp
	stocklistperiod[j] <- tmp
#	if (!identical(stocklistperiod[i], tmp)) {
#	print("diff")
#	}
#	if (!identical(stocklistperiod[ + (j-1) * 5], tmp)) {
#	print("diff2")
#	}
#	  print("here2.5");
#        str(stocklistperiod[i][j])
#	print("here2.6")
#	str(stocklistperiod)
#        list2 <- list(ds)
	if (j > 1) {
#	list1 <- stocklistperiod[j - 1]
	df1 <- stocklistperiod[j - 1]
#        stocklistperiod[i][j] <- list2
#	list2 <- stocklistperiod[i][j]
#	df2 <- stocklistperiod[i][j]
	df2 <- stocklistperiod[i]
	df2 <- tmp
#	cat(i,j)
#	print("here3");
#	str(list2)
#	   cat("here", i);
#	   periodmaps[i][j - 1] <- getperiodlist(list1, list2)
#print("bla1")
#str(df1)
#print("bla2")
#str(df2)
#print("bla1")
#print(nrow(data.frame(df1)))
#print("bla2")
#print(nrow(df2))
	   tmplist <- getperiodmap(df1, df2)
	   print("tmplist")
#	   str(tmplist)
	   periodmap[j - 1] <- list(tmplist)
	}
      }
  }
#  str(stocklistperiod3)
#  str(stocklistperiod2)
#  str(periodmaps)
#  str(periodmaps[1][1])
  return(list(periodmap, stocklistperiod))
}

#getperiodlist <- function(df1, df2) {
getperiodlist <- function(list1, list2) {
#  print("here4");
#  str(list2)
  c <- 0
  list <- list()
  df1 <- data.frame(list1[1])
  df2 <- data.frame(list2[1])
#  str(df1)
#  str(df2)
  cat("len ", nrow(df2), nrow(df1))
  for (j in 1:nrow(df2)) {
    c <- c + 1
    list[c] <- NA
      for (i in 1:nrow(df1)) {
     	#cat("ids ", df1[j, "id"], df2[i, "id"]);
      if (identical(df1[j, "id"], df2[i, "id"])) {
        list[c] <- i - j
#     	 cat("ident", i-j)
#	 print("")
      }
    }
  }
  cat("here2 ", length(list))
  return (list)
}


getperiodmap <- function(list1, list2) {
#  print("here4");
#  str(list2)
  list <- list()
  df1 <- data.frame(list1[1])
  df2 <- data.frame(list2[1])
#  str(df1)
#  str(df2)
  cat("len ", nrow(df2), nrow(df1))
  for (j in 1:nrow(df2)) {
    id <- df2[j, "id"]
    list[id] <- NA
      for (i in 1:nrow(df1)) {
     	#cat("ids ", df1[j, "id"], id);
      if (identical(df1[j, "id"], df2[i, "id"])) {
        list[id] <- i - j
#     	 cat("ident", i-j)
#	 print("")
      }
    }
  }
  cat("here2 ", length(list))
  return (list)
}

mytop <- function(datedstocklists, stocklistperiod, periodmaps, period, max) {
#str(stocklistperiod)
list1 <- stocklistperiod
list2 <- periodmaps[[period]][[1]]
print("len ")
cat(length(list1),length(list2))
print("mer")
#str(list2)
#print(class(list2[1][[1]]))
#str(list2[1][1])
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
#str(list211[1])
for (i in 1:max) {
print(sprintf("%-40s %12s %3.2f %3d %3.2f %3d\n", strtrim(list11[[1]]$name[i],38), as.POSIXct(list11[[1]]$date[i], origin="1970-01-01"), list11[[1]]$period1[i], list2[[1]][[i]], list12[[1]]$period1[i], list2[[2]][[i]]))
#cat(list11[[1]]$name[i], " ", as.POSIXct(list11[[1]]$date[i], origin="1970-01-01"), " ", list11[[1]]$period1[i], " ", list2[[1]][[i]], "\n")
}
#print(list1[[1]$name[1])
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
}

mytopperiod <- function(datedstocklists, stocklistperiod, periodmaps, period, max) {
#str(stocklistperiod)
list1 <- stocklistperiod
# TODO period
list2 <- periodmaps[[1]][[1]]

#str(list1)
print("len ")
cat(length(list1),length(list2))
print("mer")
#str(list2)
#print(class(list2[1][[1]]))
#str(list2[1][1])
list11=stocklistperiod[[1]][[1]]
list12=stocklistperiod[[1]][[2]]
#str(list211[1])
for (i in 1:max) {
print(sprintf("%3d %-40s %12s %3.2f\n", i, strtrim(list12$name[i],38), as.POSIXct(list12$date[i], origin="1970-01-01"), listperiod(list12, period, i)))
}
for (i in 1:max) {
#print(list11[[1]]$name[i])
#cat(strtrim(list11[[1]]$name[i],38), as.POSIXct(list11[[1]]$date[i], origin="1970-01-01"), list11[[1]]$period1[i], list2[i])
#print(sprintf("%3d %-35s %12s %3.2f %3d %s", i, strtrim(list11$name[i],33), as.POSIXct(list11$date[i], origin="1970-01-01"), list11$period1[i], list2[[i]], list11$id[[i]]))
id <- list12$id[i]
#cat("myid",id)
print(sprintf("%3d %-35s %12s %3.2f %3d %s", i, strtrim(list11$name[i],33), as.POSIXct(list11$date[i], origin="1970-01-01"), listperiod(list11, period, i), list2[[id]], list11$id[[i]]))
#cat(list11[[1]]$name[i], " ", as.POSIXct(list11[[1]]$date[i], origin="1970-01-01"), " ", list11[[1]]$period1[i], " ", list2[[1]][[i]], "\n")
}
#print(list1[[1]$name[1])
}

gettopchart <- function(days, topbottom, stocklistperiod, period) {
mainlist <- stocklistperiod[[1]][[1]]
oldlist <- stocklistperiod[[1]][[days]]
maindate <- mainlist$date[1]
olddate <- oldlist$date[1]
#for (j in 1:days) {
#if (j > 1) {
#l2 <- listfiltertop(stocklistperiod[[period]][j], stocklistperiod[[period]][[1]], topbottom)
#}
ls <- list()
names <- list()
c <- 0
for (i in 1:topbottom) {
cat("id", mainlist$id[i])
l <- getelem(mainlist$id[i], days, stocklistperiod, period, topbottom)
c <- c + 1
ls[c] <- list(l)
names[c] <- mainlist$name[i]
}
displaychart(ls, names, topbottom, period, maindate, olddate)
}

getchart <- function(days, stocklistperiod, period, ids) {
topbottom <- length(ids)
mainlist <- stocklistperiod[[1]][[1]]
oldlist <- stocklistperiod[[1]][[days]]
maindate <- mainlist$date[1]
olddate <- oldlist$date[1]
ls <- list()
names <- list()
c <- 0
for (i in 1:topbottom) {
l <- getelem(ids[[i]], days, stocklistperiod, period, topbottom)
c <- c + 1
ls[c] <- list(l)
names[c] <- mainlist$name[i]
}
displaychart(ls, names, topbottom, period, maindate, olddate)
if (topbottom == 2) {
c1 <- c(unlist(ls[1]))
c2 <- c(unlist(ls[2]))
print("here1")
t.test(c1,c2,paired=TRUE)
print("here2")
#t.test(c1,c1,paired=TRUE)
}
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
axis(2, las=1, at=4*0:g_range[2])
box()
#l2 <- getc(l, period)
#str(l[[1]]$period1)
#str(l2)
} else {
cat("count", i)
c = c(unlist(ls[i]))
str(c)
lines(c, type="o")
}
title(main=sprintf("Period %d", period))
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
#TODO period
p <- periodmaps[[1]][[i]]
l <- stocklistperiod[[1]][i + 1]
df <- data.frame(l[[1]])
for (j in 1:nrow(df)) {
id <- df[j, "id"]
if (is.null(retl[[id]])) {
retl[[id]] <- 0
}
if (!is.na(p[[id]])) {
retl[[id]] <- retl[[id]] + p[[id]]
}
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
}

getelem <- function(id, days, stocklistperiod, period, size) {
retl <- list()
#str(elem)
#str(elem[[10000]])
c <- 0
for (i in days:1) {
c <- c + 1
retl[c] <- NA
l <- stocklistperiod[[1]][i]
df <- data.frame(l[[1]])
#cat("mylen ", nrow(df))
for (j in 1:nrow(df)) {
if (identical(id, df[j, "id"])) {
#cat(" ident " , id, ":",  df[j, "id"], ":");
#retl[c] <- list(df[j,])
#retl[c] <- c(df[j, "period1"])
retl[c] <- c(getdfperiod(df, j, period))
}
}
}
#cat("retlen ", length(retl), c)
#str(retl)
#str(unlist(retl))
return(unlist(retl))
}

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

data <- dbGetQuery(con, "select * from stock")
data_3 <- dbGetQuery(con, "select * from stock where marketid = '3'")
names(data_3)
s <- subset(data_3, "id" == "EUCA000749");

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
#str(listdate2[1])
#print(length(listdate[1]))
#print(length(listdate[104]))

l <- listdate[[104]]
#str(l)
#plot.table(l, format(as.Date(Sys.time()), '%d %b %Y'))
#plot.table(l)
#tableplot(l)
print("herex")
#grid.table(l)
#str(l)
#s <- l[order(l$period1)]
#s <- l
#h <- head(s)
#grid.table(h)
#plot(l)
#print("herey")
#str(l)
#data(l)
#summary(l)
#data(data_3)
#summary(data_3)
#ggplot(as.data.frame(table(l)))
#plot (l$date, l$name)
#lines(l$period1, l$period2, col="blue")
#print(l$date[1])
#print(length(l$date))
#print(length(l))
print("hello")
datedstocklists <- list()
days <- 8
topbottom <- 5
count <- days
mytableintervaldays <- 5
date <- "2016-05-02"
dateindex <- getstockdate(listdate, date)
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
print(length(datedstocklists))
period <- 3
alist <- getlistanddiffperiod(datedstocklists, listid, listdate, days, 5, period)
periodmaps <- alist[1]
stocklistperiod <- alist[2]
#str(periodmaps[1][1])
#print("hello")
#print(length(datedstocklists))
#str(datedstocklists[1])
mytopperiod(datedstocklists, stocklistperiod, periodmaps, period, topbottom)

gettopchart(days, topbottom, stocklistperiod, period)
rise <- getrising(days, periodmaps, stocklistperiod, period)
risetopids <- head(names(rise[[1]]))

# close the connection
dbDisconnect(con)
dbUnloadDriver(drv)
#rm(list = ls())
print("ending")
#return

