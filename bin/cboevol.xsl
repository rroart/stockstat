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
      <xsl:apply-templates select=".//div/tr[@class='alt']"/>
      </body>
    </xsl:template>

    <!--xsl:template match="div[@class='navigation']"/-->

    <!--xsl:template match="tr[@class='sort']"/-->

    <!--xsl:template match="tfoot/tr"/-->

    <!--xsl:template match="thead/tr"/-->

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
      <xsl:if test="not(td[8]/text() = '-')">
      <row>
	<id>
	  <xsl:copy-of select="td[1]/a/text()"/>
	</id>
	<marketid>cboevol</marketid>
	<date>
	  <xsl:copy-of select="$current-date"/>
	</date>
	<name>
	  <xsl:copy-of select="td[1]/a/text()"/>
	</name>
	<period1>
	  <xsl:copy-of select="td[2]/text()"/>
	</period1>
      </row>
      </xsl:if>
    </xsl:template>

    <xsl:template match="script"/>

    <!--xsl:template match="span"/-->

</xsl:stylesheet>
