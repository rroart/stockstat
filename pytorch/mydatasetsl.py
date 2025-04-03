import glob
import os
from figaro.constants import MASK_TOKEN
import torchvision

from datasetsl import MidiDataModule

BATCH_SIZE = int(os.getenv('BATCH_SIZE', 128))

N_WORKERS = min(os.cpu_count(), float(os.getenv('N_WORKERS', 'inf')))

CONTEXT_SIZE = int(os.getenv('CONTEXT_SIZE', 256))
MAX_CONTEXT = min(1024, CONTEXT_SIZE)

def getdatasetmidi(myobj, config, classifier):
    if myobj.dataset == 'lmd_full':
        return getlmdfull(myobj, config)


def getlmdfullNOT(myobj, config):
    import pathlib
    import os
    import json
    import util.processor as midi_processor
    dir = getpath(myobj)
    if not pathlib.Path(dir + "lmd_full").exists():
        url = 'http://hog.ee.columbia.edu/craffel/lmd/lmd_full.tar.gz'
        torchvision.datasets.utils.download_url(url, dir)
        torchvision.datasets.utils.extract_archive(dir + "lmd_full.tar.gz", dir + "lmd_full")
    import glob
    midi_files = glob.glob(os.path.join(dir + "lmd_full", '**/*.mid'), recursive=True)
    if hasattr(config, 'take'):
        midi_files = midi_files[:config.take]
    dsdict = { "files" : midi_files }
    print("dsdict2")
    ds = DictToObject(dsdict)
    return ds


def getlmdfull(myobj, config):
    import pathlib
    dir = getpath(myobj)
    if not pathlib.Path(dir + "lmd_full").exists():
        url = 'http://hog.ee.columbia.edu/craffel/lmd/lmd_full.tar.gz'
        torchvision.datasets.utils.download_url(url, dir)
        torchvision.datasets.utils.extract_archive(dir + "lmd_full.tar.gz", dir + "lmd_full")
    #dir = myobj.dataset
    midi_files = glob.glob(os.path.join(dir, '**/*.mid'), recursive=True)
    description_flavor = None
    if hasattr(config, 'take'):
        midi_files = midi_files[:config.take]
    print("len", dir, len(midi_files))
    if False: #config.type == 'vae':
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
      description_flavor=description_flavor,
      max_bars=max_bars,
      max_positions=max_positions,
      description_options=description_options
      #**kwargs
    )
    dsdict = { "datamodule": datamodule }
    print("dsdict")
    ds = DictToObject(dsdict)
    return ds


def getpath(myobj):
    if hasattr(myobj, 'path') and not myobj.path is None:
        return myobj.path + '/data/'
    return '/tmp/data/'


class DictToObject:
    def __init__(self, dictionary):
        for key, value in dictionary.items():
            setattr(self, key, value)

