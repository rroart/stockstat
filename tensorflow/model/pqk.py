import tensorflow as tf
import tensorflow_quantum as tfq
import cirq
from cirq.contrib.svg import SVGCircuit
import sympy

from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config, classify):
      super(Model, self).__init__(config, classify, name='my_model')

      qubits = cirq.GridQubit.rect(1, DATASET_DIM + 1) # dup
      # Build the Keras model.
      self.pqk_model = create_pqk_model(qubits)
      self.pqk_model.compile(loss=tf.keras.losses.BinaryCrossentropy(from_logits=True),
                        optimizer=tf.keras.optimizers.Adam(learning_rate=0.003),
                        metrics=['accuracy'])

      self.pqk_model.summary()

  def train(self, dataset):
      # docs_infra: no_execute
      pqk_history = self.pqk_model.fit(tf.reshape(dataset.x_train, [N_TRAIN, -1]),
                                  dataset.y_train,
                                  batch_size=32,
                                  epochs=10,
                                  verbose=0,
                                  validation_data=(tf.reshape(dataset.x_test, [N_TEST, -1]), dataset.y_test))

      print("History", pqk_history)
      #pqk_results = self.pqk_model.evaluate(dataset.x_test, dataset.y_test)
      #print("Results", pqk_results)
      return 0, 0, 0

  def localsave(self):
      return False

#docs_infra: no_execute
def create_pqk_model(qubits):
    model = tf.keras.Sequential()
    model.add(tf.keras.layers.Dense(32, activation='sigmoid', input_shape=[len(qubits) * 3,]))
    model.add(tf.keras.layers.Dense(16, activation='sigmoid'))
    model.add(tf.keras.layers.Dense(1))
    return model


DATASET_DIM = 10
N_TRAIN = 1000
N_TEST = 200

