import tensorflow as tf
from tensorflow.keras.layers import Dense, Activation, Dropout, SimpleRNN, RNN, Flatten, TimeDistributed
from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers import Adam, RMSprop
import model.layerutils as layerutils

from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config, classify, shape):
    super(Model, self).__init__(config, classify, name='my_model')

    optimizer = layerutils.getOptimizer(config)
    regularizer = layerutils.getRegularizer(config)
    activation = tf.keras.layers.Activation(config.activation)
    lastactivation = tf.keras.layers.Activation(config.lastactivation)

    #loss = 'sparse_categorical_crossentropy'
    # Define your layers here.
    # https://subscription.packtpub.com/book/big_data_and_business_intelligence/9781788292061/7/ch07lvl1sec59/simple-rnn-with-keras
    amodel=Sequential()
    # add Input
    #print("ooo",myobj.size)
    amodel.add(tf.keras.Input(shape = shape[1:]))
    if classify and config.normalize:
        amodel.add(layerutils.getNormalLayer(shape))
    if config.inputdropout > 0:
        amodel.add(Dropout(config.inputdropout))
    amodel.add(SimpleRNN(config.hidden, return_sequences = True, kernel_regularizer=regularizer))
    if config.batchnormalize:
        amodel.add(tf.keras.layers.BatchNormalization())
    if config.dropout > 0:
        amodel.add(Dropout(config.dropout))
    for i in range(1, config.layers):
      print("Adding hidden layer", i)
      amodel.add(SimpleRNN(config.hidden, return_sequences = i != config.layers - 1, kernel_regularizer=regularizer))
      if config.batchnormalize:
          amodel.add(tf.keras.layers.BatchNormalization())
      amodel.add(activation)
      if config.dropout > 0:
          amodel.add(Dropout(config.dropout))
    amodel.add(Flatten())
    if classify:
      amodel.add(Dense(myobj.classes, activation = lastactivation, kernel_regularizer=regularizer))
    else:
      amodel.add(Dense(1, activation = lastactivation, kernel_regularizer=regularizer))
    self.model = amodel
    self.model.compile(optimizer = optimizer,
                       loss=config.loss,
                       metrics=['accuracy'])

  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    x = self.dense_2(x)
    x = self.dense_3(x)
    #print(herexxx)
    return self.dense_4(x)
