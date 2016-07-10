#!/bin/sh
cd /tmp
mv sok.html\?sok\=1 sok.html\?sok\=1.old
wget "https://www.nordnet.no/mux/web/fonder/sok.html?sok=1"
mv indexoverview.aspx indexoverview.aspx.old
wget http://www.morningstar.no/no/tools/indexoverview.aspx
mv categoryoverview.aspx categoryoverview.aspx.old
wget http://www.morningstar.no/NO/tools/categoryoverview.aspx
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
