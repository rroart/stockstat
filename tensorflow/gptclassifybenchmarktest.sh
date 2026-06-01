eval "$(conda shell.bash hook)"
conda activate $1
python gptclassifybenchmarktest.py MyTestCase.test_something $2 $3
