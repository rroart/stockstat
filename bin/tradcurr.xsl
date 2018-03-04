<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY SPACES "'&#x20;&#x9;&#xD;&#xA;'" >
<!ENTITY SPACESPERCENT "'&#x20;&#x9;&#xD;&#xA;&#x25;'" >
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="1.0">

  <xsl:include href="trad.xsl"/>
  
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
	  <marketid>tradcurr</marketid>
	  <period1>1 d</period1>
	  <period2>1 w</period2>
	  <period3>1 m</period3>
	  <period4>1 y</period4>
	</meta>
      <rows>
      <xsl:apply-templates select=".//div/table"/>
      </rows>
      </market>
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
      <xsl:if test="not(translate(thead/tr/th[1]/text(), '&#x20;&#x9;&#xD;&#xA;', '') = 'Major')">
      <xsl:apply-templates select="*"/>
      </xsl:if>
    </xsl:template>

    <xsl:template match="tbody">
      <xsl:apply-templates select="tr"/>
    </xsl:template>

    <xsl:template match="tr">
      <xsl:if test="@data-symbol">
      <row>
	<id>
	  <xsl:value-of select="td[2]/a/b/text()"/>
	</id>
	<marketid>tradcurr</marketid>
	<date>
	  <xsl:call-template name="fixdate">
	    <xsl:with-param name="indate" select="translate(td[9]/text(), '&#x20;&#x9;&#xD;&#xA;', '')"/>
	  </xsl:call-template>
	</date>
	<name>
	  <xsl:copy-of select="td[2]/a/b/text()"/>
	</name>
	<price>
	  <xsl:copy-of select="translate(td[3]/text(), '&#x20;&#x9;&#xD;&#xA;&#x2c;', '')"/>
	</price>
	<currency>
	  <xsl:value-of select="@data-ticker"/>
	</currency>
	<xsl:variable name="content1">
	  <xsl:copy-of select="translate(td[5]/text(), '&#x20;&#x9;&#xD;&#xA;&#x25;&#x2c;', '')"/>
	</xsl:variable>
	<xsl:if test="string-length($content1) > 0">
          <period1>
            <xsl:value-of select="$content1"/>
          </period1>
	</xsl:if>
	<xsl:variable name="content2">
	  <xsl:copy-of select="translate(td[6]/text(), '&#x20;&#x9;&#xD;&#xA;&#x25;&#x2c;', '')"/>
	</xsl:variable>
	<xsl:if test="string-length($content2) > 0">
          <period2>
            <xsl:value-of select="$content2"/>
          </period2>
	</xsl:if>
	<xsl:variable name="content3">
	  <xsl:copy-of select="translate(td[7]/text(), '&#x20;&#x9;&#xD;&#xA;&#x25;&#x2c;', '')"/>
	</xsl:variable>
	<xsl:if test="string-length($content3) > 0">
          <period3>
            <xsl:value-of select="$content3"/>
          </period3>
	</xsl:if>
	<xsl:variable name="content4">
	  <xsl:copy-of select="translate(td[8]/text(), '&#x20;&#x9;&#xD;&#xA;&#x25;&#x2c;', '')"/>
	</xsl:variable>
	<xsl:if test="string-length($content4) > 0">
          <period4>
            <xsl:value-of select="$content4"/>
          </period4>
	</xsl:if>
      </row>
      </xsl:if>
    </xsl:template>

    <xsl:template match="script"/>

    <!--xsl:template match="span"/-->

</xsl:stylesheet>
