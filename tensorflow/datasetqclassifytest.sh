eval "$(conda shell.bash hook)"
conda activate $1
python datasetqclassifytest.py mnist "" $2
