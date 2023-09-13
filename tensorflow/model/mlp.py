import tensorflow as tf
import keras
#from tensorflow.keras import layers
from keras.layers import Dense, Activation, Dropout
#from keras.models import Sequential
from tensorflow.keras.optimizers.legacy import Adam, RMSprop

#from .mymodelseq import MyModelSeq
from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config, classify):
    super(Model, self).__init__(config, classify, name='my_model')

    if classify:
      loss = 'sparse_categorical_crossentropy'
      activation = 'softmax'
      optimizer = Adam(learning_rate = config.lr)
    else:
      loss = 'mean_squared_error'
      activation = 'linear'
      optimizer = RMSprop(lr = config.lr)
    
    # Define your layers here.
    amodel=tf.keras.Sequential()
    amodel.add(tf.keras.layers.Dense(config.hidden, activation='relu', input_shape=(myobj.size,)))
    for i in range(0, config.layers):
      print("Adding hidden layer", i)
      amodel.add(tf.keras.layers.Dense(config.hidden, activation='relu'))
    #amodel.add(tf.keras.layers.Dense(myobj.classes))
    if classify:
      amodel.add(tf.keras.layers.Dense(myobj.classes, activation = activation))
    else:
      amodel.add(tf.keras.layers.Dense(1, activation = activation))
    self.model = amodel
    self.dense_1 = Dense(32, activation='relu', input_shape=(myobj.size,))
    self.dense_2 = Dense(32, activation='relu')
    self.dense_3 = Dense(32, activation='relu')
    #self.dense_4 = Dense(myobj.classes, activation='sigmoid')
    self.dense_4 = Dense(myobj.classes, activation='softmax')
    #adam = tf.keras.optimizers.Adam(learning_rate=1)
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
