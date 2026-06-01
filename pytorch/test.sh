o=0
m=0

if [ "$1" = "" ]; then
    o=1
    m=1
fi

while (( "$#" )); do
if [ "$1" = "m" ]; then
    m=1
fi

if [ "$1" = "o" ]; then
    o=1
fi
shift
done

if [ $o -eq 1 ]; then
    sh datasetclassifytest.sh pt29 mnist 1
    sh datasetclassifytest.sh pt212 mnist 1
fi

if [ $m -eq 1 ]; then
    if conda env list 2>&1|grep -q ptlxxx; then 
	sh midiclassifytest.sh ptl figaro 1
    fi
    sh midiclassifytest.sh ptl264 figaro 1
fi

