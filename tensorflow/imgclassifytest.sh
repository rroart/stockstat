eval "$(conda shell.bash hook)"
conda activate $1
python imgclassifytest.py MyTestCase.test_something "" $2
