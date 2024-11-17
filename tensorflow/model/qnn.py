import tensorflow as tf
import tensorflow_quantum as tfq
import cirq
from cirq.contrib.svg import SVGCircuit
import sympy

from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config, classify):
      super(Model, self).__init__(config, classify, name='my_model')

      demo_builder = CircuitLayerBuilder(data_qubits=cirq.GridQubit.rect(4, 1),
                                         readout=cirq.GridQubit(-1, -1))

      circuit = cirq.Circuit()
      demo_builder.add_layer(circuit, gate=cirq.XX, prefix='xx')
      SVGCircuit(circuit)

      model_circuit, model_readout = create_quantum_model()

      # Build the Keras model.
      self.model = tf.keras.Sequential([
          # The input is the data-circuit, encoded as a tf.string
          tf.keras.layers.Input(shape=(), dtype=tf.string),
          # The PQC layer returns the expected value of the readout gate, range [-1,1].
          tfq.layers.PQC(model_circuit, model_readout),
      ])

      self.model.compile(
          loss=tf.keras.losses.Hinge(),
          optimizer=tf.keras.optimizers.Adam(),
          metrics=[hinge_accuracy])

      print(self.model.summary())

  def train(self, dataset):
      EPOCHS = 3
      BATCH_SIZE = 32

      NUM_EXAMPLES = len(dataset.x_train_tfcirc)

      x_train_tfcirc_sub = dataset.x_train_tfcirc[:NUM_EXAMPLES]
      y_train_hinge_sub = dataset.y_train_hinge[:NUM_EXAMPLES]

      qnn_history = self.model.fit(
          x_train_tfcirc_sub, y_train_hinge_sub,
          batch_size=BATCH_SIZE,
          epochs=EPOCHS,
          verbose=1,
          validation_data=(dataset.x_test_tfcirc, dataset.y_test_hinge))

      print("History", qnn_history)
      qnn_results = self.model.evaluate(dataset.x_test_tfcirc, dataset.y_test)
      print("Results", qnn_results)

  def localsave(self):
      return False

class CircuitLayerBuilder():
    def __init__(self, data_qubits, readout):
        self.data_qubits = data_qubits
        self.readout = readout

    def add_layer(self, circuit, gate, prefix):
        for i, qubit in enumerate(self.data_qubits):
            symbol = sympy.Symbol(prefix + '-' + str(i))
            circuit.append(gate(qubit, self.readout)**symbol)

def create_quantum_model():
    """Create a QNN model circuit and readout operation to go along with it."""
    data_qubits = cirq.GridQubit.rect(4, 4)  # a 4x4 grid.
    readout = cirq.GridQubit(-1, -1)         # a single qubit at [-1,-1]
    circuit = cirq.Circuit()

    # Prepare the readout qubit.
    circuit.append(cirq.X(readout))
    circuit.append(cirq.H(readout))

    builder = CircuitLayerBuilder(
        data_qubits = data_qubits,
        readout=readout)

    # Then add layers (experiment by adding more).
    builder.add_layer(circuit, cirq.XX, "xx1")
    builder.add_layer(circuit, cirq.ZZ, "zz1")

    # Finally, prepare the readout qubit.
    circuit.append(cirq.H(readout))

    return circuit, cirq.Z(readout)

def hinge_accuracy(y_true, y_pred):
    y_true = tf.squeeze(y_true) > 0.0
    y_pred = tf.squeeze(y_pred) > 0.0
    result = tf.cast(y_true == y_pred, tf.float32)

    return tf.reduce_mean(result)

