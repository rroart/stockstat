import tensorflow as tf
from tensorflow.keras.layers import Dense, Activation, Dropout, GRU, TimeDistributed
from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers import Adam, RMSprop

from .model import MyModel
import model.layerutils as layerutils

class Model(MyModel):

  def __init__(self, myobj, config, classify, shape):
    super(Model, self).__init__(config, classify, name='my_model')

    if classify:
      loss = 'sparse_categorical_crossentropy'
      activation = 'softmax'
      optimizer = Adam(learning_rate = config.lr)
    else:
      loss = 'mean_squared_error'
      activation = 'linear'
      optimizer = RMSprop(learning_rate  = config.lr)

    regularizer = layerutils.getRegularizer(config)

    # Define your layers here.
    amodel=Sequential()
    amodel.add(tf.keras.Input(shape = shape))
    if classify and config.normalize:
        amodel.add(layerutils.getNormalLayer(shape))
    amodel.add(Dropout(config.inputdropout))
    amodel.add(GRU(config.hidden, return_sequences = True, kernel_regularizer=regularizer))
    amodel.add(Dropout(config.dropout))
    for i in range(1, config.layers):
      print("Adding hidden layer", i)
      amodel.add(GRU(config.hidden, return_sequences = i != config.layers - 1, kernel_regularizer=regularizer))
      if config.batchnormalize:
          amodel.add(tf.keras.layers.BatchNormalization())
      amodel.add(tf.keras.layers.Activation('relu'))
      amodel.add(Dropout(config.dropout))
    if classify:
      amodel.add(Dense(myobj.classes, activation = activation, kernel_regularizer=regularizer))
    else:
      amodel.add(Dense(1, activation = activation, kernel_regularizer=regularizer))
    self.model = amodel
    self.model.compile(optimizer = optimizer,
                       loss=loss,
                       metrics=['accuracy'])

  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    x = self.dense_2(x)
    x = self.dense_3(x)
    #print(herexxx)
    return self.dense_4(x)
