#tf220gpt

import unittest
import sys

import config
import gptcli as cli

steps = 10

class MyTestCase(unittest.TestCase):
    argdslist = None
    argsteps = None
    def test_something(self):
        testlist = [ config.TENSORFLOWMINIATUREGPT, config.TENSORFLOWGPT, config.TENSORFLOWGPT2 ]
        if MyTestCase.argsteps is not None:
            steps = MyTestCase.argsteps
        for test in testlist:
            if not test == config.TENSORFLOWGPT2:
                ds = 'imdb'
            else:
                ds = [ 'gpt2_base_en', 'imdb' ]
            result = cli.learn(ds = ds, cf = test, take = 40, steps = steps)
            print(result)
            self.assertIsNotNone(result['loss'], "Loss")  # add assertion
            result = cli.chat(text = "I like travelling", ds = ds, cf = test, take = 40)
            print(result)
            self.assertIsNotNone(result['classifyarray'][0], "Text")  # add assertion
        # here

if __name__ == '__main__':
    if len(sys.argv) > 1:
        print("argv", sys.argv)
        MyTestCase.argsteps = int(sys.argv.pop())
        print("args", MyTestCase.argdslist, MyTestCase.argsteps)
        MyTestCase.argdslist = [sys.argv.pop()]
        print("args", MyTestCase.argdslist, MyTestCase.argsteps)
    unittest.main()
