import torch

from typing import Dict


class _Metric(torch.nn.Module):
    def __init__(self):
        super().__init__()

    def forward(self, input: torch.Tensor, target: torch.Tensor):
        raise NotImplementedError()


class Accuracy(_Metric):
    def __init__(self):
        super().__init__()

    def forward(self, input: torch.Tensor, target: torch.Tensor):
        bool_acc = input.long() == target.long()
        return bool_acc.sum().to(torch.float) / bool_acc.numel()


class MockAccuracy(Accuracy):
    def __init__(self):
        super().__init__()

    def forward(self, input: torch.Tensor, target: torch.Tensor):
        return super().forward(input, target)


class CategoricalAccuracy(Accuracy):
    def __init__(self):
        super().__init__()

    def forward(self, input: torch.Tensor, target: torch.Tensor):
        input = input.softmax(-1)
        categorical_input = input.argmax(-1)
        return super().forward(categorical_input, target)


class LogitsBucketting(_Metric):
    def __init__(self, vocab_size):
        super().__init__()

    def forward(self, input: torch.Tensor, target: torch.Tensor):
        return input.argmax(-1).flatten().to(torch.int32)


class MetricsSet(object):
    def __init__(self, metric_dict: Dict):
        super().__init__()
        self.metrics = metric_dict

    def __call__(self, input: torch.Tensor, target: torch.Tensor):
        return self.forward(input=input, target=target)

    def forward(self, input: torch.Tensor, target: torch.Tensor):
        return {
            k: metric(input.to(target.device), target)
            for k, metric in self.metrics.items()}
