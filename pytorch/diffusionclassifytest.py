import unittest
import sys

import config
import diffusioncli as cli

steps = 10

class MyTestCase(unittest.TestCase):
    argtestlist = None
    argsteps = None
    argdir = None
    def test_something(self):
        testlist = [ config.PYTORCHDIFFUSION ]
        if MyTestCase.argtestlist is not None:
            testlist = MyTestCase.argtestlist

        if MyTestCase.argsteps is not None:
            steps = MyTestCase.argsteps

        for test in testlist:
            #result = cli.learn(ds = 'maestro', cf = test, take = 40, steps = 1)
            ds = 'cifar10'
            submodel = None
            test = config.PYTORCHDIFFUSION
            result = cli.learn(ds = ds, path = MyTestCase.argdir, cf = test, take = 40, steps = steps, submodel = submodel)
            print(result)
            #return
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion

            result = cli.generate(text ="I like travelling", ds = ds, path = MyTestCase.argdir, cf = test, take = 40, submodel = submodel, size = 256)
            print(result)
            #self.assertIsNotNone(result['classifyarray'][0], "Text")  # add assertion
        # here


if __name__ == '__main__':
    if len(sys.argv) > 1:
        print("argv", sys.argv)
        MyTestCase.argdir = sys.argv.pop()
        MyTestCase.argsteps = int(sys.argv.pop())
        print("args", MyTestCase.argtestlist, MyTestCase.argsteps)
        MyTestCase.argtestlist = [sys.argv.pop()]
    unittest.main()
