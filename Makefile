all: xhtml pdf

xhtml: docbook/DOCUMENTATION.xml docbookhtml/figures/architecture.svg
	xmlto -o docbookhtml xhtml docbook/DOCUMENTATION.xml

pdf: DOCUMENTATION.pdf

%.fo: %
	xsltproc -xinclude -o $@ /usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl $<

docbookhtml/figures/%.svg: docbook/figures/%.fig
	mkdir -p docbookhtml/figures
	fig2dev $< $@

%.pdf: %.fo
	fop $< -pdf $@

SUBDIRS = common local
#weba iclij-weba

common:
	mkdir -p conf
ifneq ($(STOCKSTATTMPL),)
	rsync -a $$STOCKSTATTMPL conf/stockstat.xml.tmpl
endif
	$(MAKE) -C conf -f ../Makefile stockstat.xml

weba:
	weba/scripts/genenv.sh

local:
	mkdir -p conf
ifneq ($(LOCALTMPL),)
	rsync -a $$LOCALTMPL conf/local.xml.tmpl
endif
	$(MAKE) -C conf -f ../Makefile local.xml

iclij-weba:
	iclij/iclij-weba/scripts/genenv.sh

%.xml: %.xml.tmpl
	envsubst < $< > $@

python-bom:
	echo

.PHONY: $(SUBDIRS)
