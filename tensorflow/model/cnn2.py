import tensorflow as tf
from tensorflow.keras.layers import Dense, Activation, Dropout, Conv2D, MaxPooling2D, Flatten, Convolution2D, BatchNormalization, LeakyReLU, Dropout
from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers import Adam

from .model import MyModel

class Model(MyModel):

  def __init__(self, myobj, config, classify):
    super(Model, self).__init__(config, classify, name='my_model')

    if classify:
      loss = 'sparse_categorical_crossentropy'
    else:
      loss = 'mean_squared_error'

    # Define your layers here.

    #https://medium.com/@alexrachnog/neural-networks-for-algorithmic-trading-2-1-multivariate-time-series-ab016ce70f57
    modelm = Sequential()
    #print("inputshape");
    #print(myobj.size);
    # input_shape = (WINDOW, EMB_SIZE),
    modelm.add(Convolution2D(input_shape = myobj.size,
                        filters=32,
                        kernel_size = config.kernelsize,
                        strides = config.stride,
                        padding='same'))
    modelm.add(BatchNormalization())
    modelm.add(LeakyReLU())
    #modelm.add(Dropout(config.dropout))
    modelm.add(Convolution2D(filters=64,
                        kernel_size = config.kernelsize,
                        strides = config.stride,
                        padding='same'))
    modelm.add(BatchNormalization())
    modelm.add(LeakyReLU())
    modelm.add(MaxPooling2D(2))
    #modelm.add(MaxPooling2D((4, 4)))
    modelm.add(Dropout(config.dropout1))
    modelm.add(Flatten())
    modelm.add(Dense(128))
    modelm.add(BatchNormalization())
    modelm.add(LeakyReLU())
    modelm.add(Dropout(config.dropout2))
    modelm.add(Dense(64))
    modelm.add(LeakyReLU())
    modelm.add(Dense(myobj.classes))
    modelm.add(Activation('softmax'))
    modelm.summary()
    
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
    self.dense_1 = Dense(32, activation='relu', input_shape=(myobj.size,))
    self.dense_2 = Dense(32, activation='relu')
    self.dense_3 = Dense(32, activation='relu')
    #self.dense_4 = Dense(myobj.classes, activation='sigmoid')
    self.dense_4 = Dense(myobj.classes, activation='softmax')
    #adam = tf.keras.optimizers.Adam(learning_rate=1)
    self.model.compile(optimizer='adadelta',
                       loss=loss,
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
