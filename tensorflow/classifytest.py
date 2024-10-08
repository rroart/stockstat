import unittest

import tensorflow as tf

class MyTestCase(unittest.TestCase):
    def test_something(self):
        self.assertEqual(True, True)  # add assertion here

    def test_slide(self):
        data = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 ]
        size = 3
        input = data[:-size]
        target = data[size:]
        print(input)
        print(target)
        stride = 1
        ds = tf.keras.utils.timeseries_dataset_from_array(input, target, size, sequence_stride = stride)
        print(ds)
        for element in ds:
            print(element)

if __name__ == '__main__':
    unittest.main()
