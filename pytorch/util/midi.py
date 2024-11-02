import torch

from util.processor import RANGE_NOTE_ON, RANGE_NOTE_OFF, RANGE_VEL, RANGE_TIME_SHIFT

SEQUENCE_START = 0
TOKEN_END               = RANGE_NOTE_ON + RANGE_NOTE_OFF + RANGE_VEL + RANGE_TIME_SHIFT
TOKEN_PAD               = TOKEN_END + 1
VOCAB_SIZE              = TOKEN_PAD + 1
TORCH_LABEL_TYPE        = torch.long
TORCH_CPU_DEVICE        = torch.device("cpu")
TORCH_FLOAT             = torch.float32

def cpu_device():
    return TORCH_CPU_DEVICE


def process_midi(raw_mid, max_seq, random_seq):
    import random
    x   = torch.full((max_seq, ), TOKEN_PAD, dtype=TORCH_LABEL_TYPE, device=cpu_device())
    tgt = torch.full((max_seq, ), TOKEN_PAD, dtype=TORCH_LABEL_TYPE, device=cpu_device())

    raw_len     = len(raw_mid)
    full_seq    = max_seq + 1 # Performing seq2seq

    if raw_len == 0:
        return x, tgt

    if raw_len < full_seq:
        x[:raw_len]         = raw_mid
        tgt[:raw_len-1]     = raw_mid[1:]
        tgt[raw_len-1]      = TOKEN_END
    else:
        if random_seq:
            end_range = raw_len - full_seq
            start = random.randint(SEQUENCE_START, end_range)

        else:
            start = SEQUENCE_START

        end = start + full_seq

        data = raw_mid[start:end]

        x = data[:max_seq]
        tgt = data[1:full_seq]

    return x, tgt


