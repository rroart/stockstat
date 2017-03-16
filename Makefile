all: xhtml pdf

xhtml: DOCUMENTATION.xml
	xmlto xhtml DOCUMENTATION.xml

pdf: DOCUMENTATION.pdf

%.fo: %
	xsltproc -xinclude -o $@ /usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl $<

%.pdf: %.fo
	fop $< -pdf $@
