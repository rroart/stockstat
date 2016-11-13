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
      <market>
	<meta>
	  <marketid>nordhist</marketid>
	  <period1>3 m</period1>
	  <period2>6 m</period2>
	  <period3>1 y</period3>
	  <period4>3 y</period4>
	  <period5>5 y</period5>
	  <period6>10 y</period6>
	</meta>
      <rows>
      <xsl:apply-templates select="//div[@id='resultatdiv']"/>
      </rows>
      </market>
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
      <xsl:if test="not(td[9]/text() = '&#160;')">
      <row>
	<id>
	  <xsl:variable name="firstpart" select="substring-after(td[2]/div/a/@href, '=')"/>
	  <xsl:value-of select="substring-before($firstpart, '&amp;')"/>
	</id>
	<marketid>nordhist</marketid>
	<date>
	  <xsl:copy-of select="td[9]/text()"/>
	</date>
	<name>
	  <xsl:copy-of select="td[2]/div/a/text()"/>
	</name>
	<period1>
	  <xsl:copy-of select="td[3]/span/text()"/>
	</period1>
	<period2>
	  <xsl:copy-of select="td[4]/span/text()"/>
	</period2>
	<period3>
	  <xsl:copy-of select="td[5]/span/text()"/>
	</period3>
	<period4>
	  <xsl:copy-of select="td[6]/span/text()"/>
	</period4>
	<period5>
	  <xsl:copy-of select="td[7]/span/text()"/>
	</period5>
	<period6>
	  <xsl:copy-of select="td[8]/span/text()"/>
	</period6>
      </row>
      </xsl:if>
    </xsl:template>

    <xsl:template match="script"/>

</xsl:stylesheet>
