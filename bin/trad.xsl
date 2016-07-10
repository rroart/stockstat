<?xml version="1.0" encoding="ISO-8859-1"?>

<!DOCTYPE xsl:stylesheet [
<!ENTITY laquo "&#38;#x00ab;">
<!ENTITY raquo "&#38;#x00bb;">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		version="1.0"
		exclude-result-prefixes="#default">

  <xsl:output method="xml"
	      indent="no"
	      doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	      doctype-system="xhtml1-transitional.dtd"/>

  <xsl:template name="fixdate">
    <xsl:param name="indate" />
    <xsl:variable name="inday" select="substring($indate, 5, 2)"/>
    <xsl:variable name="inmonth" select="substring($indate, 1, 3)"/>
    <xsl:variable name="outmonth">
    <xsl:choose>
      <xsl:when test="$inmonth = 'Jan'">
	<xsl:value-of select="'01'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'Feb'">
	<xsl:value-of select="'02'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'Mar'">
	<xsl:value-of select="'03'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'Apr'">
	<xsl:value-of select="'04'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'May'">
	<xsl:value-of select="'05'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'Jun'">
	<xsl:value-of select="'06'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'Jul'">
	<xsl:value-of select="'07'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'Aug'">
	<xsl:value-of select="'08'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'Sep'">
	<xsl:value-of select="'09'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'Oct'">
	<xsl:value-of select="'10'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'Nov'">
	<xsl:value-of select="'11'"/>
      </xsl:when>
      <xsl:when test="$inmonth = 'Dec'">
	<xsl:value-of select="'12'"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="'error'"/>
      </xsl:otherwise>
    </xsl:choose>
    </xsl:variable>
    <xsl:variable name="outyear">
      <xsl:choose>
	<xsl:when test="$outmonth = '12' and $current-month = '01'">
	  <xsl:value-of select="$current-year - 1"/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="$current-year"/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="concat($inday, '.', $outmonth, '.', $outyear)"/>
  </xsl:template>

  <xsl:template name="string-replace-all">
  <xsl:param name="text" />
  <xsl:param name="replace" />
  <xsl:param name="by" />
  <xsl:choose>
    <xsl:when test="$text = '' or $replace = ''or not($replace)" >
      <!-- Prevent this routine from hanging -->
      <xsl:value-of select="$text" />
    </xsl:when>
    <xsl:when test="contains($text, $replace)">
      <xsl:value-of select="substring-before($text,$replace)" />
      <xsl:value-of select="$by" />
      <xsl:call-template name="string-replace-all">
	<xsl:with-param name="text" select="substring-after($text,$replace)" />
	<xsl:with-param name="replace" select="$replace" />
	<xsl:with-param name="by" select="$by" />
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$text" />
    </xsl:otherwise>
  </xsl:choose>
  </xsl:template>

  <xsl:template match="processing-instruction('Pub') | processing-instruction('IS10744')"/>
</xsl:stylesheet>
