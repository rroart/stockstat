import tensorflow as tf
from tensorflow.keras.layers import Dense, Activation, Dropout, GRU
from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers import Adam

from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config):
    super(Model, self).__init__(config, name='my_model')
    # Define your layers here.
    amodel=Sequential()
    amodel.add(GRU(config.hiddenneurons, input_shape = myobj.size, return_sequences = True, time_major = False))
    amodel.add(Dropout(0.2))
    for i in range(1, config.hiddenlayers):
      print("Adding hidden layer", i)
      amodel.add(GRU(config.hiddenneurons, return_sequences = i != config.hiddenlayers - 1, time_major = False))
      amodel.add(Dropout(0.2))
    amodel.add(Dense(myobj.classes))
    self.model = amodel
    optimizer = Adam(lr = config.lr)
    self.model.compile(optimizer = optimizer,
                       loss='sparse_categorical_crossentropy',
                       metrics=['accuracy'])

  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    x = self.dense_2(x)
    x = self.dense_3(x)
    #print(herexxx)
    return self.dense_4(x)
