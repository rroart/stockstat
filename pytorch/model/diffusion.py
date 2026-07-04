import numpy as np

from tqdm.auto import trange, tqdm
import matplotlib.pyplot as plt

import torch
import torch.nn as nn
import torch.nn.functional as F

import torchvision
from torchvision import transforms

import torch.nn as nn
import torch
import torch.nn.functional as F

from model import layerutils

import diffusionutils

device = 'cpu:0'

# from https://tree.rocks/make-diffusion-model-from-scratch-easy-way-to-implement-quick-diffusion-model-e60d18fd0f2e

class Net(nn.Module):
    def __init__(self, myobj, config, classify, shape):
        super(Net, self).__init__()

        # Defining some parameters
        self.myobj = myobj
        self.config = config

        self.model = Model().to(device)

    def predict(self, x_idx=None):
        x = torch.randn(32, 3, diffusionutils.IMG_SIZE, diffusionutils.IMG_SIZE).to(device)
        with torch.no_grad():
            for i in trange(diffusionutils.timesteps):
                t = i
                x = self.model(x, torch.full([32, 1], t, dtype=torch.float, device=device))

        diffusionutils.show_examples(x.cpu())


    #predict()


    def predict_step(self):
        xs = []
        x = torch.randn(size=(8, 3, diffusionutils.IMG_SIZE, diffusionutils.IMG_SIZE), device=device)

        with torch.no_grad():
            for i in trange(diffusionutils.timesteps):
                t = i
                x = self.model(x, torch.full([8, 1], t, dtype=torch.float, device=device))
                if i % 2 == 0:
                    xs.append(x[0].cpu())
        xs = torch.stack(xs, dim=0)
        xs = torch.clip(xs, -1, 1)
        xs = diffusionutils.cvtImg(xs)

        plt.figure(figsize=(20, 2))
        for i in range(len(xs)):
            plt.subplot(1, len(xs), i + 1)
            plt.imshow(xs[i])
            plt.title(f'{i}')
            plt.axis('off')


    #predict_step()


    def train_one(self, x_img):
        x_ts = diffusionutils.generate_ts(len(x_img))
        x_a, x_b = diffusionutils.forward_noise(x_img, x_ts)

        x_ts = torch.from_numpy(x_ts).view(-1, 1).float().to(device)
        x_a = x_a.float().to(device)
        x_b = x_b.float().to(device)

        y_p = self.model(x_a, x_ts)
        loss = torch.mean(torch.abs(y_p - x_b))
        self.model.opt.zero_grad()
        loss.backward()
        self.model.opt.step()

        return loss.item()


    def train(self, trainloader, R=50):
        bar = trange(R)
        total = len(trainloader)
        for i in bar:
            for j, (x_img, _) in enumerate(trainloader):
                loss = self.train_one(x_img)
                pg = (j / total) * 100
                if j % 5 == 0:
                    bar.set_description(f'loss: {loss:.5f}, p: {pg:.2f}%')


class Block(nn.Module):
    def __init__(self, in_channels=128, size=32):
        super(Block, self).__init__()

        self.conv_param = nn.Conv2d(in_channels=in_channels, out_channels=128, kernel_size=3, padding=1)
        self.conv_out = nn.Conv2d(in_channels=in_channels, out_channels=128, kernel_size=3, padding=1)

        self.dense_ts = nn.Linear(192, 128)

        self.layer_norm = nn.LayerNorm([128, size, size])

    def forward(self, x_img, x_ts):
        x_parameter = F.relu(self.conv_param(x_img))

        time_parameter = F.relu(self.dense_ts(x_ts))
        time_parameter = time_parameter.view(-1, 128, 1, 1)
        x_parameter = x_parameter * time_parameter

        x_out = self.conv_out(x_img)
        x_out = x_out + x_parameter
        x_out = F.relu(self.layer_norm(x_out))

        return x_out


class Model(nn.Module):
    def __init__(self):
        super(Model, self).__init__()

        self.l_ts = nn.Sequential(
            nn.Linear(1, 192),
            nn.LayerNorm([192]),
            nn.ReLU(),
        )

        self.down_x32 = Block(in_channels=3, size=32)
        self.down_x16 = Block(size=16)
        self.down_x8 = Block(size=8)
        self.down_x4 = Block(size=4)

        self.mlp = nn.Sequential(
            nn.Linear(2240, 128),
            nn.LayerNorm([128]),
            nn.ReLU(),

            nn.Linear(128, 32 * 4 * 4),  # make [-1, 32, 4, 4]
            nn.LayerNorm([32 * 4 * 4]),
            nn.ReLU(),
        )

        self.up_x4 = Block(in_channels=32 + 128, size=4)
        self.up_x8 = Block(in_channels=256, size=8)
        self.up_x16 = Block(in_channels=256, size=16)
        self.up_x32 = Block(in_channels=256, size=32)

        self.cnn_output = nn.Conv2d(in_channels=128, out_channels=3, kernel_size=1, padding=0)

        # make optimizer
        self.opt = torch.optim.Adam(self.parameters(), lr=0.0008)


    def forward(self, x, x_ts):
        x_ts = self.l_ts(x_ts)

        # ----- left ( down ) -----
        blocks = [
            self.down_x32,
            self.down_x16,
            self.down_x8,
            self.down_x4,
        ]
        x_left_layers = []
        for i, block in enumerate(blocks):
            x = block(x, x_ts)
            x_left_layers.append(x)
            if i < len(blocks) - 1:
                x = F.max_pool2d(x, 2)

        # ----- MLP -----
        x = x.view(-1, 128 * 4 * 4)
        x = torch.cat([x, x_ts], dim=1)
        x = self.mlp(x)
        x = x.view(-1, 32, 4, 4)

        # ----- right ( up ) -----
        blocks = [
            self.up_x4,
            self.up_x8,
            self.up_x16,
            self.up_x32,
        ]

        for i, block in enumerate(blocks):
            # cat left
            x_left = x_left_layers[len(blocks) - i - 1]
            x = torch.cat([x, x_left], dim=1)

            x = block(x, x_ts)
            if i < len(blocks) - 1:
                x = F.interpolate(x, scale_factor=2, mode='bilinear')

        # ----- output -----
        x = self.cnn_output(x)

        return x




