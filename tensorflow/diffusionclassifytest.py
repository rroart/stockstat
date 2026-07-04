import unittest
import sys

import config
import diffusioncli as cli

class MyTestCase(unittest.TestCase):
    argdslist = None
    argsteps = None
    def test_something(self):
        ads = 'cifar10'
        testlist = [ config.TENSORFLOWDIFFUSION ]
        result = cli.learn(ds = ads, take = 10)
        print(result)
        self.assertIsNotNone(result['loss'], "Loss")  # add assertion
        print(result['files'])
        result = cli.classify("/tmp/download/" + result['files'][0], ads)
        print(result)
        self.assertIsNotNone(result['classifycatarray'], "Cat")  # add assertion

if __name__ == '__main__':
    if len(sys.argv) > 1:
        print("argv", sys.argv)
        MyTestCase.argsteps = int(sys.argv.pop())
        print("args", MyTestCase.argdslist, MyTestCase.argsteps)
        MyTestCase.argdslist = [sys.argv.pop()]
        print("args", MyTestCase.argdslist, MyTestCase.argsteps)
    unittest.main()
