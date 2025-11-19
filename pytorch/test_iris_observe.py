import importlib

import torch
import torch.nn as nn
import torch.optim as optim
from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split
from torch.utils.data import TensorDataset, DataLoader
from types import SimpleNamespace
from model.modelutils import observer_another_new, observe, observe_new, observe_new2, observe_new3, \
    print_model_parameters
import numpy as np

print("load iris")
# Load Iris
X, y = load_iris(return_X_y=True)
X = torch.tensor(X, dtype=torch.float32)
y = torch.tensor(y, dtype=torch.long)
print(X)
print(y)
# Split
X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.25, random_state=42, stratify=y)

# Dataloaders
train_loader = DataLoader(TensorDataset(X_train, y_train), batch_size=16, shuffle=True)
val_loader = DataLoader(TensorDataset(X_val, y_val), batch_size=16)

# Tiny model
model = nn.Sequential(
    nn.Linear(4, 16),
    nn.ReLU(),
    nn.Linear(16, 3),
    #nn.Softmax(dim=None)  # 3 classes
)

#model = model.MLP()
Model = importlib.import_module('model.mlp')
config = SimpleNamespace(binary=False, regularize=False, normalize=True, activation = 'relu', lastactivation='softmax', hidden=16, layers=1, inputdropout=0, dropout=0, batchnormalize=False, loss='cross_entropy', optimizer='sgd',lr=0.1)
myobj = SimpleNamespace(classes = 3)
classify = True
shape = (16, 4)
model = Model.Net(myobj, config, classify, shape)
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
model.to(device)
print(model)
print_model_parameters(model)

# Loss, optimizer
loss_fn = nn.CrossEntropyLoss()
optimizer = optim.SGD(model.parameters(), lr=0.1)

# Minimal early stopping stub
class EarlyStopStub:
    stop_training = False

early_stopping = EarlyStopStub()

# Config
config = SimpleNamespace(binary=False, regularize=False, normalize=True)
config = SimpleNamespace(binary=False, regularize=False)

# Run a few epochs
#observer_another_new(model, epochs=30, optimizer=optimizer, loss_fn=loss_fn,
observe_new(model, epochs=30, optimizer=optimizer, loss_fn=loss_fn,
        train_loader=train_loader, valid_loader=val_loader,
        batch_size=16, early_stopping=early_stopping, config=config)

def get_accuracy_multiclass(pred_arr,original_arr):
    #print("pred_arr", pred_arr, original_arr)
    if len(pred_arr)!=len(original_arr):
        return False
    pred_arr = pred_arr.numpy()
    original_arr = original_arr.numpy()
    final_pred= []
    # we will get something like this in the pred_arr [32.1680,12.9350,-58.4877]
    # so will be taking the index of that argument which has the highest value here 32.1680 which corresponds to 0th index
    for i in range(len(pred_arr)):
        final_pred.append(np.argmax(pred_arr[i]))
    final_pred = np.array(final_pred)
    count = 0
    #here we are doing a simple comparison between the predicted_arr and the original_arr to get the final accuracy
    for i in range(len(original_arr)):
        if final_pred[i] == original_arr[i]:
            count+=1
    return count/len(final_pred)

model.eval()
predictions_train = []
with torch.no_grad():
    predictions_train = model(X_train)
    #predictions_test = model(X_val)

#print("x_train", X_train)
print("scoreme", get_accuracy_multiclass(predictions_train, y_train))


