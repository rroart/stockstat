eval "$(conda shell.bash hook)"
conda activate $1
python datasetqclassifytest.py MyTestCase.test_something $2 $3
