import torch
import os
import glob
import pytorch_lightning as pl
from torch.utils.data import DataLoader

from datasetsl import MidiDataset, SeqCollator
from model.seq2seq import Seq2SeqModule
from model.vae import VqVaeModule
from figaro.input_representation import remi2midi

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

N_CODES = 2048
N_GROUPS = 16
D_MODEL = 512
D_LATENT = 1024

#CHECKPOINT = os.getenv('CHECKPOINT', None)
#VAE_CHECKPOINT = os.getenv('VAE_CHECKPOINT', None)

BATCH_SIZE = 128
TARGET_BATCH_SIZE = 512

EPOCHS = 16 # not used
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
    def __init__(self, myobj, config, dataset, vae_module):
        self.myobj = myobj
        self.config = config
        self.dataset = dataset

        MODEL = config.submodel
        print("submodel", MODEL)

        available_models = [
            'vq-vae',
            'figaro-learned',
            'figaro-expert',
            'figaro',
            'figaro-inst',
            'figaro-chord',
            'figaro-meta',
            'figaro-no-inst',
            'figaro-no-chord',
            'figaro-no-meta',
            'baseline',
        ]

        assert MODEL is not None, 'the MODEL needs to be specified'
        assert MODEL in available_models, f'unknown MODEL: {MODEL}'

        VAE_CHECKPOINT = '/tmp/vqvae.ckpt'

        OUTPUT_DIR = "/tmp/data/figaro" + '/' + MODEL # myobj.path

        if MODEL in ['figaro-learned', 'figaro'] and VAE_CHECKPOINT:
            print("Loading vae")
            vae_module = VqVaeModule.load_from_checkpoint(checkpoint_path=VAE_CHECKPOINT)
            vae_module.cpu()
            vae_module.freeze()
            vae_module.eval()

        else:
            vae_module = None

        print("vqvae", vae_module)

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

        #self.datamodule = self.model.get_datamodule(
        #            self.dataset.files, #midi_files,
        #            vae_module=vae_module,
        #            batch_size=BATCH_SIZE,
        #            num_workers=N_WORKERS,
        #            pin_memory=True
        #          )

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
            max_epochs=config.steps,
            max_steps=MAX_TRAINING_STEPS,
            log_every_n_steps=max(100, min(25*ACCUMULATE_GRADS, 200)),
            val_check_interval=max(500, min(300*ACCUMULATE_GRADS, 1000)),
            limit_val_batches=64,
            accumulate_grad_batches=ACCUMULATE_GRADS,
            gradient_clip_val=1.0,
            default_root_dir="/tmp"
        )


    def localsave(self):
       return True

    def fit(self):
        import os
        self.trainer.fit(self.model, self.dataset.datamodule)

    def save(self):
        #torch.save( { 'model' : trainer }, "/tmp/e.ckpt")
        #torch.save( trainer, "/tmp/e2.ckpt")
        #torch.save( model.state_dict(), "/tmp/e3.ckpt")
        torch.save( { 'state_dict' : self.model.state_dict() }, "/tmp/e4.ckpt")

    def generate(self, filename, vae_module=None):
        num_prime = 256
        batch_size=BATCH_SIZE
        verbose=True
        max_iter=16000
        max_bars=32

        datamodule = self.dataset.datamodule
        datamodule.setup("test")
        midi_files = datamodule.test_ds.files
        import random
        random.shuffle(midi_files)
        if hasattr(self.config, 'take'):
            midi_files = midi_files[:self.config.take]
        print("files", len(midi_files))

        description_options = None
        if self.config.submodel in ['figaro-no-inst', 'figaro-no-chord', 'figaro-no-meta']:
            description_options = self.model.description_options

        dataset = MidiDataset(
            midi_files,
            max_len=-1,
            description_flavor=self.model.description_flavor,
            description_options=description_options,
            max_bars=self.model.context_size,
            vae_module=vae_module
        )

        coll = SeqCollator(context_size=-1)
        dl = DataLoader(dataset, batch_size=batch_size, collate_fn=coll)

        make_medleys = False
        if make_medleys:
            dl = medley_iterator(dl,
              n_pieces=args.n_medley_pieces,
              n_bars=args.n_medley_bars,
              description_flavor=model.description_flavor
            )

        with torch.no_grad():
            print("here")
            for batch in dl:
              print("here2")
              reconstruct_sample(self.model, batch,
                output_dir="/tmp",
                max_iter=max_iter,
                max_bars=max_bars,
                verbose=verbose,
              )


@torch.no_grad()
def reconstruct_sample(model, batch,
  initial_context=1,
  output_dir=None,
  max_iter=-1,
  max_bars=-1,
  verbose=0,
):
  batch_size, seq_len = batch['input_ids'].shape[:2]

  batch_ = { key: batch[key][:, :initial_context] for key in ['input_ids', 'bar_ids', 'position_ids'] }
  if model.description_flavor in ['description', 'both']:
    batch_['description'] = batch['description']
    batch_['desc_bar_ids'] = batch['desc_bar_ids']
  if model.description_flavor in ['latent', 'both']:
    batch_['latents'] = batch['latents']

  max_len = seq_len + 1024
  if max_iter > 0:
    max_len = min(max_len, initial_context + max_iter)
  if verbose:
    print(f"Generating sequence ({initial_context} initial / {max_len} max length / {max_bars} max bars / {batch_size} batch size)")
  sample = model.sample(batch_, max_length=max_len, max_bars=max_bars, verbose=verbose//2)

  xs = batch['input_ids'].detach().cpu()
  xs_hat = sample['sequences'].detach().cpu()
  events = [model.vocab.decode(x) for x in xs]
  events_hat = [model.vocab.decode(x) for x in xs_hat]

  pms, pms_hat = [], []
  n_fatal = 0
  for rec, rec_hat in zip(events, events_hat):
    try:
      pm = remi2midi(rec)
      pms.append(pm)
    except Exception as err:
      print("ERROR: Could not convert events to midi:", err)
    try:
      pm_hat = remi2midi(rec_hat)
      pms_hat.append(pm_hat)
    except Exception as err:
      print("ERROR: Could not convert events to midi:", err)
      n_fatal += 1

  if output_dir:
    os.makedirs(os.path.join(output_dir, 'ground_truth'), exist_ok=True)
    for pm, pm_hat, file in zip(pms, pms_hat, batch['files']):
      if verbose:
        print(f"Saving to {output_dir}/{file}")
      pm.write(os.path.join(output_dir, 'ground_truth', file))
      pm_hat.write(os.path.join(output_dir, file))

  return events


