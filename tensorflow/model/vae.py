import tensorflow as tf
from tensorflow.keras.layers import Dense, Activation, Dropout, SimpleRNN, RNN, Flatten, TimeDistributed
from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers import Adam, RMSprop
import numpy as np
import tensorflow as tf
import keras
from keras import ops
from keras import layers

from .model import MyModel

class Model(keras.Model):

  def __init__(self, myobj, config, classify, encoder, decoder):
    super(Model, self).__init__()
    self.encoder = encoder
    self.decoder = decoder
    self.total_loss_tracker = keras.metrics.Mean(name="total_loss")
    self.reconstruction_loss_tracker = keras.metrics.Mean(
      name="reconstruction_loss"
    )
    self.kl_loss_tracker = keras.metrics.Mean(name="kl_loss")

    #self.vae.compile(optimizer=keras.optimizers.Adam())


  #def fit(self, dataset):
  #  self.vae.fit(dataset, epochs=30, batch_size=128)


  @property
  def metrics(self):
      return [
        self.total_loss_tracker,
        self.reconstruction_loss_tracker,
        self.kl_loss_tracker,
      ]

  def train_step(self, data):
      with tf.GradientTape() as tape:
          z_mean, z_log_var, z = self.encoder(data)
          reconstruction = self.decoder(z)
          reconstruction_loss = ops.mean(
              ops.sum(
                  keras.losses.binary_crossentropy(data, reconstruction),
                  axis=(1, 2),
              )
          )
          kl_loss = -0.5 * (1 + z_log_var - ops.square(z_mean) - ops.exp(z_log_var))
          kl_loss = ops.mean(ops.sum(kl_loss, axis=1))
          total_loss = reconstruction_loss + kl_loss
      grads = tape.gradient(total_loss, self.trainable_weights)
      self.optimizer.apply_gradients(zip(grads, self.trainable_weights))
      self.total_loss_tracker.update_state(total_loss)
      self.reconstruction_loss_tracker.update_state(reconstruction_loss)
      self.kl_loss_tracker.update_state(kl_loss)
      return {
          "loss": self.total_loss_tracker.result(),
          "reconstruction_loss": self.reconstruction_loss_tracker.result(),
          "kl_loss": self.kl_loss_tracker.result(),
      }


  def generate(self):
    plot_latent_space(self)
    (x_train, y_train), _ = keras.datasets.mnist.load_data()
    x_train = np.expand_dims(x_train, -1).astype("float32") / 255

    plot_label_clusters(self, x_train, y_train)


import matplotlib.pyplot as plt

def plot_latent_space(vae, n=30, figsize=15):
    # display a n*n 2D manifold of digits
    digit_size = 28
    scale = 1.0
    figure = np.zeros((digit_size * n, digit_size * n))
    # linearly spaced coordinates corresponding to the 2D plot
    # of digit classes in the latent space
    grid_x = np.linspace(-scale, scale, n)
    grid_y = np.linspace(-scale, scale, n)[::-1]

    for i, yi in enumerate(grid_y):
        for j, xi in enumerate(grid_x):
            z_sample = np.array([[xi, yi]])
            x_decoded = vae.decoder.predict(z_sample, verbose=0)
            digit = x_decoded[0].reshape(digit_size, digit_size)
            figure[
                i * digit_size : (i + 1) * digit_size,
                j * digit_size : (j + 1) * digit_size,
            ] = digit

    plt.figure(figsize=(figsize, figsize))
    start_range = digit_size // 2
    end_range = n * digit_size + start_range
    pixel_range = np.arange(start_range, end_range, digit_size)
    sample_range_x = np.round(grid_x, 1)
    sample_range_y = np.round(grid_y, 1)
    plt.xticks(pixel_range, sample_range_x)
    plt.yticks(pixel_range, sample_range_y)
    plt.xlabel("z[0]")
    plt.ylabel("z[1]")
    plt.imshow(figure, cmap="Greys_r")
    plt.show()


#plot_latent_space(vae)

"""
## Display how the latent space clusters different digit classes
"""


def plot_label_clusters(vae, data, labels):
    # display a 2D plot of the digit classes in the latent space
    z_mean, _, _ = vae.encoder.predict(data, verbose=0)
    plt.figure(figsize=(12, 10))
    plt.scatter(z_mean[:, 0], z_mean[:, 1], c=labels)
    plt.colorbar()
    plt.xlabel("z[0]")
    plt.ylabel("z[1]")
    plt.show()


#(x_train, y_train), _ = keras.datasets.mnist.load_data()
#x_train = np.expand_dims(x_train, -1).astype("float32") / 255

#plot_label_clusters(vae, x_train, y_train)
