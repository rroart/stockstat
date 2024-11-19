import unittest
import keras

import config
import imgcli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        ads = 'mnist'
        result = cli.conditionalgan(ds = ads, take = 10)
        print(result)
        self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        #TODO classify

    def test_something2(self):
        result = cli.dcgan(take = 10)
        print(result)
        self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion

    def test_something3(self):
        #base_image_path = keras.utils.get_file("paris.jpg", "https://i.imgur.com/F28w3Ac.jpg")
        #style_reference_image_path = keras.utils.get_file("starry_night.jpg", "https://i.imgur.com/9ooB60I.jpg")
        base_image_path = "paris.jpg"
        style_reference_image_path = "starry_night.jpg"
        result = cli.neural_style_transfer(path = base_image_path, path2 = style_reference_image_path, ds = 'vgg19', cf = config.TENSORFLOWNEURALSTYLETRANSFER)
        print(result)
        self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion

if __name__ == '__main__':
    unittest.main()
