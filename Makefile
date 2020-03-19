all: xhtml pdf

xhtml: DOCUMENTATION.xml
	xmlto xhtml DOCUMENTATION.xml

pdf: DOCUMENTATION.pdf

%.fo: %
	xsltproc -xinclude -o $@ /usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl $<

%.pdf: %.fo
	fop $< -pdf $@

SUBDIRS = core iclij-core

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

%.xml: %.xml.tmpl
	envsubst < $< > $@

.PHONY: $(SUBDIRS)
