import math

import tensorflow as tf
from tensorflow.keras.layers import Dense, Activation, Dropout, Conv2D, MaxPooling2D, Flatten, Convolution2D, BatchNormalization, LeakyReLU, Dropout
from tensorflow.keras.models import Sequential
import model.layerutils as layerutils
from . import cnnutils

from .model import MyModel

dilation = 1

class Model(MyModel):

  def __init__(self, myobj, config, classify, shape):
    super(Model, self).__init__(config, classify, name='my_model')

    optimizer = layerutils.getOptimizer(config)
    regularizer = layerutils.getRegularizer(config)
    activation = tf.keras.layers.Activation(config.activation)
    lastactivation = tf.keras.layers.Activation(config.lastactivation)
    if config.binary:
        myobj.classes = 1

    # Define your layers here.

    model = Sequential()

    d11 = self.calc(config, shape[1])
    d21 = self.calc(config, shape[2])
    d31 = self.calc(config, shape[3])
    d1s = []
    d2s = []
    d3s = []
    if not d11 is None:
        d1s.append(d11)
    if not d21 is None:
        d2s.append(d21)
    if not d31 is None:
        d3s.append(d31)
    for i in range(config.convlayers - 1):
        if len(d1s) > 0:
            ad1 = self.calc(config, d1s[-1])
            if not ad1 is None:
                d1s.append(ad1)
        if len(d2s) > 0:
            ad2 = self.calc(config, d2s[-1])
            if not ad2 is None:
                d2s.append(ad2)
        if len(d3s) > 0:
            ad3 = self.calc(config, d3s[-1])
            if not ad3 is None:
                d3s.append(ad3)
    print("P12", shape, d1s, d2s, d3s)
    curShape = shape[1:]
    print("curShape", curShape)

    model.add(tf.keras.Input(shape = shape[1:]))
    if classify and config.normalize:
        model.add(layerutils.getNormalLayer(shape))
    for i in range(config.convlayers):
        model.add(Convolution2D(
                        data_format = "channels_last",
                        filters=64, # todo
                        kernel_size = config.kernelsize,
                        strides = config.stride,
                        kernel_regularizer=regularizer,
                        padding='same'))
        newShape = cnnutils.calcConvShape2(config, curShape)
        print("newShape", curShape)
        if newShape is None:
            print("newShape none")
            break
        curShape = newShape
        if config.batchnormalize:
            model.add(BatchNormalization())
        if activation:
            model.add(activation)
        dopool = len(d1s) > i and len(d2s) > i and d1s[i] > 1 and d2s[i] > 1
        newShape = cnnutils.calcPoolShape2(config, curShape)
        print("newShape2", newShape)
        if newShape is not None and config.maxpool > 1:
            model.add(MaxPooling2D(config.maxpool, data_format = "channels_last"))
            curShape = newShape
        if config.dropout > 0:
            model.add(Dropout(config.dropout))
    model.add(Flatten())
    for i in range(config.layers):
        model.add(Dense(config.hidden, kernel_regularizer=regularizer))
        if config.batchnormalize:
            model.add(BatchNormalization())
        if activation:
            model.add(activation)
        if config.dropout > 0:
            model.add(Dropout(config.dropout))
    model.add(Dense(myobj.classes, kernel_regularizer=regularizer))
    if lastactivation:
        model.add(Activation(lastactivation))
    model.summary()

    self.model = model

    self.model.compile(optimizer=optimizer,
                       loss=config.loss,
                       metrics=['accuracy'])
    return

  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    x = self.dense_2(x)
    x = self.dense_3(x)
    #print(herexxx)
    return self.dense_4(x)

  def calc(self, config, x):
      return self.formula(config, x)
      x = (x - config.kernelsize + 2 * (config.kernelsize // 2)) / config.stride + 1
      x = int(x)
      x = x // config.maxpool
      if x < config.maxpool:
          return None
      return x

  def formula(self, config, x):
      padding = config.kernelsize // 2
      x = math.floor((x + 2 * padding - dilation * (config.kernelsize - 1) - 1) / config.stride + 1)
      x = x // config.maxpool
      if x < config.maxpool:
          return None
      return x
