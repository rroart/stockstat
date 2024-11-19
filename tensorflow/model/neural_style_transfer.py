import os

os.environ["KERAS_BACKEND"] = "tensorflow"

import keras
import numpy as np
import tensorflow as tf
from keras.applications import vgg19

result_prefix = "/tmp/download/generated"

total_variation_weight = 1e-6
style_weight = 1e-6
content_weight = 2.5e-8

img_nrows = 400

class Model:
    def __init__(self, myobj, config, base_image_path, style_reference_image_path):
        self.config = config

        self.base_image_path = base_image_path
        self.style_reference_image_path = style_reference_image_path
        width, height = keras.utils.load_img(self.base_image_path).size
        self.img_ncols = int(width * img_nrows / height)
        # TODO DANGER full trust
        model = vgg19.VGG19(weights="imagenet", include_top=False)

        outputs_dict = dict([(layer.name, layer.output) for layer in model.layers])

        self.feature_extractor = keras.Model(inputs=model.inputs, outputs=outputs_dict)

    def preprocess_image(self, image_path):
        img = keras.utils.load_img(image_path, target_size=(img_nrows, self.img_ncols))
        img = keras.utils.img_to_array(img)
        img = np.expand_dims(img, axis=0)
        img = vgg19.preprocess_input(img)
        return tf.convert_to_tensor(img)

    def deprocess_image(self, x):
        x = x.reshape((img_nrows, self.img_ncols, 3))
        x[:, :, 0] += 103.939
        x[:, :, 1] += 116.779
        x[:, :, 2] += 123.68
        # 'BGR'->'RGB'
        x = x[:, :, ::-1]
        x = np.clip(x, 0, 255).astype("uint8")
        return x

    def gram_matrix(self, x):
        x = tf.transpose(x, (2, 0, 1))
        features = tf.reshape(x, (tf.shape(x)[0], -1))
        gram = tf.matmul(features, tf.transpose(features))
        return gram

    def style_loss(self, style, combination):
        S = self.gram_matrix(style)
        C = self.gram_matrix(combination)
        channels = 3
        size = img_nrows * self.img_ncols
        return tf.reduce_sum(tf.square(S - C)) / (4.0 * (channels ** 2) * (size ** 2))

    def content_loss(self, base, combination):
        return tf.reduce_sum(tf.square(combination - base))

    def total_variation_loss(self, x):
        a = tf.square(
            x[:, : img_nrows - 1, : self.img_ncols - 1, :] - x[:, 1:, : self.img_ncols - 1, :]
        )
        b = tf.square(
            x[:, : img_nrows - 1, : self.img_ncols - 1, :] - x[:, : img_nrows - 1, 1:, :]
        )
        return tf.reduce_sum(tf.pow(a + b, 1.25))

    def compile(self):
        print("DANGER full trust")

    def compute_loss(self, combination_image, base_image, style_reference_image):
        style_layer_names = [
            "block1_conv1",
            "block2_conv1",
            "block3_conv1",
            "block4_conv1",
            "block5_conv1",
        ]
        content_layer_name = "block5_conv2"

        input_tensor = tf.concat(
            [base_image, style_reference_image, combination_image], axis=0
        )
        features = self.feature_extractor(input_tensor)

        loss = tf.zeros(shape=())

        layer_features = features[content_layer_name]
        base_image_features = layer_features[0, :, :, :]
        combination_features = layer_features[2, :, :, :]
        loss = loss + content_weight * self.content_loss(
            base_image_features, combination_features
        )
        for layer_name in style_layer_names:
            layer_features = features[layer_name]
            style_reference_features = layer_features[1, :, :, :]
            combination_features = layer_features[2, :, :, :]
            sl = self.style_loss(style_reference_features, combination_features)
            loss += (style_weight / len(style_layer_names)) * sl

        loss += total_variation_weight * self.total_variation_loss(combination_image)
        return loss

    @tf.function
    def compute_loss_and_grads(self, combination_image, base_image, style_reference_image):
        with tf.GradientTape() as tape:
            loss = self.compute_loss(combination_image, base_image, style_reference_image)
        grads = tape.gradient(loss, combination_image)
        return loss, grads

    def train(self):
        optimizer = keras.optimizers.SGD(
            keras.optimizers.schedules.ExponentialDecay(
                initial_learning_rate=100.0, decay_steps=100, decay_rate=0.96
            )
        )

        base_image = self.preprocess_image(self.base_image_path)
        style_reference_image = self.preprocess_image(self.style_reference_image_path)
        combination_image = tf.Variable(self.preprocess_image(self.base_image_path))

        imgs = []
        iterations = self.config.steps
        for i in range(1, iterations + 1):
            loss, grads = self.compute_loss_and_grads(
                combination_image, base_image, style_reference_image
            )
            optimizer.apply_gradients([(grads, combination_image)])
            #if i % 100 == 0:
            if False:
                print("Iteration %d: loss=%.2f" % (i, loss))
                img = self.deprocess_image(combination_image.numpy())
                fname = result_prefix + "_at_iteration_%d.png" % i
                keras.utils.save_img(fname, img)
                imgs.append(fname)
        img = self.deprocess_image(combination_image.numpy())
        fname = result_prefix + ".png"
        keras.utils.save_img(fname, img)
        imgs.append("generated.png")
        return imgs

    def generate(self):
        os.makedirs("/tmp/download", 0o777, True)
        return self.train()

    def localsave(self):
       return False
