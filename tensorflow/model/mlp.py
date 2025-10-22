import tensorflow as tf
import keras
#from tensorflow.keras import layers
from keras.layers import Dense, Activation, Dropout #, regularizers
#from keras.models import Sequential
from tensorflow.keras.optimizers import Adam, RMSprop

from . import layerutils
#from .mymodelseq import MyModelSeq
from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config, classify, shape):
    super(Model, self).__init__(config, classify, name='my_model')

    optimizer = layerutils.getOptimizer(config)
    regularizer = layerutils.getRegularizer(config)
    activation = tf.keras.layers.Activation(config.activation)
    lastactivation = tf.keras.layers.Activation(config.lastactivation)

    # Define your layers here.
    print("sh", shape, type(shape))
    if len(shape) != 2:
        raise ValueError("MLP only supports 2D input, shape:" + str(shape))
    amodel=tf.keras.Sequential()
    amodel.add(tf.keras.Input(shape = shape[1:]))
    if classify and config.normalize:
        amodel.add(layerutils.getNormalLayer(shape))
    if config.batchnormalize:
        amodel.add(tf.keras.layers.BatchNormalization())
    if config.dropout > 0:
        amodel.add(Dropout(config.inputdropout))
    for i in range(config.layers):
      print("Adding hidden layer", i)
      amodel.add(tf.keras.layers.Dense(config.hidden, kernel_regularizer=regularizer))
      if config.batchnormalize:
          amodel.add(tf.keras.layers.BatchNormalization())
      amodel.add(activation)
      if config.dropout > 0:
          amodel.add(Dropout(config.dropout))
    if classify:
      amodel.add(tf.keras.layers.Dense(myobj.classes, kernel_regularizer=regularizer))
    else:
      amodel.add(tf.keras.layers.Dense(1, kernel_regularizer=regularizer))
    amodel.add(lastactivation)
    self.model = amodel
    self.dense_1 = Dense(32, activation=config.activation)
    self.dense_2 = Dense(32, activation=config.activation)
    self.dense_3 = Dense(32, activation=config.activation)
    #self.dense_4 = Dense(myobj.classes, activation='sigmoid')
    self.dense_4 = Dense(myobj.classes, activation=config.lastactivation)
    #adam = tf.keras.optimizers.Adam(learning_rate=1)
    self.model.compile(optimizer = optimizer,
                       loss=config.loss,
                       metrics=['accuracy'])

  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    x = self.dense_2(x)
    x = self.dense_3(x)
    print("herexxx")
    return self.dense_4(x)
