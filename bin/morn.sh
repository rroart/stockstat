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

xsltproc -v --encoding ISO-8859-1 --novalid --html $D -o morn.xml /home/roart/src/stockstat/bin/morn.xsl indexoverview.aspx

# --encoding ISO-8859-1 -v
