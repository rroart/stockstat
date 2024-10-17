import tensorflow as tf
import numpy as np
import keras
import os
import gdown
from zipfile import ZipFile

#import pandas as pd
from keras import backend as K

def getdataset3(myobj, config, classifier):
    if myobj.dataset == 'gpt2':
        return None, None, None, None
    if myobj.dataset == 'simplebooks':
        dirs = simplebooksdir(myobj, config)
        return simplebooks(myobj, config, classifier, dirs[0])
    if myobj.dataset == 'reddit_tifu':
        import tensorflow_datasets as tfds
        reddit_ds = tfds.load("reddit_tifu", split="train", as_supervised=True, data_dir = "/tmp")
        for document, title in reddit_ds:
            print(document.numpy())
            print(title.numpy())
            break
        train_ds = (
            reddit_ds.map(lambda document, _: document)
            .batch(32)
            .cache()
            .prefetch(tf.data.AUTOTUNE)
        )

        if hasattr(config, 'take'):
            train_ds = train_ds.take(config.take)

        return train_ds, None, None, None

    if myobj.dataset == 'imdb':
        dirs = imdbdir(myobj, config)
    else:
        dirs = filenamedir(myobj, config)
    return do_dir(myobj, config, dirs)

def getdataset2(myobj, config, classifier):
    if myobj.dataset == 'mnist':
        return getmnist2(myobj, config)
    if myobj.dataset == 'celeba_gan':
        return getceleba_gan(myobj, config)

def getmnist2(myobj, config):
    """
    ## Loading the MNIST dataset and preprocessing it
    """
    batch_size = 64

    # We'll use all the available examples from both the training and test
    # sets.
    (x_train, y_train), (x_test, y_test) = keras.datasets.mnist.load_data()
    all_digits = np.concatenate([x_train, x_test])
    all_labels = np.concatenate([y_train, y_test])

    # Scale the pixel values to [0, 1] range, add a channel dimension to
    # the images, and one-hot encode the labels.
    all_digits = all_digits.astype("float32") / 255.0
    all_digits = np.reshape(all_digits, (-1, 28, 28, 1))
    all_labels = keras.utils.to_categorical(all_labels, 10)

    # Create tf.data.Dataset.
    dataset = tf.data.Dataset.from_tensor_slices((all_digits, all_labels))
    dataset = dataset.shuffle(buffer_size=1024).batch(batch_size)

    print(f"Shape of training images: {all_digits.shape}")
    print(f"Shape of training labels: {all_labels.shape}")
    mydim = (28, 28, 1)
    return dataset, mydim, 10, True, True

def getceleba_gan(myobj, config):
    # TODO download
    os.makedirs("/tmp/celeba_gan", 0o777, True)

    url = "https://drive.google.com/uc?id=1O7m1010EJjLE5QxLZiM9Fpjs7Oj6e684"
    output = "/tmp/celeba_gan/data.zip"
    gdown.download(url, output, quiet=True)
    with ZipFile("/tmp/celeba_gan/data.zip", "r") as zipobj:
        zipobj.extractall("/tmp/celeba_gan")
    dataset = keras.utils.image_dataset_from_directory(
        "/tmp/celeba_gan", label_mode=None, image_size=(64, 64), batch_size=32
    )
    dataset = dataset.map(lambda x: x / 255.0)
    mydim = (64, 64)
    return dataset, mydim, 10, True, False

def getdataset(myobj, config, classifier):
    if myobj.dataset == 'mnist':
        return getmnist(myobj, config)
    if myobj.dataset == 'cifar10':
        return getcifar10(config)
    if myobj.dataset == 'dailymintemperatures':
        return getdailymintemperatures(myobj)
    if myobj.dataset == 'nasdaq':
        return getnasdaq(myobj, config)
    if myobj.dataset == 'number':
        return getnumber(myobj, config)

def getmnist(myobj, config):
    os.makedirs("/tmp/datasets", 0o777, True)
    #load mnist data
    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()
    if hasattr(myobj, 'normalizevalue'):
        from quantum import mydatasets as q
        q.getmnist(myobj, config)
        return

    #print(tf.keras.backend.image_data_format())
    def create_mnist_dataset(data, labels, batch_size):
        def gen():
            for image, label in zip(data, labels):
                yield image, label
        ds = tf.data.Dataset.from_generator(gen, (tf.float32, tf.int32), ((28,28 ), ()))
        return ds.repeat().batch(batch_size)
    #train and validation dataset with different batch size
    train_dataset = create_mnist_dataset(x_train, y_train, 10)
    valid_dataset = create_mnist_dataset(x_test, y_test, 20)
    #print(type(x_train))
    #print(type(y_train))
    #print(type(train_dataset))
    #print(x_train.shape)
    #print(x_train.shape, x_train.shape[2])
    #print(type(y_train.shape))
    mydimorig = (x_train.shape[1], x_train.shape[2])
    mydim = (x_train.shape[1], x_train.shape[2])
    if not config.name == "cnn" and not config.name == "cnn2":
        if config.name == "rnn" or config.name == "lstm" or config.name == "gru":
            #print("here")
            if config.name == "rnnnot":
                #print(x_train.shape)
                x_train = np.transpose(x_train, [1, 0, 2])
                #print(x_train.shape)
                x_test = np.transpose(x_test, [1, 0, 2])
            #x_train = x_train.reshape(1, x_train.shape[0], x_train.shape[1])
            #x_test = x_test.reshape(1, x_test.shape[0], x_test.shape[1])
        else:
            x_train = x_train.reshape((x_train.shape[0], 784))
            x_test = x_test.reshape((x_test.shape[0], 784))
            mydim = 784
        y_train = np.int32(y_train)
        y_test = np.int32(y_test)
    else:
        if config.name == "cnn2":
            mydim = (28, 28, 1)
            x_train = x_train.reshape(x_train.shape[0], 28, 28, 1)
            x_test = x_test.reshape(x_test.shape[0], 28, 28, 1)
    x_test = np.float16(x_test)        
    #print(x_train.shape)
    #print(y_train.shape)
    return x_train, y_train, x_test, y_test, mydimorig, mydim, 10, True

def getcifar10(config):
    #load mnist data
    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.cifar10.load_data()
    #"/tmp/datasets/cifar.npz")
    def create_cifar10_dataset(data, labels, batch_size):
        def gen():
            for image, label in zip(data, labels):
                yield image, label
        ds = tf.data.Dataset.from_generator(gen, (tf.float32, tf.int32), ((32,32 ), ()))
        return ds.repeat().batch(batch_size)
    #train and validation dataset with different batch size
    train_dataset = create_cifar10_dataset(x_train, y_train, 10)
    valid_dataset = create_cifar10_dataset(x_test, y_test, 20)
    #print(type(x_train))
    #print(type(y_train))
    #print(type(train_dataset))
    #print(x_train.shape)
    #print(x_train.shape, x_train.shape[2])
    #print(type(y_train.shape))
    mydimorig = (x_train.shape[1], x_train.shape[2])
    mydim = (x_train.shape[1], x_train.shape[2])
    if not config.name == "cnn" and not config.name == "cnn2":
        if config.name == "rnn" or config.name == "lstm" or config.name == "gru":
            #print("here")
            if config.name == "rnnnot":
                #print(x_train.shape)
                x_train = np.transpose(x_train, [1, 0, 2])
                #print(x_train.shape)
                x_test = np.transpose(x_test, [1, 0, 2])
            #x_train = x_train.reshape(1, x_train.shape[0], x_train.shape[1])
            #x_test = x_test.reshape(1, x_test.shape[0], x_test.shape[1])
        else:
            x_train = x_train.reshape((x_train.shape[0], 1024 * 3))
            x_test = x_test.reshape((x_test.shape[0], 1024 * 3))
            mydim = 1024 * 3
        y_train = np.int32(y_train)
        y_test = np.int32(y_test)
    else:
        if config.name == "cnn2":
            #print("here21")
            #print(x_train.shape)
            mydim = (32, 32, 3)
            x_train = x_train.reshape(x_train.shape[0], 32, 32, 3)
            x_test = np.float16(x_test.reshape(x_test.shape[0], 32, 32, 3))
            #x_train = x_train.reshape(x_train.shape[0], 1, 32, 32)
            #x_test = x_test.reshape(x_test.shape[0], 1, 32, 32)
            #mydim = (1, 32, 32)
    #print(x_train.shape)
    #print(y_train.shape)
    return x_train, y_train, x_test, y_test, mydimorig, mydim, 10, True

def getdailymintemperatures(myobj):
    url = 'https://raw.githubusercontent.com/jbrownlee/Datasets/master/daily-min-temperatures.csv'
    file_path = tf.keras.utils.get_file("/tmp/daily-min-temperatures.csv", url)
    data = np.genfromtxt(file_path, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'temp'), 'formats': (np.str, np.float)})
    data = data['temp']
    data = [ data ]
    #print(type(data), len(data), data)
    return data, None, data, None, myobj.size, myobj.size, myobj.classes, False
#    return data, None, None, None, myobj.size, myobj.classes
    
def getnasdaq(myobj, config):
    url = 'https://fred.stlouisfed.org/graph/fredgraph.csv?mode=fred&id=NASDAQCOM&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05'
    file_path = tf.keras.utils.get_file("/tmp/", url)
    file_path = "/tmp/fredgraph.csv?mode=fred&id=NASDAQCOM&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05"
    data = np.genfromtxt(file_path, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'nasdaqcom'), 'formats': (np.str, np.float)})
    #print(type(data), data.shape, data)
    data = data['nasdaqcom']
    data = data[np.logical_not(np.isnan(data))]
    #data = data[1:20]

    url = 'https://fred.stlouisfed.org/graph/fredgraph.csv?mode=fred&id=DJIA&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05'
    file_path2 = tf.keras.utils.get_file("/tmp/", url)
    file_path2 = "/tmp/fredgraph.csv?mode=fred&id=DJIA&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05"
    data2 = np.genfromtxt(file_path2, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'djia'), 'formats': (np.str, np.float)})
    #print(type(data2), data2.shape, data2)
    data2 = data2['djia']
    data2 = data2[np.logical_not(np.isnan(data2))]
    #data = data[1:20]

    #print(type(data), len(data), data)
    #print(type(data2), len(data2), data2)

    data3 = 100 * data / data[0]
    data4 = 100 * data2 / data2[0]
    #print(data3[0:10])
    
    data = [ data ]
    data2 = [ data2 ]

    return data, None, data, None, myobj.size, myobj.size, myobj.classes, False

def getnumber(myobj, config):
    data = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40 ]
    data1 = [ 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140 ]

    data = [ data, data1 ]

    return data, None, data, None, myobj.size, myobj.size, myobj.classes, False

vectorize_layer = None

def imdbdir(myobj, config):
    keras.utils.get_file(origin = "https://ai.stanford.edu/~amaas/data/sentiment/aclImdb_v1.tar.gz", extract = True, cache_dir = "/tmp/.keras")
    return [
        "/tmp/.keras/datasets/aclImdb_v1.tar.gz/aclImdb/train/pos",
        "/tmp/.keras/datasets/aclImdb_v1.tar.gz/aclImdb/train/neg",
        "/tmp/.keras/datasets/aclImdb_v1.tar.gz/aclImdb/test/pos",
        "/tmp/.keras/datasets/aclImdb_v1.tar.gz/aclImdb/test/neg",
    ]

def simplebooksdir(myobj, config):
    keras.utils.get_file(
        origin="https://dldata-public.s3.us-east-2.amazonaws.com/simplebooks.zip",
        extract=True,
        cache_dir = "/tmp/.keras"
    )
    dir = os.path.expanduser("/tmp/.keras/datasets/simplebooks.zip/simplebooks/")
    return [
        dir
    ]

class DictToObject:
    def __init__(self, dictionary):
        for key, value in dictionary.items():
            setattr(self, key, value)

class Dummy:
    def __init__(self, v):
        vars(self).update(v)

def do_dir(myobj, config, directories):
    import random
    import tensorflow.data as tf_data
    from keras.layers import TextVectorization
    batch_size = 128
    vocab_size = 20000
    maxlen = 80
    filenames = []
    for dir in directories:
        for f in os.listdir(dir):
            filenames.append(os.path.join(dir, f))

    print(f"{len(filenames)} files")

    # Create a dataset from text files
    random.shuffle(filenames)
    text_ds = tf_data.TextLineDataset(filenames)
    text_ds = text_ds.shuffle(buffer_size=256)
    text_ds = text_ds.batch(batch_size)

    if hasattr(config, 'take'):
        text_ds = text_ds.take(config.take)


    global vectorize_layer
    vectorize_layer = TextVectorization(
        standardize=custom_standardization,
        max_tokens=vocab_size - 1,
        output_mode="int",
        output_sequence_length=maxlen + 1,
    )
    vectorize_layer.adapt(text_ds)
    vocab = vectorize_layer.get_vocabulary()

    text_ds = text_ds.map(prepare_lm_inputs_labels, num_parallel_calls=tf_data.AUTOTUNE)
    text_ds = text_ds.prefetch(tf_data.AUTOTUNE)
    mddict = { 'vocab_size' : vocab_size, 'maxlen' : maxlen, 'vocab' : vocab, 'name' : myobj.dataset }
    #import json
    #s = json.dumps(mddict)
    #md = json.loads(s, object_hook = Dummy)
    #print(type(md), md)
    md = DictToObject(mddict)
    #print(type(md), md)
    return text_ds, None, None, md

def custom_standardization(input_string):
    import string
    import tensorflow.strings as tf_strings
    lowercased = tf_strings.lower(input_string)
    stripped_html = tf_strings.regex_replace(lowercased, "<br />", " ")
    return tf_strings.regex_replace(stripped_html, f"([{string.punctuation}])", r" \1")

def prepare_lm_inputs_labels(text):
    import tensorflow
    text = tensorflow.expand_dims(text, -1)
    tokenized_sentences = vectorize_layer(text)
    x = tokenized_sentences[:, :-1]
    y = tokenized_sentences[:, 1:]
    return x, y

def filenamedir(myobj, config):
    return [ myobj.filename ]

def simplebooks(myobj, config, classifier, dir):
    import tensorflow.data as tf_data
    import tensorflow.strings as tf_strings
    import keras_nlp

    BATCH_SIZE = 64
    MIN_STRING_LEN = 512
    VOCAB_SIZE = 5000
    SEQ_LEN = 128
    raw_train_ds = (
        tf_data.TextLineDataset(dir + "simplebooks-92-raw/train.txt")
        .filter(lambda x: tf_strings.length(x) > MIN_STRING_LEN)
        .batch(BATCH_SIZE)
        .shuffle(buffer_size=256)
    )

    raw_val_ds = (
        tf_data.TextLineDataset(dir + "simplebooks-92-raw/valid.txt")
        .filter(lambda x: tf_strings.length(x) > MIN_STRING_LEN)
        .batch(BATCH_SIZE)
    )

    if hasattr(config, 'take'):
        raw_train_ds = raw_train_ds.take(config.take)
        raw_val_ds = raw_val_ds.take(config.take)

    vocab = keras_nlp.tokenizers.compute_word_piece_vocabulary(
        raw_train_ds,
        vocabulary_size=VOCAB_SIZE,
        lowercase=True,
        reserved_tokens=["[PAD]", "[UNK]", "[BOS]"],
    )

    tokenizer = keras_nlp.tokenizers.WordPieceTokenizer(
        vocabulary=vocab,
        sequence_length=SEQ_LEN,
        lowercase=True,
    )

    start_packer = keras_nlp.layers.StartEndPacker(
        sequence_length=SEQ_LEN,
        start_value=tokenizer.token_to_id("[BOS]"),
    )

    def preprocess(inputs):
        outputs = tokenizer(inputs)
        features = start_packer(outputs)
        labels = outputs
        return features, labels


    train_ds = raw_train_ds.map(preprocess, num_parallel_calls=tf_data.AUTOTUNE).prefetch(
        tf_data.AUTOTUNE
    )
    val_ds = raw_val_ds.map(preprocess, num_parallel_calls=tf_data.AUTOTUNE).prefetch(
        tf_data.AUTOTUNE
    )

    mddict = {'vocab_size': VOCAB_SIZE, 'seq_len': SEQ_LEN, 'vocab': vocab, 'tokenizer' : tokenizer, 'start_packer' : start_packer, 'name': myobj.dataset, 'train_ds' : train_ds}
    md = DictToObject(mddict)

    return train_ds, val_ds, None, md

