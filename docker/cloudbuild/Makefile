VERSION = 0.6-SNAPSHOT
SUBDIRS = tensorflow spark pdfred eureka web iclij-web iclij-webcore core iclij-core
SUBDIRS = dbclone pytorchdata tensorflowdata
SUBDIRS2 = core iclij-core

all: $(SUBDIRS)
$(SUBDIRS):
	$(MAKE) -C $@ 

core:
ifneq ($(STOCKSTATTMPL),)
	rsync -a $$STOCKSTATTMPL core/stockstat.xml.tmpl
endif
	$(MAKE) -C $@ -f ../Makefile stockstat.xml

iclij-core:
ifneq ($(ICLIJTMPL),)
	rsync -a $$ICLIJTMPL iclij-core/iclij.xml.tmpl
endif
	$(MAKE) -C $@ -f ../Makefile iclij.xml

iclij-webcore:
ifneq ($(ICLIJTMPL),)
	rsync -a $$ICLIJTMPL iclij-webcore/iclij.xml.tmpl
endif
	$(MAKE) -C $@ -f ../Makefile iclij.xml

%.xml: %.xml.tmpl
	envsubst < $< > $@

.PHONY: all $(SUBDIRS) $(SUBDIRS2)
