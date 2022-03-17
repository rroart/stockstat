all: xhtml pdf

xhtml: DOCUMENTATION.xml
	xmlto xhtml DOCUMENTATION.xml

pdf: DOCUMENTATION.pdf

%.fo: %
	xsltproc -xinclude -o $@ /usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl $<

%.pdf: %.fo
	fop $< -pdf $@

SUBDIRS = core iclij-core
#weba iclij-weba

core:
	mkdir -p conf
ifneq ($(STOCKSTATTMPL),)
	rsync -a $$STOCKSTATTMPL conf/stockstat.xml.tmpl
endif
	$(MAKE) -C conf -f ../Makefile stockstat.xml

iclij-core:
	mkdir -p conf
ifneq ($(ICLIJTMPL),)
	rsync -a $$ICLIJTMPL conf/iclij.xml.tmpl
endif
	$(MAKE) -C conf -f ../Makefile iclij.xml

iclij-sim:
	mkdir -p conf
ifneq ($(ISIMTMPL),)
	rsync -a $$ISIMTMPL conf/isim.xml.tmpl
endif
	$(MAKE) -C conf -f ../Makefile isim.xml

iclij-evolve:
	mkdir -p conf
ifneq ($(IEVOLVETMPL),)
	rsync -a $$IEVOLVETMPL conf/ievolve.xml.tmpl
endif
	$(MAKE) -C conf -f ../Makefile ievolve.xml

weba:
	weba/scripts/genenv.sh

iclij-weba:
	iclij/iclij-weba/scripts/genenv.sh

%.xml: %.xml.tmpl
	envsubst < $< > $@

.PHONY: $(SUBDIRS)
