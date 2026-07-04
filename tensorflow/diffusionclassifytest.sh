eval "$(conda shell.bash hook)"
conda activate $1
python diffusionclassifytest.py MyTestCase.test_something "" $2
