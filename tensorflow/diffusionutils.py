import numpy as np
import matplotlib.pyplot as plt

IMG_SIZE = 32  # input image size, CIFAR-10 is 32x32
BATCH_SIZE = 128  # for training batch size
timesteps = 16  # how many steps for a noisy image into clear
time_bar = 1 - np.linspace(0, 1.0, timesteps + 1)  # linspace for timesteps

def cvtImg(img):
    img = img - img.min()
    img = (img / img.max())
    return img.astype(np.float32)


def show_examples(x):
    plt.figure(figsize=(10, 10))
    for i in range(25):
        plt.subplot(5, 5, i + 1)
        img = cvtImg(x[i])
        plt.imshow(img)
        plt.axis('off')


def forward_noise(x, t):
    a = time_bar[t]  # base on t
    b = time_bar[t + 1]  # image for t + 1

    noise = np.random.normal(size=x.shape)  # noise mask
    a = a.reshape((-1, 1, 1, 1))
    b = b.reshape((-1, 1, 1, 1))
    img_a = x * (1 - a) + noise * a
    img_b = x * (1 - b) + noise * b
    return img_a, img_b


def generate_ts(num):
    return np.random.randint(0, timesteps, size=num)

def show_examples2(X_train):
    # t = np.full((25,), timesteps - 1) # if you want see clarity
    # t = np.full((25,), 0)             # if you want see noisy
    t = generate_ts(25)  # random for training data
    a, b = forward_noise(X_train[:25], t)
    show_examples(a)