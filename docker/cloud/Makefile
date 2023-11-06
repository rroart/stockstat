VERSION = 0.6-SNAPSHOT
SUBDIRS = tensorflowdata pytorchdata spark pdfred eureka web iclij-web iclij-webcore core iclij-core
#SUBDIRS = dbclone
#pytorchdata tensorflowdata
SUBDIRS = core iclij-core iclij-webcore dbclone iclij tensorflowdata pytorchdata sparkdata
#webr iclij-webr

all: $(SUBDIRS)
#$(SUBDIRS2)

dbclone tensorflowdata pytorchdata sparkdata:
	$(MAKE) -C $@ 

core:
ifneq ($(STOCKSTATTMPL),)
	rsync -a $$STOCKSTATTMPL core/stockstat.xml.tmpl
endif
	$(MAKE) -C $@ -f ../Makefile stockstat.xml
#	$(MAKE) -C $@ -f ../Makefile stockstat-$@-${VERSION}.jar

iclij-core:
ifneq ($(ICLIJTMPL),)
	rsync -a $$ICLIJTMPL iclij-core/iclij.xml.tmpl
endif
	$(MAKE) -C $@ -f ../Makefile iclij.xml
#	$(MAKE) -C $@ -f ../Makefile stockstat-$@-${VERSION}.jar

iclij-webcore:
ifneq ($(ICLIJTMPL),)
	rsync -a $$ICLIJTMPL iclij-webcore/iclij.xml.tmpl
endif
	$(MAKE) -C $@ -f ../Makefile iclij.xml
#	$(MAKE) -C $@ -f ../Makefile stockstat-$@-${VERSION}.jar

webr iclij-webr:
	mkdir -p $@/files
	rsync -a ../../distribution/target/stockstat-distribution-0.6-SNAPSHOT-bin/stockstat-distribution-0.6-SNAPSHOT/$@/docroot/ $@/files/docroot

iclij:
ifneq ($(ICLIJTMPL),)
	rsync -a $$ICLIJTMPL iclij/iclij.xml.tmpl
endif
	$(MAKE) -C $@ -f ../Makefile iclij.xml
ifneq ($(ISIMTMPL),)
	rsync -a $$ISIMTMPL iclij/isim.xml.tmpl
endif
	$(MAKE) -C $@ -f ../Makefile isim.xml
ifneq ($(IEVOLVETMPL),)
	rsync -a $$IEVOLVETMPL iclij/ievolve.xml.tmpl
endif
	$(MAKE) -C $@ -f ../Makefile ievolve.xml
ifneq ($(STOCKSTATTMPL),)
	rsync -a $$STOCKSTATTMPL iclij/stockstat.xml.tmpl
endif
	$(MAKE) -C $@ -f ../Makefile stockstat.xml
#	$(MAKE) -C $@ -f ../Makefile stockstat-$@-${VERSION}.jar

%.xml: %.xml.tmpl
	envsubst < $< > $@

stockstat-%-${VERSION}.jar: ../../../distribution/target/stockstat-distribution-0.6-SNAPSHOT-bin/stockstat-distribution-0.6-SNAPSHOT/lib/stockstat-%-${VERSION}.jar
	 mkdir -p files && ln -f ../../../distribution/target/stockstat-distribution-0.6-SNAPSHOT-bin/stockstat-distribution-0.6-SNAPSHOT/lib/$@ files/$@

xcore xiclij-core xiclij-webcore:
	cd $@ && mkdir -p files && ln -f ../../../distribution/target/stockstat-distribution-0.6-SNAPSHOT-bin/stockstat-distribution-0.6-SNAPSHOT/lib/stockstat-$@-${VERSION}.jar files/stockstat-$@-${VERSION}.jar

.PHONY: all $(SUBDIRS) $(SUBDIRS2)
