eval "$(conda shell.bash hook)"
conda activate $1
python diffusionclassifytest.py $2 $3 $4
