import unittest

import config
import datasetcli as cli

steps = 10
take = None

losses = [ "sparse_categorical_crossentropy" ]
# "categorical_crossentropy",, "ctc"

optimizers = [ "adadelta", "adagrad", "adam", "adamw", "adamax", "ftrl", "nadam", "rmsprop", "sgd", "lion",
              "lamb", "adafactor" ]
# "muon", "loss_scale_optimizer", 
#optimizers = [ ]

activations = [ "celu", "elu", "exponential", "gelu", "hard_shrink", "hard_sigmoid", "hard_silu", "hard_tanh", "leaky_relu", "linear", "log_sigmoid", "log_softmax", "mish", "relu", "relu6", "selu", "sigmoid", "silu", "softmax", "soft_shrink", "softplus", "softsign", "sparse_plus", "sparsemax", "squareplus", "tanh", "tanh_shrink" ]
# "glu",, "threshold"

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testlist = [ config.TENSORFLOWDNN, config.TENSORFLOWLIC, config.TENSORFLOWMLP, config.TENSORFLOWRNN, config.TENSORFLOWCNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU, config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWRNN ]
        testlist = [ config.TENSORFLOWLSTM ]
        testlist = [ config.TENSORFLOWLSTM, config.TENSORFLOWGRU ]
        testlist = [ config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWDNN ]
        testlist = [ config.TENSORFLOWMLP ]
        for test in testlist:
          dslist = [ 'mnist', 'cifar10' ]
          dslist = [ 'mnist' ]
          for ds in dslist:
            result = cli.learn(ds = ds, cf = test, take = take, steps = steps)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
            # toto classify
        # here

    def test_comb(self):
        steps = 1
        testlist = [ config.TENSORFLOWDNN, config.TENSORFLOWLIC, config.TENSORFLOWMLP, config.TENSORFLOWRNN, config.TENSORFLOWCNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU, config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWRNN ]
        testlist = [ config.TENSORFLOWLSTM ]
        testlist = [ config.TENSORFLOWLSTM, config.TENSORFLOWGRU ]
        testlist = [ config.TENSORFLOWCNN2 ]
        testlist = [ config.TENSORFLOWDNN ]
        testlist = [ config.TENSORFLOWMLP ]
        for test in testlist:
          dslist = [ 'mnist', 'cifar10' ]
          dslist = [ 'mnist' ]
          for ds in dslist:
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
            # toto classify
        # here

if __name__ == '__main__':
    unittest.main()
