import unittest

import config
import midicli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        testlist = [ config.PYTORCHGPTMIDIRPR, config.PYTORCHGPTMIDIRPR, config.PYTORCHGPTMIDIFIGARO ]
        testlist = [ config.PYTORCHGPTMIDIFIGARO ]
        testlist = [ config.PYTORCHGPTMIDIMMT ]
        for test in testlist:
            #result = cli.learn(ds = 'maestro', cf = test, take = 40, steps = 1)
            result = cli.learn(ds = 'sod', cf = test, take = 40, steps = 1)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        # here

if __name__ == '__main__':
    unittest.main()
