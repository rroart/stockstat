import unittest

import config
import gptcli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testlist = [ config.TENSORFLOWMINIATUREGPT, config.TENSORFLOWGPT, config.TENSORFLOWGPT2 ]
        for test in testlist:
            if not test == config.TENSORFLOWGPT2:
                ds = 'imdb'
            else:
                ds = [ 'gpt2_base_en', 'imdb' ]
            result = cli.learn(ds = ds, cf = test, take = 40, steps = 1)
            print(result)
            self.assertIsNotNone(result['loss'], "Loss")  # add assertion
            result = cli.chat(text = "I like travelling", ds = ds, cf = test, take = 40)
            print(result)
            self.assertIsNotNone(result['classifyarray'][0], "Text")  # add assertion
        # here

if __name__ == '__main__':
    unittest.main()
