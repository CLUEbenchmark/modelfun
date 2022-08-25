PYTHON=/home/modelfun/anaconda3/envs/paddle/bin/python


for p in 2 3 #5
do(
for l in 5e-5 1e-5 #5e-4 5e-3
    do
        for r in 0 0.2 0.1 #0.01 0.001
            do
                echo $PYTHON ptunning.py --task_name eprstmt --p_embedding_num $p --learning_rate $l --rdrop_coef $r
                $PYTHON ptunning.py --task_name eprstmt --p_embedding_num $p --learning_rate $l --rdrop_coef $r
            done
    done
)
done