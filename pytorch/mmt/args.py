import pathlib

class Args:
    def __init__(self, name, dir):
        self.dataset = name
        self.in_dir = pathlib.Path(f"{dir}{self.dataset}/processed/notes/")
        self.out_dir = pathlib.Path(f"{dir}exp/test_{self.dataset}")
        self.train_names = pathlib.Path(f"{dir}{self.dataset}/processed/train-names.txt")
        self.valid_names = pathlib.Path(f"{dir}{self.dataset}/processed/valid-names.txt")
        self.batch_size = 8
        self.use_csv = True
        self.aug = True
        self.max_seq_len = 1024
        self.max_beat = 256
        self.dim = 512
        self.layers = 6
        self.heads = 8
        self.dropout = 0.2
        self.abs_pos_emb = True
        self.rel_pos_emb = False
        self.steps = 1
        self.valid_steps = 1
        self.early_stopping = True
        self.early_stopping_tolerance = 20
        self.learning_rate = 0.0005
        self.lr_warmup_steps = 5000
        self.lr_decay_steps = 100000
        self.lr_decay_multiplier = 0.1
        self.grad_norm_clip = 1.0
        self.jobs = 4
        self.gpu = None
        self.n_samples = 50
        self.shuffle = True
        self.model_steps = None