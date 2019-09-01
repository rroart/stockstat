import tensorflow as tf
import keras
#from tensorflow.keras import layers
from keras.layers import Dense, Activation, Dropout
#from keras.models import Sequential

#from .mymodelseq import MyModelSeq
from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config):
    super(Model, self).__init__(config, name='my_model')
    # Define your layers here.
    amodel=tf.keras.Sequential()
    amodel.add(tf.keras.layers.Dense(config.hiddenunits, activation='relu', input_shape=(myobj.size,)))
    for i in range(0, config.hiddenlayers):
      print("Adding hidden layer", i)
      amodel.add(tf.keras.layers.Dense(config.hiddenunits, activation='relu'))
    amodel.add(tf.keras.layers.Dense(myobj.classes, activation='softmax'))
    self.model = amodel
    self.dense_1 = Dense(32, activation='relu', input_shape=(myobj.size,))
    self.dense_2 = Dense(32, activation='relu')
    self.dense_3 = Dense(32, activation='relu')
    #self.dense_4 = Dense(myobj.classes, activation='sigmoid')
    self.dense_4 = Dense(myobj.classes, activation='softmax')
    #adam = tf.keras.optimizers.Adam(lr=1)
    self.model.compile(optimizer='adam',
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
