VERSION = 0.5-SNAPSHOT
SUBDIRS = tensorflow spark pd eureka web iclij-web core iclij-core

all: $(SUBDIRS)

eureka:
	cd $@ && mkdir -p files && ln -f ../../lib/stockstat-$@-${VERSION}.jar files/stockstat-$@-${VERSION}.jar && docker build -t="stockstat-$@" .

web iclij-web:
	cd $@ && mkdir -p files && ln -f ../../lib/jetty-runner-9.4.12.v20180830.jar files/
	cd $@ && mkdir -p files && ln -f ../../lib/stockstat-$@-${VERSION}.war files/stockstat-$@-${VERSION}.war && docker build -t="stockstat-$@" .

core:
	cd $@ && mkdir -p files && ln -f stockstat.xml files
	cd $@ && mkdir -p files && ln -f ../../lib/stockstat-$@-${VERSION}.jar files/stockstat-$@-${VERSION}.jar && docker build -t="stockstat-$@" .

iclij-core:
	cd $@ && mkdir -p files && ln -f iclij.xml files
	cd $@ && mkdir -p files && ln -f ../../lib/stockstat-$@-${VERSION}.jar files/stockstat-$@-${VERSION}.jar && docker build -t="stockstat-$@" .

.PHONY: all $(SUBDIRS)
