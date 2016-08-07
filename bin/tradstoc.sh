#!/bin/bash

DIRNAME=`dirname $0`

while getopts d FLAG; do
  case $FLAG in
  d)
          D="--debug"
;;
  esac
done

cd /tmp

touch xhtml1-transitional.dtd

YEAR=`date +%Y`
MONTH=`date +%m`
DAY=`date +%d`

xsltproc --stringparam current-year $YEAR --stringparam current-month $MONTH --encoding UTF-8 --novalid --html $D -o tradstoc.xml $DIRNAME/tradstoc.xsl stocks

# --encoding ISO-8859-1 -v
