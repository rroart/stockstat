export ICLIJTMPL=config/icore/iclij.xml.tmpl
export FUTUREDAYS="[10]"
export THRESHOLD="[ 1.0 ]"
export SPARK=false
export TENSORFLOW=true
export PYTORCH=true
export GEM=false
export ML=true
export FINDPROFIT=true
export IMPROVEPROFIT=false
export IMPROVEFILTER=false
export EVOLVE=false
export CROSSTEST=false
export DATASET=false
export MARKETS='<markets>
    <marketlist>
      <market id="fred">
        <config>{ "market" : "fred", "persisttime" : 120, "findtime" : 7, "evolvetime" : 60, "improvetime" : 150, "filtertime": 160, "startoffset" : 0, "crosstime" : 90, "mlmarkets" : [ ] }</config>
        <filter>
          { "confidence" : 0.75, "recordage" : 7 }
        </filter>
      </market>
    </marketlist>
  </markets>'
