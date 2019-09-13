import tensorflow as tf

from .myestimator import MyEstimator

class Model(MyEstimator):

  def __init__(self, myobj, config):
    super(Model, self).__init__(config, name='my_model')
    hidden_units = [ config.hiddenneurons ] * config.hiddenlayers
    feature_columns = [ tf.feature_column.numeric_column("features", shape=[myobj.size] ) ]
    pu = '/cpu:0'
    with tf.device(pu):
        self.classifier = tf.estimator.DNNClassifier(feature_columns=feature_columns,
                                                     hidden_units=hidden_units,
                                                     n_classes = myobj.classes)

  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    return self.dense_2(x)
    
