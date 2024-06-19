import tensorflow as tf
from tensorflow.keras.optimizers import Adam, RMSprop

from .model import MyModel
    
def create_sample_optimizer(tf_version):
    optimizer = tf.keras.optimizers.Ftrl(
        l1_regularization_strength=0.001,
        learning_rate=tf.keras.optimizers.schedules.ExponentialDecay(
            initial_learning_rate=0.1, decay_steps=10000, decay_rate=0.9))
    return optimizer

class Model(MyModel):

  def __init__(self, myobj, config, classify):
    super(Model, self).__init__(config, classify, name='my_model')
    hidden_units = [ config.hidden ] * config.layers
    activation = 'relu'
    self.model = tf.keras.models.Sequential()
    self.model.add(tf.keras.layers.Dense(config.hidden, activation='relu'))
    if False and classify:
        self.model.add(tf.keras.layers.Dense(myobj.classes, activation = activation))
    else:
        self.model.add(tf.keras.layers.Dense(1, activation = activation))
        
    optimizer = Adam(learning_rate = config.lr)
    self.model.compile(loss='mse', optimizer=optimizer, metrics=['accuracy'])

  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    return self.dense_2(x)
