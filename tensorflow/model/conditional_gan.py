import keras

from keras import layers
from keras import ops
import tensorflow as tf
import numpy as np
import os

batch_size = 64
num_channels = 1
num_classes = 10
latent_dim = 128

generator_in_channels = latent_dim + num_classes
discriminator_in_channels = num_channels + num_classes
print(generator_in_channels, discriminator_in_channels)

def discriminator(x, y):
    return keras.Sequential(
        [
            keras.layers.InputLayer((x, y, discriminator_in_channels)),
            layers.Conv2D(64, (3, 3), strides=(2, 2), padding="same"),
            layers.LeakyReLU(negative_slope=0.2),
            layers.Conv2D(128, (3, 3), strides=(2, 2), padding="same"),
            layers.LeakyReLU(negative_slope=0.2),
            layers.GlobalMaxPooling2D(),
            layers.Dense(1),
        ],
        name="discriminator",
    )

def generator():
    return keras.Sequential(
        [
            keras.layers.InputLayer((generator_in_channels,)),
            layers.Dense(7 * 7 * generator_in_channels),
            layers.LeakyReLU(negative_slope=0.2),
            layers.Reshape((7, 7, generator_in_channels)),
            layers.Conv2DTranspose(128, (4, 4), strides=(2, 2), padding="same"),
            layers.LeakyReLU(negative_slope=0.2),
            layers.Conv2DTranspose(128, (4, 4), strides=(2, 2), padding="same"),
            layers.LeakyReLU(negative_slope=0.2),
            layers.Conv2D(1, (7, 7), padding="same", activation="sigmoid"),
        ],
        name="generator",
    )

class Model(tf.keras.Model):
    def __init__(self, myobj, config, shape):
        super().__init__()
        #super(Model, self).__init__(config, classify, name='my_model')
        self.myobj = myobj
        self.config = config
        self.shape = shape

        self.discriminator = discriminator(shape[1], shape[2])
        self.generator = generator()
        self.seed_generator = keras.random.SeedGenerator(1337)
        self.gen_loss_tracker = keras.metrics.Mean(name="generator_loss")
        self.disc_loss_tracker = keras.metrics.Mean(name="discriminator_loss")

    @property
    def metrics(self):
        return [self.gen_loss_tracker, self.disc_loss_tracker]

    def compile(self, d_optimizer, g_optimizer, loss_fn):
        super().compile()
        #self.model.compile()
        self.d_optimizer = d_optimizer
        self.g_optimizer = g_optimizer
        self.loss_fn = loss_fn

    def train_step(self, data):
        real_images, one_hot_labels = data

        image_one_hot_labels = one_hot_labels[:, :, None, None]
        image_one_hot_labels = ops.repeat(
            image_one_hot_labels, repeats=[self.shape[1] * self.shape[2]]
        )
        image_one_hot_labels = ops.reshape(
            image_one_hot_labels, (-1, self.shape[1], self.shape[2], num_classes)
        )

        batch_size = ops.shape(real_images)[0]
        random_latent_vectors = keras.random.normal(
            shape=(batch_size, latent_dim), seed=self.seed_generator
        )
        random_vector_labels = ops.concatenate(
            [random_latent_vectors, one_hot_labels], axis=1
        )

        generated_images = self.generator(random_vector_labels)

        fake_image_and_labels = ops.concatenate(
            [generated_images, image_one_hot_labels], -1
        )
        real_image_and_labels = ops.concatenate([real_images, image_one_hot_labels], -1)
        combined_images = ops.concatenate(
            [fake_image_and_labels, real_image_and_labels], axis=0
        )

        # common
        labels = ops.concatenate(
            [ops.ones((batch_size, 1)), ops.zeros((batch_size, 1))], axis=0
        )

        # common
        with tf.GradientTape() as tape:
            predictions = self.discriminator(combined_images)
            d_loss = self.loss_fn(labels, predictions)
        grads = tape.gradient(d_loss, self.discriminator.trainable_weights)
        self.d_optimizer.apply_gradients(
            zip(grads, self.discriminator.trainable_weights)
        )

        random_latent_vectors = keras.random.normal(
            shape=(batch_size, latent_dim), seed=self.seed_generator
        )
        random_vector_labels = ops.concatenate(
            [random_latent_vectors, one_hot_labels], axis=1
        )

        # common
        misleading_labels = ops.zeros((batch_size, 1))

        # common
        with tf.GradientTape() as tape:
            fake_images = self.generator(random_vector_labels)
            fake_image_and_labels = ops.concatenate(
                [fake_images, image_one_hot_labels], -1
            )
            predictions = self.discriminator(fake_image_and_labels)
            g_loss = self.loss_fn(misleading_labels, predictions)
        grads = tape.gradient(g_loss, self.generator.trainable_weights)
        self.g_optimizer.apply_gradients(zip(grads, self.generator.trainable_weights))

        # common
        self.gen_loss_tracker.update_state(g_loss)
        self.disc_loss_tracker.update_state(d_loss)
        return {
            "g_loss": self.gen_loss_tracker.result(),
            "d_loss": self.disc_loss_tracker.result(),
        }


    def interpolate_class(self, first_number, second_number, interpolation_noise, num_interpolation):
        # Convert the start and end labels to one-hot encoded vectors.
        first_label = keras.utils.to_categorical([first_number], num_classes)
        second_label = keras.utils.to_categorical([second_number], num_classes)
        first_label = ops.cast(first_label, "float32")
        second_label = ops.cast(second_label, "float32")

        # Calculate the interpolation vector between the two labels.
        percent_second_label = ops.linspace(0, 1, num_interpolation)[:, None]
        percent_second_label = ops.cast(percent_second_label, "float32")
        interpolation_labels = (
            first_label * (1 - percent_second_label) + second_label * percent_second_label
        )

        # Combine the noise and the labels and run inference with the generator.
        noise_and_labels = ops.concatenate([interpolation_noise, interpolation_labels], 1)
        print("nl", noise_and_labels)
        fake = self.generator.predict(noise_and_labels)
        return fake

    def generate(self):
        os.makedirs("/tmp/download", 0o777, True)

        trained_gen = self.generator

        num_interpolation = self.myobj.files # 9  # @param {type:"integer"}

        interpolation_noise = tf.keras.random.normal(shape=(1, latent_dim))
        interpolation_noise = tf.keras.ops.repeat(interpolation_noise, repeats=num_interpolation)
        interpolation_noise = tf.keras.ops.reshape(interpolation_noise, (num_interpolation, latent_dim))

        start_class = 2  # @param {type:"slider", min:0, max:9, step:1}
        end_class = 6  # @param {type:"slider", min:0, max:9, step:1}

        fake_images = self.interpolate_class(start_class, end_class, interpolation_noise, num_interpolation)

        fake_images *= 255.0
        converted_images = fake_images.astype(np.uint8)
        # TODO
        #converted_images = keras.ops.image.resize(converted_images, (96, 96)).numpy().astype(np.uint8)
        print("conv", converted_images.shape)

        imgs = []
        for i in range(self.myobj.files):
            img = keras.utils.array_to_img(converted_images[i])
            img.save("/tmp/download/generated_img_%d.png" % (i))
            imgs.append("generated_img_" + str(i) + ".png")
        print("Done")
        return imgs
        import imageio

        imageio.mimsave("animation.gif", converted_images[:, :, :, 0], fps=1)

    def localsave(self):
        return True

    #def save(self, filename):
    #    self.save(filename)

    def getcallback(self):
        return GANMonitor(num_img=10)

class GANMonitor(keras.callbacks.Callback):
    def __init__(self, num_img=3):
        self.num_img = num_img
        self.seed_generator = keras.random.SeedGenerator(42)

    def on_epoch_end(self, epoch, logs=None):
        return