o=0
g=0
i=0
q=0
d=0
if [ "$1" = "" ]; then
    q=1
    g=1
    i=1
    o=1
fi

while (( "$#" )); do
if [ "$1" = "q" ]; then
    q=1
fi

if [ "$1" = "g" ]; then
    g=1
fi

if [ "$1" = "i" ]; then
    i=1
fi

if [ "$1" = "o" ]; then
    o=1
fi

if [ "$1" = "d" ]; then
    d=1
fi
shift
done

if [ $o -eq 1 ]; then
       sh datasetclassifytest.sh tf220 mnist 1
       sh datasetclassifytest.sh tf221 mnist 1
fi

if [ $q -eq 1 ]; then
    sh datasetqclassifytest.sh tfq mnist 1
    sh datasetqclassifytest.sh tfq2181 mnist 1
fi

if [ $g -eq 1 ]; then
    sh gptclassifytest.sh tf220gpt 1
fi

if [ $i -eq 1 ]; then
    sh imgclassifytest.sh tf220 1
fi

if [ $d -eq 1 ]; then
    sh diffusionclassifytest.sh tf221 1
fi

#python datasetclassifytest.py "" 1

