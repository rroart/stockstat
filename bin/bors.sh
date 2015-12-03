#!/bin/bash

while getopts d FLAG; do
  case $FLAG in
  d)
          D="--debug"
;;
  esac
done

xsltproc --encoding ISO-8859-1 --novalid --html $D -o bors.xml bors.xsl sok.html\?sok\=1

# --encoding ISO-8859-1 -v
