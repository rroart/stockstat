import tensorflow as tf
import keras
#from tensorflow.keras import layers
from keras.layers import Dense, Activation, Dropout #, regularizers
#from keras.models import Sequential
from tensorflow.keras.optimizers import Adam, RMSprop

import numpy as np

from tqdm.auto import trange, tqdm
import matplotlib.pyplot as plt

import tensorflow as tf
from tensorflow.keras import layers

import diffusionutils

from . import layerutils
#from .mymodelseq import MyModelSeq
from .model import MyModel

# from https://tree.rocks/make-diffusion-model-from-scratch-easy-way-to-implement-quick-diffusion-model-e60d18fd0f2e

class Model(MyModel):

  def __init__(self, myobj, config, classify, shape):
    super(Model, self).__init__(config, classify, name='my_model')

    #def make_model():
    x = x_input = layers.Input(shape=(shape[1], shape[2], 3), name='x_input')

    x_ts = x_ts_input = layers.Input(shape=(1,), name='x_ts_input')
    x_ts = layers.Dense(192)(x_ts)
    x_ts = layers.LayerNormalization()(x_ts)
    x_ts = layers.Activation('relu')(x_ts)

    # ----- left ( down ) -----
    x = x32 = block(x, x_ts)
    x = layers.MaxPool2D(2)(x)

    x = x16 = block(x, x_ts)
    x = layers.MaxPool2D(2)(x)

    x = x8 = block(x, x_ts)
    x = layers.MaxPool2D(2)(x)

    x = x4 = block(x, x_ts)

    # ----- MLP -----
    x = layers.Flatten()(x)
    x = layers.Concatenate()([x, x_ts])
    x = layers.Dense(128)(x)
    x = layers.LayerNormalization()(x)
    x = layers.Activation('relu')(x)

    x = layers.Dense(4 * 4 * 32)(x)
    x = layers.LayerNormalization()(x)
    x = layers.Activation('relu')(x)
    x = layers.Reshape((4, 4, 32))(x)

    # ----- right ( up ) -----
    x = layers.Concatenate()([x, x4])
    x = block(x, x_ts)
    x = layers.UpSampling2D(2)(x)

    x = layers.Concatenate()([x, x8])
    x = block(x, x_ts)
    x = layers.UpSampling2D(2)(x)

    x = layers.Concatenate()([x, x16])
    x = block(x, x_ts)
    x = layers.UpSampling2D(2)(x)

    x = layers.Concatenate()([x, x32])
    x = block(x, x_ts)

    # ----- output -----
    x = layers.Conv2D(3, kernel_size=1, padding='same')(x)
    model = tf.keras.models.Model([x_input, x_ts_input], x)

    optimizer = tf.keras.optimizers.Adam(learning_rate=0.0008)
    loss_func = tf.keras.losses.MeanAbsoluteError()
    model.compile(loss=loss_func, optimizer=optimizer)
    self.model = model
    self.optimizer = optimizer


  def train_one(self, x_img):
    x_ts = diffusionutils.generate_ts(len(x_img))
    x_a, x_b = diffusionutils.forward_noise(x_img, x_ts)
    loss = self.model.train_on_batch([x_a, x_ts], x_b)
    return loss


  def train(self, X_train, R=50):
    bar = trange(R)
    total = 100
    for i in bar:
        for j in range(total):
            x_img = X_train[np.random.randint(len(X_train), size=diffusionutils.BATCH_SIZE)]
            loss = self.train_one(x_img)
            pg = (j / total) * 100
            if j % 5 == 0:
                bar.set_description(f'loss: {loss:.5f}, p: {pg:.2f}%')


  def predict(self, x_idx=None):
    x = np.random.normal(size=(32, diffusionutils.IMG_SIZE, diffusionutils.IMG_SIZE, 3))
    for i in trange(diffusionutils.timesteps):
        t = i
        x = self.model.predict([x, np.full((32), t)], verbose=0)
    diffusionutils.show_examples(x)


  def predict_step(self):
    xs = []
    x = np.random.normal(size=(8, diffusionutils.IMG_SIZE, diffusionutils.IMG_SIZE, 3))

    for i in trange(diffusionutils.timesteps):
        t = i
        x = self.model.predict([x, np.full((8),  t)], verbose=0)
        if i % 2 == 0:
            xs.append(x[0])

    plt.figure(figsize=(20, 2))
    for i in range(len(xs)):
        plt.subplot(1, len(xs), i+1)
        plt.imshow(diffusionutils.cvtImg(xs[i]))
        plt.title(f'{i}')
        plt.axis('off')

def block(x_img, x_ts):
    x_parameter = layers.Conv2D(128, kernel_size=3, padding='same')(x_img)
    x_parameter = layers.Activation('relu')(x_parameter)

    time_parameter = layers.Dense(128)(x_ts)
    time_parameter = layers.Activation('relu')(time_parameter)
    time_parameter = layers.Reshape((1, 1, 128))(time_parameter)
    x_parameter = x_parameter * time_parameter

    # -----
    x_out = layers.Conv2D(128, kernel_size=3, padding='same')(x_img)
    x_out = x_out + x_parameter
    x_out = layers.LayerNormalization()(x_out)
    x_out = layers.Activation('relu')(x_out)

    return x_out


