import torch
import torch.nn as nn
import torch.optim as optim
from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split
from torch.utils.data import TensorDataset, DataLoader
from types import SimpleNamespace
from model.modelutils import observe_new

# Load Iris
X, y = load_iris(return_X_y=True)
X = torch.tensor(X, dtype=torch.float32)
y = torch.tensor(y, dtype=torch.long)

# Split
X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.25, random_state=42, stratify=y)

# Dataloaders
train_loader = DataLoader(TensorDataset(X_train, y_train), batch_size=16, shuffle=True)
val_loader = DataLoader(TensorDataset(X_val, y_val), batch_size=16)

# Tiny model
model = nn.Sequential(
    nn.Linear(4, 16),
    nn.ReLU(),
    nn.Linear(16, 3)  # 3 classes
)
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
model.to(device)

# Loss, optimizer
loss_fn = nn.CrossEntropyLoss()
optimizer = optim.SGD(model.parameters(), lr=0.1)

# Minimal early stopping stub
class EarlyStopStub:
    stop_training = False

early_stopping = EarlyStopStub()

# Config
config = SimpleNamespace(binary=False, regularize=False)

# Run a few epochs
observe_new(model, epochs=30, optimizer=optimizer, loss_fn=loss_fn,
        train_loader=train_loader, valid_loader=val_loader,
        batch_size=16, early_stopping=early_stopping, config=config)
