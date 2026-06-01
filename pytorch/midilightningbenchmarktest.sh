eval "$(conda shell.bash hook)"

if conda env list 2>&1|grep -q ptl254; then 
conda activate ptl255
python midiclassifybenchmarktest.py figaro 1
fi

conda activate ptl264
python midiclassifybenchmarktest.py figaro 1
