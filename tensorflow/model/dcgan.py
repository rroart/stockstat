import keras
import tensorflow as tf

from keras import layers
from keras import ops
import os

def discriminator(x, y):
    discriminator = keras.Sequential(
        [
            keras.Input(shape=(x, y, 3)),
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
    return discriminator

latent_dim = 128

def generator():
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
    return generator

class Model(keras.Model):
    def __init__(self, myobj, config, shape):
        super().__init__()
        self.myobj = myobj
        self.config = config

        self.discriminator = discriminator(shape[1], shape[2])
        self.generator = generator()
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
            shape=(batch_size, latent_dim), seed=self.seed_generator
        )

        generated_images = self.generator(random_latent_vectors)

        combined_images = ops.concatenate([generated_images, real_images], axis=0)

        # common
        labels = ops.concatenate(
            [ops.ones((batch_size, 1)), ops.zeros((batch_size, 1))], axis=0
        )

        # different
        labels += 0.05 * tf.random.uniform(tf.shape(labels))

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

        # common
        misleading_labels = ops.zeros((batch_size, 1))

        # common
        with tf.GradientTape() as tape:
            predictions = self.discriminator(self.generator(random_latent_vectors))
            g_loss = self.loss_fn(misleading_labels, predictions)
        grads = tape.gradient(g_loss, self.generator.trainable_weights)
        self.g_optimizer.apply_gradients(zip(grads, self.generator.trainable_weights))

        # common
        self.d_loss_metric.update_state(d_loss)
        self.g_loss_metric.update_state(g_loss)
        return {
            "d_loss": self.d_loss_metric.result(),
            "g_loss": self.g_loss_metric.result(),
        }

    def generate(self):
        os.makedirs("/tmp/download", 0o777, True)
        seed_generator = keras.random.SeedGenerator(42)
        random_latent_vectors = keras.random.normal(
            shape=(self.myobj.files, latent_dim), seed=seed_generator
        )
        print("ra", random_latent_vectors)
        generated_images = self.generator(random_latent_vectors)
        generated_images *= 255
        generated_images.numpy()
        print("gi", generated_images.shape)
        imgs = []
        for i in range(self.myobj.files):
            img = keras.utils.array_to_img(generated_images[i])
            img.save("/tmp/download/generated_img_%d.png" % (i))
            imgs.append("generated_img_" + str(i) + ".png")
        print("Done")
        return imgs

    def localsave(self):
       return True

    #def save(self, filename):
    #    self.save(filename)

    def getcallback(self):
        return GANMonitor(num_img=self.myobj.files)

class GANMonitor(keras.callbacks.Callback):
    def __init__(self, num_img=3):
        self.num_img = num_img
        self.seed_generator = keras.random.SeedGenerator(42)

    def on_epoch_end(self, epoch, logs=None):
        return

class GANMonitor2(keras.callbacks.Callback):
    def __init__(self, num_img=3):
        self.num_img = num_img
        self.seed_generator = keras.random.SeedGenerator(42)

    def on_epoch_end(self, epoch, logs=None):
        random_latent_vectors = keras.random.normal(
            shape=(self.num_img, latent_dim), seed=self.seed_generator
        )
        print("ra", random_latent_vectors)
        generated_images = self.model. generator(random_latent_vectors)
        generated_images *= 255
        generated_images.numpy()
        for i in range(self.num_img):
            img = keras.utils.array_to_img(generated_images[i])
            img.save("generated_img_%03d_%d.png" % (epoch, i))

