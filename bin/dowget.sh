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
