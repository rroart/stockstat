import tensorflow as tf
import tensorflow_quantum as tfq

import cirq
import sympy
import numpy as np
import seaborn as sns
import collections
#matplotlib inline
import matplotlib.pyplot as plt
from cirq.contrib.svg import SVGCircuit

import mydatasetscommon

def getdataset(myobj, config, classifier):
    if myobj.dataset == 'mnist':
        return getmnist(myobj, config)
    if myobj.dataset == 'fashion_mnist':
        return getfashionmnist(myobj, config)
    if myobj.dataset == 'iris':
        return getiris(myobj, config)
    if myobj.dataset == 'iris2':
        return getiris2(myobj, config)

def getmnist(myobj, config):
        #load mnist data
        (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()

        #print(type(y_train))
        #exit
        normalizevalue = 255.0
        x_train, x_test = x_train[..., np.newaxis]/normalizevalue, x_test[..., np.newaxis]/normalizevalue
        print("shape",x_train.shape, y_train.shape)
        x_train, y_train = filter_36(x_train, y_train)
        x_test, y_test = filter_36(x_test, y_test)
        x_train_small = tf.image.resize(x_train, (4,4)).numpy()
        x_test_small = tf.image.resize(x_test, (4,4)).numpy()
        x_train_nocon, y_train_nocon = remove_contradicting(x_train_small, y_train)
        THRESHOLD = 0.5

        x_train_bin = np.array(x_train_nocon > THRESHOLD, dtype=np.float32)
        x_test_bin = np.array(x_test_small > THRESHOLD, dtype=np.float32)

        _ = remove_contradicting(x_train_bin, y_train_nocon)

        x_train_circ = [convert_to_circuit(x) for x in x_train_bin]
        x_test_circ = [convert_to_circuit(x) for x in x_test_bin]

        SVGCircuit(x_train_circ[0])

        bin_img = x_train_bin[0,:,:,0]
        indices = np.array(np.where(bin_img)).T
        indices

        x_train_tfcirc = tfq.convert_to_tensor(x_train_circ)
        x_test_tfcirc = tfq.convert_to_tensor(x_test_circ)

        dsdict = { 'x_train' : x_train_tfcirc, 'x_test' : x_test_tfcirc, 'y_train' : y_train_nocon, 'y_test' : y_test }
        ds = DictToObject(dsdict)
        meta = DictToObject({ 'classify' : True })
        return ds, meta

def filter_36(x, y):
    keep = (y == 3) | (y == 6)
    x, y = x[keep], y[keep]
    y = y == 3
    return x,y

def remove_contradicting(xs, ys):
    mapping = collections.defaultdict(set)
    orig_x = {}
    # Determine the set of labels for each unique image:
    for x,y in zip(xs,ys):
       orig_x[tuple(x.flatten())] = x
       mapping[tuple(x.flatten())].add(y)

    new_x = []
    new_y = []
    for flatten_x in mapping:
      x = orig_x[flatten_x]
      labels = mapping[flatten_x]
      if len(labels) == 1:
          new_x.append(x)
          new_y.append(next(iter(labels)))
      else:
          # Throw out images that match more than one label.
          pass

    num_uniq_3 = sum(1 for value in mapping.values() if len(value) == 1 and True in value)
    num_uniq_6 = sum(1 for value in mapping.values() if len(value) == 1 and False in value)
    num_uniq_both = sum(1 for value in mapping.values() if len(value) == 2)

    print("Number of unique images:", len(mapping.values()))
    print("Number of unique 3s: ", num_uniq_3)
    print("Number of unique 6s: ", num_uniq_6)
    print("Number of unique contradicting labels (both 3 and 6): ", num_uniq_both)
    print()
    print("Initial number of images: ", len(xs))
    print("Remaining non-contradicting unique images: ", len(new_x))

    return np.array(new_x), np.array(new_y)

def convert_to_circuit(image):
    """Encode truncated classical image into quantum datapoint."""
    values = np.ndarray.flatten(image)
    qubits = cirq.GridQubit.rect(4, 4)
    circuit = cirq.Circuit()
    for i, value in enumerate(values):
        if value:
            circuit.append(cirq.X(qubits[i]))
    return circuit

def getfashionmnist(myobj, config):
    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.fashion_mnist.load_data()

    # Rescale the images from [0,255] to the [0.0,1.0] range.
    x_train, x_test = x_train/255.0, x_test/255.0

    print("Number of original training examples:", len(x_train))
    print("Number of original test examples:", len(x_test))

    x_train, y_train = filter_03(x_train, y_train)
    x_test, y_test = filter_03(x_test, y_test)

    print("Number of filtered training examples:", len(x_train))
    print("Number of filtered test examples:", len(x_test))

    print(y_train[0])

    plt.imshow(x_train[0, :, :])
    plt.colorbar()

    DATASET_DIM = 10
    x_train, x_test = truncate_x(x_train, x_test, n_components=DATASET_DIM)
    print(f'New datapoint dimension:', len(x_train[0]))

    N_TRAIN = 1000
    N_TEST = 200
    x_train, x_test = x_train[:N_TRAIN], x_test[:N_TEST]
    y_train, y_test = y_train[:N_TRAIN], y_test[:N_TEST]

    print("New number of training examples:", len(x_train))
    print("New number of test examples:", len(x_test))

    SVGCircuit(single_qubit_wall(
        cirq.GridQubit.rect(1,4), np.random.uniform(size=(4, 3))))

    test_circuit, test_symbols = v_theta(cirq.GridQubit.rect(1, 2))
    print(f'Symbols found in circuit:{test_symbols}')
    SVGCircuit(test_circuit)

    qubits = cirq.GridQubit.rect(1, DATASET_DIM + 1)
    q_x_train_circuits = prepare_pqk_circuits(qubits, x_train)
    q_x_test_circuits = prepare_pqk_circuits(qubits, x_test)

    x_train_pqk = get_pqk_features(qubits, q_x_train_circuits)
    x_test_pqk = get_pqk_features(qubits, q_x_test_circuits)
    print('New PQK training dataset has shape:', x_train_pqk.shape)
    print('New PQK testing dataset has shape:', x_test_pqk.shape)

    S_pqk, V_pqk = get_spectrum(
        tf.reshape(tf.concat([x_train_pqk, x_test_pqk], 0), [-1, len(qubits) * 3]))

    S_original, V_original = get_spectrum(
        tf.cast(tf.concat([x_train, x_test], 0), tf.float32), gamma=0.005)

    print('Eigenvectors of pqk kernel matrix:', V_pqk)
    print('Eigenvectors of original kernel matrix:', V_original)

    y_relabel = get_stilted_dataset(S_pqk, V_pqk, S_original, V_original)
    y_train_new, y_test_new = y_relabel[:N_TRAIN], y_relabel[N_TRAIN:]

    dsdict = {'x_train': x_train_pqk, 'x_test': x_test_pqk, 'y_train': y_train_new,
              'y_test': y_test_new }
    ds = DictToObject(dsdict)
    meta = DictToObject({'classify': True})
    return ds, meta

    #docs_infra: no_execute
    plt.figure(figsize=(10,5))
    plt.plot(classical_history.history['accuracy'], label='accuracy_classical')
    plt.plot(classical_history.history['val_accuracy'], label='val_accuracy_classical')
    plt.plot(pqk_history.history['accuracy'], label='accuracy_quantum')
    plt.plot(pqk_history.history['val_accuracy'], label='val_accuracy_quantum')
    plt.xlabel('Epoch')
    plt.ylabel('Accuracy')
    plt.legend()

def filter_03(x, y):
    keep = (y == 0) | (y == 3)
    x, y = x[keep], y[keep]
    y = y == 0
    return x,y

def truncate_x(x_train, x_test, n_components=10):
  """Perform PCA on image dataset keeping the top `n_components` components."""
  n_points_train = tf.gather(tf.shape(x_train), 0)
  n_points_test = tf.gather(tf.shape(x_test), 0)

  # Flatten to 1D
  x_train = tf.reshape(x_train, [n_points_train, -1])
  x_test = tf.reshape(x_test, [n_points_test, -1])

  # Normalize.
  feature_mean = tf.reduce_mean(x_train, axis=0)
  x_train_normalized = x_train - feature_mean
  x_test_normalized = x_test - feature_mean

  # Truncate.
  e_values, e_vectors = tf.linalg.eigh(
      tf.einsum('ji,jk->ik', x_train_normalized, x_train_normalized))
  return tf.einsum('ij,jk->ik', x_train_normalized, e_vectors[:,-n_components:]), \
    tf.einsum('ij,jk->ik', x_test_normalized, e_vectors[:, -n_components:])

def single_qubit_wall(qubits, rotations):
  """Prepare a single qubit X,Y,Z rotation wall on `qubits`."""
  wall_circuit = cirq.Circuit()
  for i, qubit in enumerate(qubits):
    for j, gate in enumerate([cirq.X, cirq.Y, cirq.Z]):
      wall_circuit.append(gate(qubit) ** rotations[i][j])

  return wall_circuit

def v_theta(qubits):
  """Prepares a circuit that generates V(\theta)."""
  ref_paulis = [
      cirq.X(q0) * cirq.X(q1) + \
      cirq.Y(q0) * cirq.Y(q1) + \
      cirq.Z(q0) * cirq.Z(q1) for q0, q1 in zip(qubits, qubits[1:])
  ]
  exp_symbols = list(sympy.symbols('ref_0:'+str(len(ref_paulis))))
  return tfq.util.exponential(ref_paulis, exp_symbols), exp_symbols

def prepare_pqk_circuits(qubits, classical_source, n_trotter=10):
  """Prepare the pqk feature circuits around a dataset."""
  n_qubits = len(qubits)
  n_points = len(classical_source)

  # Prepare random single qubit rotation wall.
  random_rots = np.random.uniform(-2, 2, size=(n_qubits, 3))
  initial_U = single_qubit_wall(qubits, random_rots)

  # Prepare parametrized V
  V_circuit, symbols = v_theta(qubits)
  exp_circuit = cirq.Circuit(V_circuit for t in range(n_trotter))

  # Convert to `tf.Tensor`
  initial_U_tensor = tfq.convert_to_tensor([initial_U])
  initial_U_splat = tf.tile(initial_U_tensor, [n_points])

  full_circuits = tfq.layers.AddCircuit()(
      initial_U_splat, append=exp_circuit)
  # Replace placeholders in circuits with values from `classical_source`.
  return tfq.resolve_parameters(
      full_circuits, tf.convert_to_tensor([str(x) for x in symbols]),
      tf.convert_to_tensor(classical_source*(n_qubits/3)/n_trotter))

def get_pqk_features(qubits, data_batch):
  """Get PQK features based on above construction."""
  ops = [[cirq.X(q), cirq.Y(q), cirq.Z(q)] for q in qubits]
  ops_tensor = tf.expand_dims(tf.reshape(tfq.convert_to_tensor(ops), -1), 0)
  batch_dim = tf.gather(tf.shape(data_batch), 0)
  ops_splat = tf.tile(ops_tensor, [batch_dim, 1])
  exp_vals = tfq.layers.Expectation()(data_batch, operators=ops_splat)
  rdm = tf.reshape(exp_vals, [batch_dim, len(qubits), -1])
  return rdm

def compute_kernel_matrix(vecs, gamma):
  """Computes d[i][j] = e^ -gamma * (vecs[i] - vecs[j]) ** 2 """
  scaled_gamma = gamma / (
      tf.cast(tf.gather(tf.shape(vecs), 1), tf.float32) * tf.math.reduce_std(vecs))
  return scaled_gamma * tf.einsum('ijk->ij',(vecs[:,None,:] - vecs) ** 2)

def get_spectrum(datapoints, gamma=1.0):
  """Compute the eigenvalues and eigenvectors of the kernel of datapoints."""
  KC_qs = compute_kernel_matrix(datapoints, gamma)
  S, V = tf.linalg.eigh(KC_qs)
  S = tf.math.abs(S)
  return S, V

def get_stilted_dataset(S, V, S_2, V_2, lambdav=1.1):
  """Prepare new labels that maximize geometric distance between kernels."""
  S_diag = tf.linalg.diag(S ** 0.5)
  S_2_diag = tf.linalg.diag(S_2 / (S_2 + lambdav) ** 2)
  scaling = S_diag @ tf.transpose(V) @ \
            V_2 @ S_2_diag @ tf.transpose(V_2) @ \
            V @ S_diag

  # Generate new lables using the largest eigenvector.
  _, vecs = tf.linalg.eig(scaling)
  new_labels = tf.math.real(
      tf.einsum('ij,j->i', tf.cast(V @ S_diag, tf.complex64), vecs[-1])).numpy()
  # Create new labels and add some small amount of noise.
  final_y = new_labels > np.median(new_labels)
  noisy_y = (final_y ^ (np.random.uniform(size=final_y.shape) > 0.95))
  return noisy_y

#docs_infra: no_execute
def create_fair_classical_model(DATASET_DIM):
    model = tf.keras.Sequential()
    model.add(tf.keras.layers.Dense(32, activation='sigmoid', input_shape=[DATASET_DIM,]))
    model.add(tf.keras.layers.Dense(16, activation='sigmoid'))
    model.add(tf.keras.layers.Dense(1))
    return model


def getiris(myobj, config):
    x_train, y_train, x_test, y_test = mydatasetscommon.iriscommon()
    x_train = np.array(x_train)
    y_train = np.array(y_train)
    x_test = np.array(x_test)
    y_test = np.array(y_test)

    y_train = y_train.reshape(-1)
    y_test = y_test.reshape(-1)

    normalizevalue = 8.0
    print("shape",x_train.shape, y_train.shape)
    x_train, x_test = x_train[..., np.newaxis] / normalizevalue, x_test[..., np.newaxis] / normalizevalue
    print("shape",x_train.shape, y_train.shape)
    x_train, y_train = filter_12(x_train, y_train)
    x_test, y_test = filter_12(x_test, y_test)
    x_train_small = x_train #tf.image.resize(x_train, (4, 4)).numpy()
    x_test_small = x_test #tf.image.resize(x_test, (4, 4)).numpy()
    x_train_nocon, y_train_nocon = remove_contradicting(x_train_small, y_train)
    THRESHOLD = 0.5

    x_train_bin = np.array(x_train_nocon > THRESHOLD, dtype=np.float32)
    x_test_bin = np.array(x_test_small > THRESHOLD, dtype=np.float32)

    _ = remove_contradicting(x_train_bin, y_train_nocon)

    x_train_circ = [convert_to_circuit(x) for x in x_train_bin]
    x_test_circ = [convert_to_circuit(x) for x in x_test_bin]

    SVGCircuit(x_train_circ[0])

    bin_img = x_train_bin[0, :, :]
    indices = np.array(np.where(bin_img)).T
    indices

    x_train_tfcirc = tfq.convert_to_tensor(x_train_circ)
    x_test_tfcirc = tfq.convert_to_tensor(x_test_circ)

    dsdict = {'x_train': x_train_tfcirc, 'x_test': x_test_tfcirc, 'y_train': y_train_nocon,
              'y_test': y_test}
    ds = DictToObject(dsdict)
    meta = DictToObject({'classify': True})
    return ds, meta


def filter_12(x, y):
    keep = (y == 1) | (y == 2)
    x, y = x[keep], y[keep]
    y = y == 1
    return x,y


def getiris2(myobj, config):
    x_train, y_train, x_test, y_test = mydatasetscommon.iriscommon()
    x_train = np.array(x_train)
    y_train = np.array(y_train)
    x_test = np.array(x_test)
    y_test = np.array(y_test)

    y_train = y_train.reshape(-1)
    y_test = y_test.reshape(-1)

    normalizevalue = 8.0
    print("shape",x_train.shape, y_train.shape)
    x_train, x_test = x_train[..., np.newaxis] / normalizevalue, x_test[..., np.newaxis] / normalizevalue
    print("shape",x_train.shape, y_train.shape)

    x_train, y_train = filter_12(x_train, y_train)
    x_test, y_test = filter_12(x_test, y_test)

    print("Number of filtered training examples:", len(x_train))
    print("Number of filtered test examples:", len(x_test))

    print(y_train[0])

    plt.imshow(x_train[0, :, :])
    plt.colorbar()

    DATASET_DIM = 10
    x_train, x_test = truncate_x(x_train, x_test, n_components=DATASET_DIM)
    print(f'New datapoint dimension:', len(x_train[0]))

    N_TRAIN = 1000
    N_TEST = 200
    x_train, x_test = x_train[:N_TRAIN], x_test[:N_TEST]
    y_train, y_test = y_train[:N_TRAIN], y_test[:N_TEST]

    print("New number of training examples:", len(x_train))
    print("New number of test examples:", len(x_test))

    SVGCircuit(single_qubit_wall(
        cirq.GridQubit.rect(1,4), np.random.uniform(size=(4, 3))))

    test_circuit, test_symbols = v_theta(cirq.GridQubit.rect(1, 2))
    print(f'Symbols found in circuit:{test_symbols}')
    SVGCircuit(test_circuit)

    qubits = cirq.GridQubit.rect(1, DATASET_DIM + 1)
    q_x_train_circuits = prepare_pqk_circuits(qubits, x_train)
    q_x_test_circuits = prepare_pqk_circuits(qubits, x_test)

    x_train_pqk = get_pqk_features(qubits, q_x_train_circuits)
    x_test_pqk = get_pqk_features(qubits, q_x_test_circuits)
    print('New PQK training dataset has shape:', x_train_pqk.shape)
    print('New PQK testing dataset has shape:', x_test_pqk.shape)

    S_pqk, V_pqk = get_spectrum(
        tf.reshape(tf.concat([x_train_pqk, x_test_pqk], 0), [-1, len(qubits) * 3]))

    S_original, V_original = get_spectrum(
        tf.cast(tf.concat([x_train, x_test], 0), tf.float32), gamma=0.005)

    print('Eigenvectors of pqk kernel matrix:', V_pqk)
    print('Eigenvectors of original kernel matrix:', V_original)

    y_relabel = get_stilted_dataset(S_pqk, V_pqk, S_original, V_original)
    y_train_new, y_test_new = y_relabel[:N_TRAIN], y_relabel[N_TRAIN:]

    dsdict = {'x_train': x_train_pqk, 'x_test': x_test_pqk, 'y_train': y_train_new,
              'y_test': y_test_new }
    ds = DictToObject(dsdict)
    meta = DictToObject({'classify': True})
    return ds, meta

    #docs_infra: no_execute
    plt.figure(figsize=(10,5))
    plt.plot(classical_history.history['accuracy'], label='accuracy_classical')
    plt.plot(classical_history.history['val_accuracy'], label='val_accuracy_classical')
    plt.plot(pqk_history.history['accuracy'], label='accuracy_quantum')
    plt.plot(pqk_history.history['val_accuracy'], label='val_accuracy_quantum')
    plt.xlabel('Epoch')
    plt.ylabel('Accuracy')
    plt.legend()


class DictToObject:
    def __init__(self, dictionary):
        for key, value in dictionary.items():
            setattr(self, key, value)

