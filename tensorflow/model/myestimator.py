import tensorflow as tf
import shutil

class MyEstimator():
    def __init__(self, config, name):
        self.config = config
        self.classifier = None
  
    def train(self, train, traincat):
        get_train_inputs = tf.estimator.inputs.numpy_input_fn(
            x = { "features": train },
            y = traincat,
            num_epochs=None,
            shuffle=True
        )
        self.classifier.train(input_fn = get_train_inputs, steps = self.config.steps)

    def evaluate(self, test, testcat):
        get_test_inputs = tf.estimator.inputs.numpy_input_fn(
            x = { "features": test },
            y = testcat,
            num_epochs=None,
            shuffle=True
        )
        eval_dict = self.classifier.evaluate(input_fn = get_test_inputs, steps = self.config.steps)
        #print(eval_dict)
        accuracy_score = eval_dict["accuracy"]
        average_loss = eval_dict["average_loss"]
        return average_loss, accuracy_score

    def predict(self, array):
        get_classifier_inputs = tf.estimator.inputs.numpy_input_fn(
            x = { "features": array },
            shuffle=False
        )
        intlist = []
        problist = []
        predictions = self.classifier.predict(input_fn=get_classifier_inputs)
        for prediction in predictions:
            class_id = prediction['class_ids'][0]
            # NOTE changing prediction back again. see other NOTE
            probability = float(prediction['probabilities'][class_id])
            class_id = int(class_id)
            intlist.append(class_id)
            problist.append(probability)
        return intlist, problist

    def tidy(self):
        shutil.rmtree(self.classifier.model_dir)
        del self.classifier
