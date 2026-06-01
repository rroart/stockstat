eval "$(conda shell.bash hook)"
conda activate $1
python midiclassifytest.py $2 $3
