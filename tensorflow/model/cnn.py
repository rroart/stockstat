import math

import tensorflow as tf
from tensorflow.keras.layers import Dense, Activation, Dropout, Conv1D, MaxPooling1D, Flatten, Convolution1D, BatchNormalization, LeakyReLU, Dropout
from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers import Adam
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

    # Define your layers here.

    # https://machinelearningmastery.com/how-to-develop-convolutional-neural-network-models-for-time-series-forecasting/
    # define model
    modelj = Sequential()
    #, input_shape=(n_steps, n_features)
    modelj.add(Conv1D(filters=64, kernel_size=2, activation='relu', input_shape = (28, 28)))
    modelj.add(MaxPooling1D(pool_size=2))
    modelj.add(Flatten())
    modelj.add(Dense(50, activation='relu'))
    modelj.add(Dense(1))
    d11 = self.calc(config, shape[1])
    d21 = self.calc(config, shape[2])
    d1s = []
    d2s = []
    if not d11 is None:
        d1s.append(d11)
    if not d21 is None:
        d2s.append(d21)
    for i in range(config.convlayers - 1):
        if len(d1s) > 0:
            ad1 = self.calc(config, d1s[-1])
            if not ad1 is None:
                d1s.append(ad1)
        if len(d2s) > 0:
            ad2 = self.calc(config, d2s[-1])
            if not ad2 is None:
                d2s.append(ad2)

    print("P12", shape, d1s, d2s)
    curShape = shape[1:]
    print("curShape", curShape)

    #https://medium.com/@alexrachnog/neural-networks-for-algorithmic-trading-2-1-multivariate-time-series-ab016ce70f57
    modelm = Sequential()
    # input_shape = (WINDOW, EMB_SIZE),
    modelm.add(tf.keras.Input(shape = shape[1:]))
    if classify and config.normalize:
        modelm.add(layerutils.getNormalLayer(shape))
    for i in range(config.convlayers):
        modelm.add(Convolution1D(
                        data_format = "channels_last",
                        filters=16, # todo
                        kernel_size = config.kernelsize,
                        strides = config.stride,
                        kernel_regularizer=regularizer,
                        padding='same'))
        newShape = cnnutils.calcConvShape(config, curShape)
        print("newShape", curShape)
        if newShape is None:
            print("newShape none")
            break
        curShape = newShape
        if config.batchnormalize:
            modelm.add(BatchNormalization())
        modelm.add(activation)
        dopool = len(d1s) > i and len(d2s) > i and d1s[i] > 1 and d2s[i] > 1
        dopool = len(d1s) > i and d1s[i] > 1
        newShape = cnnutils.calcPoolShape(config, curShape)
        print("newShape2", newShape)
        if newShape is not None and config.maxpool > 1:
            modelm.add(MaxPooling1D(config.maxpool, data_format = "channels_last"))
            curShape = newShape
        if config.dropout > 0:
            modelm.add(Dropout(config.dropout))
    modelm.add(Flatten())
    for i in range(config.layers):
        modelm.add(Dense(config.hidden, kernel_regularizer=regularizer))
        if config.batchnormalize:
            modelm.add(BatchNormalization())
        modelm.add(activation)
        if config.dropout > 0:
            modelm.add(Dropout(config.dropout))
    modelm.add(Dense(myobj.classes, kernel_regularizer=regularizer))
    modelm.add(lastactivation)
    
    #https://medium.com/@alexrachnog/neural-networks-for-algorithmic-trading-part-one-simple-time-series-forecasting-f992daa1045a
    model1 = Sequential()
    # input_shape = (TRAIN_SIZE, EMB_SIZE), 
    #model1.add(Convolution1D(input_shape = (28, 28),
    #                    nb_filter=64,
    #                    filter_length=2,
    #                    border_mode='valid',
    #                    activation='relu',
    #                    subsample_length=1))
    #model1.add(MaxPooling1D(pool_length=2))

    # input_shape = (TRAIN_SIZE, EMB_SIZE), 
    #model1.add(Convolution1D(input_shape = (28, 28),
    #                    nb_filter=64,
    #                    filter_length=2,
    #                    border_mode='valid',
    #                    activation='relu',
    #                    subsample_length=1))
    #model1.add(MaxPooling1D(pool_length=2))

    #model1.add(Dropout(0.25))
    #model1.add(Flatten())

    #model1.add(Dense(250))
    #model1.add(Dropout(0.25))
    #model1.add(Activation('relu'))
    
    #model1.add(Dense(1))
    #model1.add(Activation('linear'))
    #end
    
    self.model = modelm
    self.dense_1 = Dense(32, activation='relu')
    self.dense_2 = Dense(32, activation='relu')
    self.dense_3 = Dense(32, activation='relu')
    #self.dense_4 = Dense(myobj.classes, activation='sigmoid')
    self.dense_4 = Dense(myobj.classes, activation='softmax')

    self.model.compile(optimizer=optimizer,
                       loss=config.loss,
                       metrics=['accuracy'])
    return
    # https://www.kaggle.com/heyhello/mnist-simple-convnet/data
    model = models.Sequential()
    model.add(layers.Conv2D(32, (3, 3), activation='relu', input_shape=(28, 28, 1)))
    model.add(layers.MaxPooling2D((2, 2)))
    model.add(layers.Conv2D(64, (3, 3), activation='relu'))
    model.add(layers.MaxPooling2D((2, 2)))
    model.add(layers.Conv2D(64, (3, 3), activation='relu'))
    model.add(layers.Flatten())
    model.add(layers.Dense(64, activation='relu'))
    model.add(layers.Dense(10, activation='softmax'))


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
