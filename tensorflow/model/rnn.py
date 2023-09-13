import tensorflow as tf
from tensorflow.keras.layers import Dense, Activation, Dropout, SimpleRNN, RNN, Flatten, TimeDistributed
from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers.legacy import Adam, RMSprop

from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config, classify):
    super(Model, self).__init__(config, classify, name='my_model')

    #print("class", classify)
    if classify:
      loss = 'sparse_categorical_crossentropy'
      activation = 'softmax'
      optimizer = Adam(learning_rate = config.lr)
    else:
      loss = 'mean_squared_error'
      activation = 'linear'
      optimizer = RMSprop(lr = config.lr)

    #loss = 'sparse_categorical_crossentropy'
    # Define your layers here.
    # https://subscription.packtpub.com/book/big_data_and_business_intelligence/9781788292061/7/ch07lvl1sec59/simple-rnn-with-keras
    amodel=Sequential()
    #print("ooo",myobj.size)
    amodel.add(Dropout(config.dropoutin, input_shape = myobj.size))
    amodel.add(SimpleRNN(config.hidden, return_sequences = True))
    # , time_major = False
    amodel.add(Dropout(config.dropout))
    for i in range(1, config.layers):
      print("Adding hidden layer", i)
      amodel.add(SimpleRNN(config.hidden, return_sequences = i != config.layers - 1, time_major = False))
      amodel.add(Dropout(config.dropout))
    amodel.add(Flatten())
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
