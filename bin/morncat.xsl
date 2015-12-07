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
      <xsl:apply-templates select="div//table[@id='CategoryOverviewTable']/tbody"/>
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
	  <xsl:value-of select="substring-after(td[1]/a/@href, '=')"/>
	</id>
	<marketid>3</marketid>
	<date>
	  <xsl:copy-of select="td[8]/text()"/>
	</date>
	<name>
	  <xsl:copy-of select="td[1]/a/text()"/>
	</name>
	<period1>
	  <xsl:copy-of select="td[2]/text()"/>
	</period1>
	<period2>
	  <xsl:copy-of select="td[3]/text()"/>
	</period2>
	<period3>
	  <xsl:copy-of select="td[4]/text()"/>
	</period3>
	<period4>
	  <xsl:copy-of select="td[5]/text()"/>
	</period4>
	<period5>
	  <xsl:copy-of select="td[6]/text()"/>
	</period5>
      </row>
      </xsl:if>
    </xsl:template>

    <xsl:template match="script"/>

    <!--xsl:template match="span"/-->

</xsl:stylesheet>
