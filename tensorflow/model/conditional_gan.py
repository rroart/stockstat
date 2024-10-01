import keras

from keras import layers
from keras import ops
import tensorflow as tf
import numpy as np

"""
## Constants and hyperparameters
"""

batch_size = 64
num_channels = 1
num_classes = 10
latent_dim = 128


"""
## Calculating the number of input channel for the generator and discriminator

In a regular (unconditional) GAN, we start by sampling noise (of some fixed
dimension) from a normal distribution. In our case, we also need to account
for the class labels. We will have to add the number of classes to
the input channels of the generator (noise input) as well as the discriminator
(generated image input).
"""

generator_in_channels = latent_dim + num_classes
discriminator_in_channels = num_channels + num_classes
print(generator_in_channels, discriminator_in_channels)

"""
## Creating the discriminator and generator

The model definitions (`discriminator`, `generator`, and `ConditionalGAN`) have been
adapted from [this example](https://keras.io/guides/customizing_what_happens_in_fit/).
"""

# Create the discriminator.
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

# Create the generator.
def generator():
    return keras.Sequential(
        [
            keras.layers.InputLayer((generator_in_channels,)),
            # We want to generate 128 + num_classes coefficients to reshape into a
            # 7x7x(128 + num_classes) map.
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

"""
## Creating a `ConditionalGAN` model
"""


class Model(tf.keras.Model):
    def __init__(self, myobj, config, classify):
        super().__init__()
        #super(Model, self).__init__(config, classify, name='my_model')
        self.myobj = myobj
        self.config = config
        self.classify = classify

        self.discriminator = discriminator(myobj.size[0], myobj.size[1])
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
        # Unpack the data.
        real_images, one_hot_labels = data

        # Add dummy dimensions to the labels so that they can be concatenated with
        # the images. This is for the discriminator.
        image_one_hot_labels = one_hot_labels[:, :, None, None]
        image_one_hot_labels = ops.repeat(
            image_one_hot_labels, repeats=[self.myobj.size[0] * self.myobj.size[1]]
        )
        image_one_hot_labels = ops.reshape(
            image_one_hot_labels, (-1, self.myobj.size[0] * self.myobj.size[1], num_classes)
        )

        # Sample random points in the latent space and concatenate the labels.
        # This is for the generator.
        batch_size = ops.shape(real_images)[0]
        random_latent_vectors = keras.random.normal(
            shape=(batch_size, latent_dim), seed=self.seed_generator
        )
        random_vector_labels = ops.concatenate(
            [random_latent_vectors, one_hot_labels], axis=1
        )

        # Decode the noise (guided by labels) to fake images.
        generated_images = self.generator(random_vector_labels)

        # Combine them with real images. Note that we are concatenating the labels
        # with these images here.
        fake_image_and_labels = ops.concatenate(
            [generated_images, image_one_hot_labels], -1
        )
        real_image_and_labels = ops.concatenate([real_images, image_one_hot_labels], -1)
        combined_images = ops.concatenate(
            [fake_image_and_labels, real_image_and_labels], axis=0
        )

        # common
        # Assemble labels discriminating real from fake images.
        labels = ops.concatenate(
            [ops.ones((batch_size, 1)), ops.zeros((batch_size, 1))], axis=0
        )

        # common
        # Train the discriminator.
        with tf.GradientTape() as tape:
            predictions = self.discriminator(combined_images)
            d_loss = self.loss_fn(labels, predictions)
        grads = tape.gradient(d_loss, self.discriminator.trainable_weights)
        self.d_optimizer.apply_gradients(
            zip(grads, self.discriminator.trainable_weights)
        )

        # Sample random points in the latent space.
        random_latent_vectors = keras.random.normal(
            shape=(batch_size, latent_dim), seed=self.seed_generator
        )
        random_vector_labels = ops.concatenate(
            [random_latent_vectors, one_hot_labels], axis=1
        )

        # common
        # Assemble labels that say "all real images".
        misleading_labels = ops.zeros((batch_size, 1))

        # common
        # Train the generator (note that we should *not* update the weights
        # of the discriminator)!
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
        # Monitor loss.
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
        fake = self.generator.predict(noise_and_labels)
        return fake

    def generate(self):
        """
        ## Interpolating between classes with the trained generator
        """

        # mnist
        # We first extract the trained generator from our Conditional GAN.
        trained_gen = self.generator

        # Choose the number of intermediate images that would be generated in
        # between the interpolation + 2 (start and last images).
        num_interpolation = 9  # @param {type:"integer"}

        # Sample noise for the interpolation.
        interpolation_noise = tf.keras.random.normal(shape=(1, latent_dim))
        interpolation_noise = tf.keras.ops.repeat(interpolation_noise, repeats=num_interpolation)
        interpolation_noise = tf.keras.ops.reshape(interpolation_noise, (num_interpolation, latent_dim))

        start_class = 2  # @param {type:"slider", min:0, max:9, step:1}
        end_class = 6  # @param {type:"slider", min:0, max:9, step:1}

        fake_images = self.interpolate_class(start_class, end_class, interpolation_noise, num_interpolation)

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

    def localsave(self):
        return True

    def save(self, filename):
        self.save(filename)
