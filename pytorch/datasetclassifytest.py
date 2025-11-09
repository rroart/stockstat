import unittest

import config
import datasetcli as cli

steps = 2
take = None

#todo predict

losses = [ "cross_entropy", "nl" ]
# "ctc", "hingeembedding", "gaussiannl", , "cosineembedding"
#losses = [ ]

optimizers = [ "adadelta", "adafactor", "adagrad", "adam", "adamw", "adamax", "asgd", "nadam", "radam", "rmsprop", "rprop", "sgd" ]
# "sparseadam", "lbfgs",
#optimizers = [ ]

activations = [ "elu", "hard_shrink", "hard_sigmoid", "hard_tanh", "hard_swish", "leakyrelu", "log_sigmoid", "prelu", "relu", "relu6", "rrelu", "selu", "celu", "gelu", "sigmoid", "silu", "mish", "soft_plus", "soft_shrink", "soft_sign", "tanh", "tanh_shrink", "soft_min", "softmax", "log_soft_max" ]
# "multihead_attention" "threshold","glu","soft_max_2d",, "adaptive_log_soft_max_with_loss"

class MyTestCase(unittest.TestCase):
    def test_something(self):
        steps = 10
        testlist = [ config.PYTORCHMLP, config.PYTORCHRNN, config.PYTORCHLSTM, config.PYTORCHGRU, config.PYTORCHCNN, config.PYTORCHCNN2 ]

        testlist = [ config.PYTORCHRNN ]
        testlist = [ config.PYTORCHGRU ]
        testlist = [ config.PYTORCHMLP ]
        testlist = [ config.PYTORCHCNN, config.PYTORCHCNN2 ]
        testlist = [ config.PYTORCHCNN2 ]

        for test in testlist:
            override = {'convlayers': 3, 'layers': 2, 'steps' : 1, 'kernelsize' : 4, 'maxpool': 4 }
            result = cli.learn(ds = 'mnist', cf = test, take = take, steps = steps, override = override)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        # here

    def test_cnn(self):
        steps = 1

        testlist = [ config.PYTORCHCNN, config.PYTORCHCNN2 ]

        for test in testlist:
            for i in range(1, 4):
                for j in range(1, 3):
                    for k in [1, 2, 3, 4]:
                        for l in [1, 2, 3, 4]:
                            print("i j k", i, j, k)
                            override = {'convlayers': i, 'layers': j, 'maxpool': k, 'kernelsize' : l }
                            result = cli.learn(ds = 'mnist', cf = test, take = take, steps = steps, override = override)
                            print(result)
            #self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
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
