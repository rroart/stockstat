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

xsltproc --encoding UTF-8 --novalid --html $D -o mornind.xml $DIRNAME/mornind.xsl indexoverview.aspx

# --encoding ISO-8859-1 -v
