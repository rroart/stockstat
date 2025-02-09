import glob
import os
from figaro.constants import MASK_TOKEN

from datasetsl import MidiDataModule

BATCH_SIZE = int(os.getenv('BATCH_SIZE', 128))

N_WORKERS = min(os.cpu_count(), float(os.getenv('N_WORKERS', 'inf')))

CONTEXT_SIZE = int(os.getenv('CONTEXT_SIZE', 256))
MAX_CONTEXT = min(1024, CONTEXT_SIZE)

def getdatasetmidi(myobj, config, classifier):
    if myobj.dataset == 'figaro':
        return getfigaro(myobj, config)


def getfigaro(myobj, config):
    # todo download
    dir = myobj.dataset
    midi_files = glob.glob(os.path.join(dir, '**/*.mid'), recursive=True)
    if hasattr(config, 'take'):
        midi_files = midi_files[:config.take]
    if config.type == 'vae':
        context_size=MAX_CONTEXT
        datamodule = MidiDataModule(
            midi_files,
            context_size,
            max_bars_per_context=1,
            bar_token_mask=MASK_TOKEN
            #**kwargs
        )
    else:
        context_size=256
        max_bars = context_size
        max_positions = 512
        description_options=None
        datamodule = MidiDataModule(
      midi_files,
      context_size,
      description_flavor=config.description_flavor,
      max_bars=max_bars,
      max_positions=max_positions,
      description_options=description_options
      #**kwargs
    )

    return datamodule
