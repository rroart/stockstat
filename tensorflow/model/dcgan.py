"""
Title: DCGAN to generate face images
Author: [fchollet](https://twitter.com/fchollet)
Date created: 2019/04/29
Last modified: 2023/12/21
Description: A simple DCGAN trained using `fit()` by overriding `train_step` on CelebA images.
Accelerator: GPU
"""

"""
## Setup
"""

import keras
import tensorflow as tf

from keras import layers
from keras import ops
#import matplotlib.pyplot as plt
import os
#import gdown
#from zipfile import ZipFile


"""
## Prepare CelebA data

We'll use face images from the CelebA dataset, resized to 64x64.
"""

#os.makedirs("celeba_gan")

#url = "https://drive.google.com/uc?id=1O7m1010EJjLE5QxLZiM9Fpjs7Oj6e684"
#output = "celeba_gan/data.zip"
#gdown.download(url, output, quiet=True)

#with ZipFile("celeba_gan/data.zip", "r") as zipobj:
#    zipobj.extractall("celeba_gan")

"""
Create a dataset from our folder, and rescale the images to the [0-1] range:
"""

#dataset = keras.utils.image_dataset_from_directory(
#    "celeba_gan", label_mode=None, image_size=(64, 64), batch_size=32
#)
#dataset = dataset.map(lambda x: x / 255.0)


"""
Let's display a sample image:
"""


#for x in dataset:
#    plt.axis("off")
#    plt.imshow((x.numpy() * 255).astype("int32")[0])
#    break


"""
## Create the discriminator

It maps a 64x64 image to a binary classification score.
"""

discriminator = keras.Sequential(
    [
        keras.Input(shape=(64, 64, 3)),
        layers.Conv2D(64, kernel_size=4, strides=2, padding="same"),
        layers.LeakyReLU(negative_slope=0.2),
        layers.Conv2D(128, kernel_size=4, strides=2, padding="same"),
        layers.LeakyReLU(negative_slope=0.2),
        layers.Conv2D(128, kernel_size=4, strides=2, padding="same"),
        layers.LeakyReLU(negative_slope=0.2),
        layers.Flatten(),
        layers.Dropout(0.2),
        layers.Dense(1, activation="sigmoid"),
    ],
    name="discriminator",
)
discriminator.summary()

"""
## Create the generator

It mirrors the discriminator, replacing `Conv2D` layers with `Conv2DTranspose` layers.
"""

latent_dim = 128

generator = keras.Sequential(
    [
        keras.Input(shape=(latent_dim,)),
        layers.Dense(8 * 8 * 128),
        layers.Reshape((8, 8, 128)),
        layers.Conv2DTranspose(128, kernel_size=4, strides=2, padding="same"),
        layers.LeakyReLU(negative_slope=0.2),
        layers.Conv2DTranspose(256, kernel_size=4, strides=2, padding="same"),
        layers.LeakyReLU(negative_slope=0.2),
        layers.Conv2DTranspose(512, kernel_size=4, strides=2, padding="same"),
        layers.LeakyReLU(negative_slope=0.2),
        layers.Conv2D(3, kernel_size=5, padding="same", activation="sigmoid"),
    ],
    name="generator",
)
generator.summary()

"""
## Override `train_step`
"""


class Model(keras.Model):
    def __init__(self, config, classify, discriminator, generator, latent_dim):
        super().__init__()
        self.config = config
        self.classify = classify

        self.discriminator = discriminator
        self.generator = generator
        self.latent_dim = latent_dim
        self.seed_generator = keras.random.SeedGenerator(1337)

    def compile(self, d_optimizer, g_optimizer, loss_fn):
        super().compile()
        self.d_optimizer = d_optimizer
        self.g_optimizer = g_optimizer
        self.loss_fn = loss_fn
        self.d_loss_metric = keras.metrics.Mean(name="d_loss")
        self.g_loss_metric = keras.metrics.Mean(name="g_loss")

    @property
    def metrics(self):
        return [self.d_loss_metric, self.g_loss_metric]

    def train_step(self, real_images):
        # Sample random points in the latent space
        batch_size = ops.shape(real_images)[0]
        random_latent_vectors = keras.random.normal(
            shape=(batch_size, self.latent_dim), seed=self.seed_generator
        )

        # Decode them to fake images
        generated_images = self.generator(random_latent_vectors)

        # Combine them with real images
        combined_images = ops.concatenate([generated_images, real_images], axis=0)

        # common
        # Assemble labels discriminating real from fake images
        labels = ops.concatenate(
            [ops.ones((batch_size, 1)), ops.zeros((batch_size, 1))], axis=0
        )

        # different
        # Add random noise to the labels - important trick!
        labels += 0.05 * tf.random.uniform(tf.shape(labels))

        # common
        # Train the discriminator
        with tf.GradientTape() as tape:
            predictions = self.discriminator(combined_images)
            d_loss = self.loss_fn(labels, predictions)
        grads = tape.gradient(d_loss, self.discriminator.trainable_weights)
        self.d_optimizer.apply_gradients(
            zip(grads, self.discriminator.trainable_weights)
        )

        # Sample random points in the latent space
        random_latent_vectors = keras.random.normal(
            shape=(batch_size, self.latent_dim), seed=self.seed_generator
        )

        # common
        # Assemble labels that say "all real images"
        misleading_labels = ops.zeros((batch_size, 1))

        # common
        # Train the generator (note that we should *not* update the weights
        # of the discriminator)!
        with tf.GradientTape() as tape:
            predictions = self.discriminator(self.generator(random_latent_vectors))
            g_loss = self.loss_fn(misleading_labels, predictions)
        grads = tape.gradient(g_loss, self.generator.trainable_weights)
        self.g_optimizer.apply_gradients(zip(grads, self.generator.trainable_weights))

        # common
        # Update metrics
        self.d_loss_metric.update_state(d_loss)
        self.g_loss_metric.update_state(g_loss)
        return {
            "d_loss": self.d_loss_metric.result(),
            "g_loss": self.g_loss_metric.result(),
        }


"""
## Create a callback that periodically saves generated images
"""


class GANMonitor(keras.callbacks.Callback):
    def __init__(self, num_img=3, latent_dim=128):
        self.num_img = num_img
        self.latent_dim = latent_dim
        self.seed_generator = keras.random.SeedGenerator(42)

    def on_epoch_end(self, epoch, logs=None):
        random_latent_vectors = keras.random.normal(
            shape=(self.num_img, self.latent_dim), seed=self.seed_generator
        )
        generated_images = self.model.generator(random_latent_vectors)
        generated_images *= 255
        generated_images.numpy()
        for i in range(self.num_img):
            img = keras.utils.array_to_img(generated_images[i])
            img.save("generated_img_%03d_%d.png" % (epoch, i))


"""
## Train the end-to-end model
"""

#epochs = 1  # In practice, use ~100 epochs

#gan = Model(discriminator=discriminator, generator=generator, latent_dim=latent_dim)
#gan.compile(
#    d_optimizer=keras.optimizers.Adam(learning_rate=0.0001),
#    g_optimizer=keras.optimizers.Adam(learning_rate=0.0001),
#    loss_fn=keras.losses.BinaryCrossentropy(),
#)

#gan.fit(
#    dataset, epochs=epochs, callbacks=[GANMonitor(num_img=10, latent_dim=latent_dim)]
#)

"""
Some of the last generated images around epoch 30
(results keep improving after that):

![results](https://i.imgur.com/h5MtQZ7l.png)
"""

gen:
    """
    ## Interpolating between classes with the trained generator
    """

    # mnist
    # We first extract the trained generator from our Conditional GAN.
    trained_gen = gan.generator

    # Choose the number of intermediate images that would be generated in
    # between the interpolation + 2 (start and last images).
    num_interpolation = 9  # @param {type:"integer"}

    # Sample noise for the interpolation.
    interpolation_noise = tf.keras.random.normal(shape=(1, gan.latent_dim))
    interpolation_noise = tf.keras.ops.repeat(interpolation_noise, repeats=num_interpolation)
    interpolation_noise = tf.keras.ops.reshape(interpolation_noise, (num_interpolation, gan.latent_dim))

    start_class = 2  # @param {type:"slider", min:0, max:9, step:1}
    end_class = 6  # @param {type:"slider", min:0, max:9, step:1}

    fake_images = gan.interpolate_class(start_class, end_class, interpolation_noise, num_interpolation, trained_gen)

    """
    Here, we first sample noise from a normal distribution and then we repeat that for
    `num_interpolation` times and reshape the result accordingly.
    We then distribute it uniformly for `num_interpolation`
    with the label identities being present in some proportion.
    """

    fake_images *= 255.0
    converted_images = fake_images.astype(np.uint8)
    converted_images = keras.ops.image.resize(converted_images, (96, 96)).numpy().astype(np.uint8)
    import imageio

    imageio.mimsave("animation.gif", converted_images[:, :, :, 0], fps=1)
