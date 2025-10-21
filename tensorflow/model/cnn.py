import tensorflow as tf
from tensorflow.keras.layers import Dense, Activation, Dropout, Conv1D, MaxPooling1D, Flatten, Convolution1D, BatchNormalization, LeakyReLU, Dropout
from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers import Adam
import model.layerutils as layerutils

from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config, classify, shape):
    super(Model, self).__init__(config, classify, name='my_model')

    optimizer = layerutils.getOptimizer(config)
    regularizer = layerutils.getRegularizer(config)
    activation = tf.keras.layers.Activation(config.activation)
    lastactivation = tf.keras.layers.Activation(config.lastactivation)

    # Define your layers here.

    # https://machinelearningmastery.com/how-to-develop-convolutional-neural-network-models-for-time-series-forecasting/
    # define model
    modelj = Sequential()
    #, input_shape=(n_steps, n_features)
    modelj.add(Conv1D(filters=64, kernel_size=2, activation='relu', input_shape = (28, 28)))
    modelj.add(MaxPooling1D(pool_size=2))
    modelj.add(Flatten())
    modelj.add(Dense(50, activation='relu'))
    modelj.add(Dense(1))

    #https://medium.com/@alexrachnog/neural-networks-for-algorithmic-trading-2-1-multivariate-time-series-ab016ce70f57
    modelm = Sequential()
    # input_shape = (WINDOW, EMB_SIZE),
    modelm.add(tf.keras.Input(shape = shape[1:]))
    if classify and config.normalize:
        modelm.add(layerutils.getNormalLayer(shape))
    for i in range(config.convlayers):
        modelm.add(Convolution1D(
                        filters=16,
                        kernel_size = config.kernelsize,
                        strides = config.stride,
                        kernel_regularizer=regularizer,
                        padding='same'))
        if config.batchnormalize:
            modelm.add(BatchNormalization())
        modelm.add(activation)
        modelm.add(Dropout(config.dropout))
    modelm.add(Flatten())
    for i in range(config.layers):
        modelm.add(Dense(config.hidden, kernel_regularizer=regularizer))
        if config.batchnormalize:
            modelm.add(BatchNormalization())
        modelm.add(activation)
    modelm.add(Dense(myobj.classes, kernel_regularizer=regularizer))
    modelm.add(lastactivation)
    
    #https://medium.com/@alexrachnog/neural-networks-for-algorithmic-trading-part-one-simple-time-series-forecasting-f992daa1045a
    model1 = Sequential()
    # input_shape = (TRAIN_SIZE, EMB_SIZE), 
    #model1.add(Convolution1D(input_shape = (28, 28),
    #                    nb_filter=64,
    #                    filter_length=2,
    #                    border_mode='valid',
    #                    activation='relu',
    #                    subsample_length=1))
    #model1.add(MaxPooling1D(pool_length=2))

    # input_shape = (TRAIN_SIZE, EMB_SIZE), 
    #model1.add(Convolution1D(input_shape = (28, 28),
    #                    nb_filter=64,
    #                    filter_length=2,
    #                    border_mode='valid',
    #                    activation='relu',
    #                    subsample_length=1))
    #model1.add(MaxPooling1D(pool_length=2))

    #model1.add(Dropout(0.25))
    #model1.add(Flatten())

    #model1.add(Dense(250))
    #model1.add(Dropout(0.25))
    #model1.add(Activation('relu'))
    
    #model1.add(Dense(1))
    #model1.add(Activation('linear'))
    #end
    
    self.model = modelm
    self.dense_1 = Dense(32, activation='relu')
    self.dense_2 = Dense(32, activation='relu')
    self.dense_3 = Dense(32, activation='relu')
    #self.dense_4 = Dense(myobj.classes, activation='sigmoid')
    self.dense_4 = Dense(myobj.classes, activation='softmax')

    self.model.compile(optimizer=optimizer,
                       loss=config.loss,
                       metrics=['accuracy'])
    return
    # https://www.kaggle.com/heyhello/mnist-simple-convnet/data
    model = models.Sequential()
    model.add(layers.Conv2D(32, (3, 3), activation='relu', input_shape=(28, 28, 1)))
    model.add(layers.MaxPooling2D((2, 2)))
    model.add(layers.Conv2D(64, (3, 3), activation='relu'))
    model.add(layers.MaxPooling2D((2, 2)))
    model.add(layers.Conv2D(64, (3, 3), activation='relu'))
    model.add(layers.Flatten())
    model.add(layers.Dense(64, activation='relu'))
    model.add(layers.Dense(10, activation='softmax'))


  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    x = self.dense_2(x)
    x = self.dense_3(x)
    #print(herexxx)
    return self.dense_4(x)
