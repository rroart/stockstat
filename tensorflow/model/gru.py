import tensorflow as tf
from tensorflow.keras.layers import Dense, Activation, Dropout, GRU, TimeDistributed
from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers import Adam, RMSprop

from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config, classify):
    super(Model, self).__init__(config, classify, name='my_model')

    if classify:
      loss = 'sparse_categorical_crossentropy'
      activation = 'softmax'
      optimizer = Adam(lr = config.lr)
    else:
      loss = 'mean_squared_error'
      activation = 'linear'
      optimizer = RMSprop(lr = config.lr)

    # Define your layers here.
    amodel=Sequential()
    amodel.add(Dropout(config.dropoutin, input_shape = myobj.size))
    amodel.add(GRU(config.hidden, return_sequences = True, time_major = False))
    amodel.add(Dropout(config.dropout))
    for i in range(1, config.layers):
      print("Adding hidden layer", i)
      amodel.add(GRU(config.hidden, return_sequences = i != config.layers - 1, time_major = False))
      amodel.add(Dropout(config.dropout))
    if classify:
      amodel.add(Dense(myobj.classes, activation = activation))
    else:
      amodel.add(Dense(1, activation = activation))
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
