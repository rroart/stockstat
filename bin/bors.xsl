<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="1.0">

    <xsl:output method="xml"
        indent="yes"
        xml:space="preserve"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="xhtml1-transitional.dtd"/>

    <xsl:template match="html">
      <xsl:apply-templates select="body"/>
    </xsl:template>

    <xsl:template match="body">
      <body>
      <xsl:apply-templates select="//div[@id='resultatdiv']"/>
      </body>
    </xsl:template>

    <xsl:template match="div[@class='navigation']"/>

    <xsl:template match="tr[@class='sort']"/>

    <xsl:template match="tfoot/tr"/>

    <xsl:template match="thead/tr"/>

    <xsl:template match="div">
      <xsl:apply-templates select="*"/>
      <!--xsl:apply-templates select="table"/-->
    </xsl:template>

    <xsl:template match="table">
      <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="tbody">
      <xsl:apply-templates select="tr"/>
    </xsl:template>

    <xsl:template match="tr">
      <row>
	<id>
	  <xsl:variable name="firstpart" select="substring-after(td[2]/div/a/@href, '=')"/>
	  <xsl:value-of select="substring-before($firstpart, '&amp;')"/>
	</id>
	<date>
	  <xsl:copy-of select="td[10]/text()"/>
	</date>
	<name>
	  <xsl:copy-of select="td[2]/div/a/text()"/>
	</name>
	<day>
	  <xsl:copy-of select="td[5]/span/text()"/>
	</day>
	<week>
	  <xsl:copy-of select="td[6]/span/text()"/>
	</week>
	<month>
	  <xsl:copy-of select="td[7]/span/text()"/>
	</month>
	<thisyear>
	  <xsl:copy-of select="td[8]/span/text()"/>
	</thisyear>
	<price>
	  <xsl:copy-of select="td[3]/text()"/>
	</price>
	<currency>
	  <xsl:copy-of select="td[4]/text()"/>
	</currency>
      </row>
    </xsl:template>

    <xsl:template match="script"/>

</xsl:stylesheet>
