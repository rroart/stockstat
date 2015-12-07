#!/bin/bash

while getopts d FLAG; do
  case $FLAG in
  d)
          D="--debug"
;;
  esac
done

cd /tmp

touch xhtml1-transitional.dtd

xsltproc --encoding UTF-8 --novalid --html $D -o morncat.xml /home/roart/src/stockstat/bin/morncat.xsl categoryoverview.aspx

# --encoding ISO-8859-1 -v
