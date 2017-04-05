#!/bin/sh
cd /tmp
domv="0";
while getopts 'm' flag; do
    case "${flag}" in
	m) domv="1" ;;
    esac
done
if [ "$domv" = "1" ]; then
DATE=`date +%Y.%m.%d.%H`
DIRNAME=`dirname $0`
mkdir $DIRNAME/$DATE
mv /tmp/*.xml $DIRNAME/$DATE
fi
mv introduction.aspx introduction.aspx.old
wget http://www.cboe.com/micro/volatility/introduction.aspx
#mv kitco-gold-index.html kitco-gold-index.html.old
#wget http://www.kitco.com/kitco-gold-index.html
mv currencies currencies.old
wget http://www.tradingeconomics.com/currencies
sed -e 's/\x0D$//' -i currencies
mv stocks stocks.old
wget http://www.tradingeconomics.com/stocks
sed -e 's/\x0D$//' -i stocks
mv commodities commodities.old
wget http://www.tradingeconomics.com/commodities
sed -e 's/\x0D$//' -i commodities
