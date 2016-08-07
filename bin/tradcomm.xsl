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
	  <marketid>tradcomm</marketid>
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
      <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="tbody">
      <xsl:apply-templates select="tr"/>
    </xsl:template>

    <xsl:template match="tr">
      <xsl:if test="@data-symbol">
      <row>
	<id>
	  <xsl:value-of select="@data-symbol"/>
	</id>
	<marketid>tradcomm</marketid>
	<date>
	  <xsl:call-template name="fixdate">
	    <xsl:with-param name="indate" select="translate(td[9]/text(), '&#x20;&#x9;&#xD;&#xA;', '')"/>
	  </xsl:call-template>
	</date>
	<name>
	  <xsl:copy-of select="td[1]/a/b/text()"/>
	</name>
	<price>
	  <xsl:copy-of select="translate(td[3]/text(), '&#x20;&#x9;&#xD;&#xA;&#x2c;', '')"/>
	</price>
	<currency>USD</currency>
	<period1>
	  <xsl:copy-of select="translate(td[5]/text(), '&#x20;&#x9;&#xD;&#xA;&#x25;&#x2c;', '')"/>
	</period1>
	<period2>
	  <xsl:copy-of select="translate(td[6]/text(), '&#x20;&#x9;&#xD;&#xA;&#x25;&#x2c;', '')"/>
	</period2>
	<period3>
	  <xsl:copy-of select="translate(td[7]/text(), '&#x20;&#x9;&#xD;&#xA;&#x25;&#x2c;', '')"/>
	</period3>
	<period4>
	  <xsl:copy-of select="translate(td[8]/text(), '&#x20;&#x9;&#xD;&#xA;&#x25;&#x2c;', '')"/>
	</period4>
      </row>
      </xsl:if>
    </xsl:template>

    <xsl:template match="script"/>

    <!--xsl:template match="span"/-->

</xsl:stylesheet>
