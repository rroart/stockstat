from torch.utils.data import Dataset
import numpy as np

class CustomDataset(Dataset):
    def __init__(self, x, y=None, transform=None):
        self.x = x
        self.y = y
        #self.transform = transform

    def __len__(self):
        return len(self.x)

    def __getitem__(self, index):
        #image = self.x.iloc[index,].values.astype(np.uint8).reshape((28, 28, 1))

        #if self.transform is not None:
        #    image = self.transform(image)

        if self.y is not None:
            return self.x[index], self.y[index]
        else:
            return self.x[index]

