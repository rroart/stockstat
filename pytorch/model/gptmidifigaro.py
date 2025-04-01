import torch
import os
import glob
import pytorch_lightning as pl

from model.seq2seq import Seq2SeqModule
from model.vae import VqVaeModule

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

N_CODES = 2048
N_GROUPS = 16
D_MODEL = 512
D_LATENT = 1024

#CHECKPOINT = os.getenv('CHECKPOINT', None)
#VAE_CHECKPOINT = os.getenv('VAE_CHECKPOINT', None)

BATCH_SIZE = 128
TARGET_BATCH_SIZE = 512

EPOCHS = 2
WARMUP_STEPS = 4000
MAX_STEPS = 1e20
MAX_TRAINING_STEPS = 100_000
LEARNING_RATE = 1e-4
LR_SCHEDULE = 'const'
CONTEXT_SIZE = 256

ACCUMULATE_GRADS = max(1, TARGET_BATCH_SIZE//BATCH_SIZE)

N_WORKERS = min(os.cpu_count(), float(os.getenv('N_WORKERS', 'inf')))
if device.type == 'cuda':
  N_WORKERS = min(N_WORKERS, 8*torch.cuda.device_count())
N_WORKERS = int(N_WORKERS)

MAX_CONTEXT = min(1024, CONTEXT_SIZE)

class Model:
    def __init__(self, myobj, config, dataset):
        self.myobj = myobj
        self.config = config
        self.dataset = dataset

        MODEL = 'figaro-expert' #config.model

        VAE_CHECKPOINT = None

        OUTPUT_DIR = "/tmp/data/figaro" + '/' + MODEL # myobj.path

        if MODEL in ['figaro-learned', 'figaro'] and VAE_CHECKPOINT:
            vae_module = VqVaeModule.load_from_checkpoint(checkpoint_path=VAE_CHECKPOINT)
            vae_module.cpu()
            vae_module.freeze()
            vae_module.eval()

        else:
            vae_module = None

        if True:
            seq2seq_kwargs = {
              'encoder_layers': 4,
              'decoder_layers': 6,
              'num_attention_heads': 8,
              'intermediate_size': 2048,
              'd_model': D_MODEL,
              'context_size': MAX_CONTEXT,
              'lr': LEARNING_RATE,
              'warmup_steps': WARMUP_STEPS,
              'max_steps': MAX_STEPS,
            }
            dec_kwargs = { **seq2seq_kwargs }
            dec_kwargs['encoder_layers'] = 0

            # use lambda functions for lazy initialization
            self.model = {
              'vq-vae': lambda: VqVaeModule(
                encoder_layers=4,
                decoder_layers=6,
                encoder_ffn_dim=2048,
                decoder_ffn_dim=2048,
                n_codes=N_CODES,
                n_groups=N_GROUPS,
                context_size=MAX_CONTEXT,
                lr=LEARNING_RATE,
                lr_schedule=LR_SCHEDULE,
                warmup_steps=WARMUP_STEPS,
                max_steps=MAX_STEPS,
                d_model=D_MODEL,
                d_latent=D_LATENT,
              ),
              'figaro-learned': lambda: Seq2SeqModule(
                description_flavor='latent',
                n_codes=vae_module.n_codes,
                n_groups=vae_module.n_groups,
                d_latent=vae_module.d_latent,
                **seq2seq_kwargs
              ),
              'figaro': lambda: Seq2SeqModule(
                description_flavor='both',
                n_codes=vae_module.n_codes,
                n_groups=vae_module.n_groups,
                d_latent=vae_module.d_latent,
                **seq2seq_kwargs
              ),
              'figaro-expert': lambda: Seq2SeqModule(
                description_flavor='description',
                **seq2seq_kwargs
              ),
              'figaro-no-meta': lambda: Seq2SeqModule(
                description_flavor='description',
                description_options={ 'instruments': True, 'chords': True, 'meta': False },
                **seq2seq_kwargs
              ),
              'figaro-no-inst': lambda: Seq2SeqModule(
                description_flavor='description',
                description_options={ 'instruments': False, 'chords': True, 'meta': True },
                **seq2seq_kwargs
              ),
              'figaro-no-chord': lambda: Seq2SeqModule(
                description_flavor='description',
                description_options={ 'instruments': True, 'chords': False, 'meta': True },
                **seq2seq_kwargs
              ),
              'baseline': lambda: Seq2SeqModule(
                description_flavor='none',
                **dec_kwargs
              ),
            }[MODEL]()

        self.datamodule = self.model.get_datamodule(
                    self.dataset.files, #midi_files,
                    vae_module=vae_module,
                    batch_size=BATCH_SIZE,
                    num_workers=N_WORKERS,
                    pin_memory=True
                  )

        checkpoint_callback = pl.callbacks.model_checkpoint.ModelCheckpoint(
            monitor='valid_loss',
            dirpath=os.path.join(OUTPUT_DIR, MODEL),
            filename='{step}-{valid_loss:.2f}',
            save_last=True,
            save_top_k=2,
            every_n_train_steps=1000,
          )

        lr_monitor = pl.callbacks.LearningRateMonitor(logging_interval='step')

        swa_callback = pl.callbacks.StochasticWeightAveraging(swa_lrs=0.05)

        self.trainer = pl.Trainer(
            devices=1 if device.type == 'cpu' else torch.cuda.device_count(),
            accelerator='auto',
            profiler='simple',
            callbacks=[checkpoint_callback, lr_monitor, swa_callback],
            max_epochs=EPOCHS,
            max_steps=MAX_TRAINING_STEPS,
            log_every_n_steps=max(100, min(25*ACCUMULATE_GRADS, 200)),
            val_check_interval=max(500, min(300*ACCUMULATE_GRADS, 1000)),
            limit_val_batches=64,
            accumulate_grad_batches=ACCUMULATE_GRADS,
            gradient_clip_val=1.0,
          )


    def localsave(self):
       return True

    def fit(self):
        import os
        self.trainer.fit(self.model, self.datamodule)

    def save(self):
        #torch.save( { 'model' : trainer }, "/tmp/e.ckpt")
        #torch.save( trainer, "/tmp/e2.ckpt")
        #torch.save( model.state_dict(), "/tmp/e3.ckpt")
        torch.save( { 'state_dict' : self.model.state_dict() }, "/tmp/e4.ckpt")

    def generate(self, filename):
        num_prime = 256
