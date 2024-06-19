import tensorflow as tf

from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config, classify):
    super(Model, self).__init__(config, classify, name='my_model')
    normalizer = tf.keras.layers.Normalization(input_shape=[1,], axis=None)
    #normalizer.adapt(
    self.model =  tf.keras.models.Sequential()
    # Define the model consisting of a single neuron.
    self.model.add(tf.keras.layers.Dense(units=1)) #, input_shape=(myobj.size,)))
    self.model.compile(#optimizer=tf.keras.optimizers.RMSprop(lr=.005),
                       loss='mse')
        
  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    return self.dense_2(x)
    
