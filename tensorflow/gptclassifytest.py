import unittest

import config
import gptcli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testlist = [ config.TENSORFLOWMINIATUREGPT, config.TENSORFLOWGPT, config.TENSORFLOWGPT2 ]
        for test in testlist:
            result = cli.learn(ds = 'imdb', cf = test, take = 40, steps = 1)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
            result = cli.chat(text = "I like travelling", ds = 'imdb', cf = test, take = 40)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        # here

if __name__ == '__main__':
    unittest.main()
