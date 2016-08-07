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

DATE=`date +%d.%m.%Y`
xsltproc --stringparam current-date $DATE --encoding UTF-8 --novalid --html $D -o cboevol.xml $DIRNAME/cboevol.xsl introduction.aspx

# --encoding ISO-8859-1 -v
