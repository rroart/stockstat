o=0
m=0
d=0

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

if [ "$1" = "d" ]; then
    d=1
fi
shift
done

if [ $o -eq 1 ]; then
    sh datasetclassifytest.sh pt29 mnist 1
    sh datasetclassifytest.sh pt212 mnist 1
fi

if [ $m -eq 1 ]; then
    sh midiclassifytest.sh pt29 pytorchGPTMIDIConfig 1 "$TMPDIR"
    sh midiclassifytest.sh pt29 pytorchGPTMIDIRPRConfig 1 "$TMPDIR"
    #sh midiclassifytest.sh pt29 pytorchGPTMIDIMMTConfig 1 "$TMPDIR"
    sh midiclassifytest.sh pt212 pytorchGPTMIDIRPRConfig 1 "$TMPDIR"
    sh midiclassifytest.sh pt212 pytorchGPTMIDIMMTConfig 1 "$TMPDIR"
    #sh midiclassifytest.sh mtmt pytorchGPTMIDIMMTConfig 1 "$TMPDIR"
    sh midiclassifytest.sh ptl264 pytorchGPTMIDIFigaroConfig 1 "$TMPDIR"
    #if conda env list 2>&1|grep -q ptl255; then 
	#sh midiclassifytest.sh ptl255 pytorchGPTMIDIFigaroConfig 1 "$TMPDIR"
    #fi
fi

if [ $d -eq 1 ]; then
    sh diffusionclassifytest.sh pt212 1 0 0
    #cifar
fi

