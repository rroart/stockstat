import tensorflow as tf

from .myestimator import MyEstimator

class Model(MyEstimator):

  def __init__(self, myobj, config, classify):
    super(Model, self).__init__(myobj, config, classify)
    feature_columns = [ tf.feature_column.numeric_column("features", shape=[myobj.size] ) ]
    pu = '/cpu:0'
    with tf.device(pu):
        self.classifier = tf.estimator.LinearClassifier(
            feature_columns = feature_columns,
            model_dir = self.getModelDir(),
            n_classes = myobj.classes, loss_reduction=tf.keras.losses.Reduction.SUM)

  def call(self, inputs):
    # Define your forward pass here,
    # using layers you previously defined (in `__init__`).
    x = self.dense_1(inputs)
    return self.dense_2(x)
    
