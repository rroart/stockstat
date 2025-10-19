import unittest

import config
import datasetcli as cli

steps = 2
take = None

losses = [ "cross_entropy", "nl" ]
# "ctc", "hingeembedding", "gaussiannl", , "cosineembedding"
#losses = [ ]

optimizers = [ "adadelta", "adafactor", "adagrad", "adam", "adamw", "adamax", "asgd", "nadam", "radam", "rmsprop", "rprop", "sgd" ]
# "sparseadam", "lbfgs",
#optimizers = [ ]

activations = [ "elu", "hard_shrink", "hard_sigmoid", "hard_tanh", "hard_swish", "leakyrelu", "log_sigmoid", "prelu", "relu", "relu6", "rrelu", "selu", "celu", "gelu", "sigmoid", "silu", "mish", "soft_plus", "soft_shrink", "soft_sign", "tanh", "tanh_shrink", "soft_min", "soft_max", "log_soft_max" ]
# "multihead_attention" "threshold","glu","soft_max_2d",, "adaptive_log_soft_max_with_loss"

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testlist = [ config.PYTORCHMLP, config.PYTORCHRNN, config.PYTORCHLSTM, config.PYTORCHGRU, config.PYTORCHCNN, config.PYTORCHCNN2 ]

        testlist = [ config.PYTORCHRNN ]
        #testlist = [ config.PYTORCHMLP ]
        testlist = [ config.PYTORCHGRU ]

        for test in testlist:
            result = cli.learn(ds = 'mnist', cf = test, take = take, steps = steps)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        # here

    def test_comb(self):
        steps = 1
        testlist = [ config.PYTORCHMLP, config.PYTORCHRNN, config.PYTORCHLSTM, config.PYTORCHGRU, config.PYTORCHCNN, config.PYTORCHCNN2 ]

        #testlist = [ config.PYTORCHRNN ]
        #testlist = [ config.PYTORCHMLP ]
        #testlist = [ config.PYTORCHGRU ]

        for test in testlist:
            for loss in losses:
                override = { 'loss' : loss }
                result = cli.learn(ds = 'mnist', cf = test, take = take, steps = steps, override = override)
                print(result)
                self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
            for optimizer in optimizers:
                override = { 'optimizer' : optimizer }
                result = cli.learn(ds = 'mnist', cf = test, take = take, steps = steps, override = override)
                print(result)
                self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
            for activation in activations:
                override = { 'activation' : activation }
                result = cli.learn(ds = 'mnist', cf = test, take = take, steps = steps, override = override)
                print(result)
                self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        # here

if __name__ == '__main__':
    unittest.main()
