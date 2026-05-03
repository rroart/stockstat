import unittest

import config
import gptcli as cli
import time

class MyTestCase(unittest.TestCase):
    def test(self):
        testlist = [ config.TENSORFLOWMINIATUREGPT, config.TENSORFLOWGPT, config.TENSORFLOWGPT2 ]
        loss = {}
        times = {}
        for test in testlist:
          try:
            curtime = time.time()
            ds = ""
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
            loss[(test,ds)] = (result['loss'])
            times[(test,ds)] = time.time() - curtime
          except Exception:
            import traceback
            traceback.print_exc()
            pass
        print("loss", loss)
        print("times", times)

if __name__ == '__main__':
    unittest.main()
