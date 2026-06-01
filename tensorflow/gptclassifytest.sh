eval "$(conda shell.bash hook)"
conda activate $1
python gptclassifytest.py MyTestCase.test_something "" $2
