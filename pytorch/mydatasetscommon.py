import numpy as np

def iriscommon(binary):
    file_path_train = "/tmp/iris_training.csv"
    file_path_test = "/tmp/iris_test.csv"
    iris_data_train = np.genfromtxt(file_path_train, delimiter=',', skip_header=1)
    iris_data_test = np.genfromtxt(file_path_test, delimiter=',', skip_header=1)
    print(iris_data_test)
    classes = 3
    if binary:
        rows = np.where( iris_data_train[:,4] < 2 )
        iris_data_train = iris_data_train[rows]
        rows = np.where( iris_data_test[:,4] < 2 )
        iris_data_test = iris_data_test[rows]
        classes = 2
    print(iris_data_test)
    train_x = iris_data_train[:,:-1].tolist()
    train_y = iris_data_train[:,-1].tolist()
    test_x = iris_data_test[:,:-1].tolist()
    test_y = iris_data_test[:,-1].tolist()
    print(test_x, test_y)
    return train_x, train_y, test_x, test_y, classes
