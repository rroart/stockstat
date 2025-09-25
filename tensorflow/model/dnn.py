import tensorflow as tf
from tensorflow.keras.optimizers import Adam, RMSprop

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
    optimizer = layerutils.getOptimizer(config)
    regularizer = layerutils.getRegularizer(config)
    activation = config.activation
    lastactivation = config.lastactivation

    hidden_units = [ config.hidden ] * config.layers

    self.model = tf.keras.models.Sequential()
    self.model.add(tf.keras.Input(shape = (shape,)))
    if classify and config.normalize:
        self.model.add(layerutils.getNormalLayer(shape))
    self.model.add(tf.keras.layers.Dense(config.hidden, activation=activation))
    if False and classify:
        self.model.add(tf.keras.layers.Dense(myobj.classes, activation = lastactivation))
    else:
        self.model.add(tf.keras.layers.Dense(1, activation = lastactivation))
        
    self.model.compile(loss=config.loss, optimizer=optimizer, metrics=['accuracy'])

  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    return self.dense_2(x)
