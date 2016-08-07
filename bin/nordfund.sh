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

xsltproc --encoding ISO-8859-1 --novalid --html $D -o nordfund.xml $DIRNAME/nordfund.xsl sok.html\?sok\=1

# --encoding ISO-8859-1 -v
