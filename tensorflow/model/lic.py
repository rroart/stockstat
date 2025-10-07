import tensorflow as tf

from .model import MyModel
import model.layerutils as layerutils

def create_sample_optimizer(tf_version):
    optimizer = tf.keras.optimizers.Ftrl(
        l1_regularization_strength=0.001,
        learning_rate=tf.keras.optimizers.schedules.ExponentialDecay(
            initial_learning_rate=0.1, decay_steps=10000, decay_rate=0.9))
    return optimizer

class Model(MyModel):

  def __init__(self, myobj, config, classify, shape):
    super(Model, self).__init__(config, classify, name='my_model')
    self.model = tf.keras.models.Sequential()
    self.model.add(tf.keras.Input(shape = (shape[1:])))
    # Define the model consisting of a single neuron.
    self.model.add(tf.keras.layers.Dense(units=1))
    self.model.compile(loss='mse', optimizer=create_sample_optimizer('tf2'), metrics=['accuracy'])

  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    return self.dense_2(x)
    
