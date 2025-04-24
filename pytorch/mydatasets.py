import torch
from torch.utils.data import DataLoader, Dataset
import torchvision

import numpy as np

from util.midi import process_midi, TORCH_LABEL_TYPE, cpu_device


def getdatasetmidi(myobj, config, classifier):
    if myobj.dataset == 'maestro':
        return getmaestro(myobj, config)
    if myobj.dataset == 'lmd_full':
        return getlmdfull(myobj, config)
    if myobj.dataset == 'lmd':
        return getlmd(myobj, config)
    if myobj.dataset == 'sod':
        return getsod(myobj, config)
    if myobj.dataset == 'snd':
        return getsnd(myobj, config)
    if myobj.dataset == 'lmd_full2':
        return getlmdfull2(myobj, config)
    return do_dir(myobj, config)

def getdataset(myobj, config, classifier):
    if myobj.dataset == 'mnist':
        return getmnist(myobj, config)
    if myobj.dataset == 'cifar10':
        return getcifar10(myobj, config)
    if myobj.dataset == 'dailymintemperatures':
        return getdailymintemperatures(myobj, config)
    if myobj.dataset == 'nasdaq':
        return getnasdaq(myobj, config)
    if myobj.dataset == 'number':
        return getnumber(myobj, config)
    
def getdatasetdl(myobj, config, classifier):
    if myobj.dataset == 'mnist':
        return getmnistdl(myobj, config)
    if myobj.dataset == 'cifar10':
        return getcifar10(myobj, config)
    if myobj.dataset == 'dailymintemperatures':
        return getdailymintemperatures(myobj, config)
    if myobj.dataset == 'nasdaq':
        return getnasdaq(myobj, config)
    if myobj.dataset == 'number':
        return getnumber(myobj, config)
    
def getmnistdl(myobj, config):
    dir = getpath(myobj)
    transform = torchvision.transforms.Compose([torchvision.transforms.ToTensor()])
    ds = torchvision.datasets.MNIST(dir + 'datasets/mnist', train=True, download=True, transform = transform)
    # size, classes, dl
    return 784, 1, DataLoader(dataset=ds,
                      shuffle=True,
                      batch_size=64)

def getmnist(myobj, config):
    dir = getpath(myobj)
    dl = DataLoader(torchvision.datasets.MNIST(dir + 'datasets/mnist', train=True, download=True))

    tensor = dl.dataset.data
    tensor = tensor.to(dtype=torch.float32)
    #print("tt", tensor.shape)
    tr = tensor
    #.reshape(tensor.size(0), -1) 
    #print("tt", tr.shape)
    tr = tr/128
    targets = dl.dataset.targets
    targets = targets.to(dtype=torch.float)

    x_train = tr[0:50000]
    y_train = targets[0:50000]
    x_valid = tr[50000:60000]
    y_valid = targets[50000:60000]

    y_valid = y_valid.to(dtype=torch.long)
    
    #bs=64

    #train_ds = TensorDataset(x_train, y_train)
    #train_dl = DataLoader(train_ds, batch_size=bs, drop_last=False, shuffle=True)

    #valid_ds = TensorDataset(x_valid, y_valid)
    #valid_dl = DataLoader(valid_ds, batch_size=bs * 2)

    #loaders={}
    #loaders['train'] = train_dl
    #loaders['valid'] = valid_dl
    #print("X", x_train.shape)
    mydim = (x_train.shape[1], x_train.shape[2])
    if not config.name == "cnn" and not config.name == "cnn2":
      if config.name == "rnn" or config.name == "lstm" or config.name == "gru":
        #x_train = x_train.view(-1, 1, 784)
        #x_valid = x_valid.view(-1, 1, 784)
        #y_train = y_train.view(1, y_train.size()[0])
        #y_valid = y_valid.view(1, y_valid.size()[0])
        #x_train = x_train.view(-1, 28, 28)
        #x_valid = x_valid.view(-1, 28, 28)
        mydim = 28
        #print("here")
      else:
        x_train = x_train.reshape(x_train.size(0), -1)
        x_valid = x_valid.reshape(x_valid.size(0), -1)
        mydim = 784
    else:
      #mydim = 28
      mydim = mydim
      if config.name == "cnn2":
          #print("here21")
          mydim = (28, 28, 1)
          #x_train = x_train.reshape(x_train.shape[0], 28, 28, 1)
          #x_valid = x_valid.reshape(x_valid.shape[0], 28, 28, 1)
          x_train = x_train.reshape(x_train.shape[0], 1, 28, 28)
          x_valid = x_valid.reshape(x_valid.shape[0], 1, 28, 28)
          mydim = (1, 28, 28)
          #x_train = x_train.reshape(x_train.shape[0], 1, 7, 112)
          #x_valid = x_valid.reshape(x_valid.shape[0], 1, 7, 112)
          #mydim = (1, 7, 112)
          #x_train = x_train.reshape(x_train.shape[0], 1, 112, 7)
          #x_valid = x_valid.reshape(x_valid.shape[0], 1, 112, 7)
          #mydim = (1, 112, 7)
    #print(type(x_train), x_train.shape)
    #print(type(y_train), y_train.shape)
    #print("mydim", mydim)
    return x_train, y_train, x_valid, y_valid, mydim, 10, True

def getcifar10(myobj, config):
    dir = getpath(myobj)
    dl = DataLoader(torchvision.datasets.CIFAR10(dir + 'datasets/cifar10', train=True, download=True))

    tensor = dl.dataset.data
    #print(tensor.shape)
    tensor = torch.FloatTensor(tensor)
    tensor = tensor.to(dtype=torch.float32)
    #print("tt", tensor.shape)
    tr = tensor
    #.reshape(tensor.size(0), -1) 
    #print("tt", tr.shape)
    tr = tr/128
    targets = dl.dataset.targets
    targets = torch.FloatTensor(targets)
    targets = targets.to(dtype=torch.float)

    x_train = tr[0:40000]
    y_train = targets[0:40000]
    x_valid = tr[40000:50000]
    y_valid = targets[40000:50000]

    y_valid = y_valid.to(dtype=torch.long)
    
    #bs=64

    #train_ds = TensorDataset(x_train, y_train)
    #train_dl = DataLoader(train_ds, batch_size=bs, drop_last=False, shuffle=True)

    #valid_ds = TensorDataset(x_valid, y_valid)
    #valid_dl = DataLoader(valid_ds, batch_size=bs * 2)

    #loaders={}
    #loaders['train'] = train_dl
    #loaders['valid'] = valid_dl
    #print("X", x_train.shape)
    mydim = (x_train.shape[1], x_train.shape[2])
    if not config.name == "cnn" and not config.name == "cnn2":
      if config.name == "rnn" or config.name == "lstm" or config.name == "gru":
        #x_train = x_train.view(-1, 1, 1024)
        #x_valid = x_valid.view(-1, 1, 1024)
        #y_train = y_train.view(1, y_train.size()[0])
        #y_valid = y_valid.view(1, y_valid.size()[0])
        #x_train = x_train.view(-1, 32, 32)
        #x_valid = x_valid.view(-1, 32, 32)
        mydim = 32
        #print("here")
      else:
        #print(x_train.shape)
        x_train = x_train.reshape(x_train.size(0), 3072)
        x_valid = x_valid.reshape(x_valid.size(0), 3072)
        mydim = 3072
    else:
      #mydim = 32
      mydim = mydim
      if config.name == "cnn2":
          #print("here21")
          mydim = (32, 32, 1)
          #x_train = x_train.reshape(x_train.shape[0], 28, 28, 1)
          #x_valid = x_valid.reshape(x_valid.shape[0], 28, 28, 1)
          x_train = x_train.reshape(x_train.shape[0], 3, 32, 32)
          x_valid = x_valid.reshape(x_valid.shape[0], 3, 32, 32)
          mydim = (3, 32, 32)
          #x_train = x_train.reshape(x_train.shape[0], 3, 128, 8)
          #x_valid = x_valid.reshape(x_valid.shape[0], 3, 128, 8)
          #mydim = (3, 128, 8)
    #print(type(x_train), x_train.shape)
    #print(type(y_train), y_train.shape)
    #print(type(x_valid), x_valid.shape)
    #print("mydim", mydim)
    return x_train, y_train, x_valid, y_valid, mydim, 10, True

def getdailymintemperatures(myobj, config):
    dir = getpath(myobj)
    url = 'https://raw.githubusercontent.com/jbrownlee/Datasets/master/daily-min-temperatures.csv'
    torchvision.datasets.utils.download_url(url, dir)
    file_path = dir + "daily-min-temperatures.csv"
    data = np.genfromtxt(file_path, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'temp'), 'formats': (np.str, np.float)})
    #print(type(data), data.shape, data)
    data = data['temp']
    #data = data[1:20]
    data = [ data ]
    #print(type(data), len(data), data)

    return data, None, data, None, myobj.size, myobj.classes, False

def getnasdaq(myobj, config):
    dir = getpath(myobj)
    url = 'https://fred.stlouisfed.org/graph/fredgraph.csv?mode=fred&id=NASDAQCOM&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05'
    torchvision.datasets.utils.download_url(url, dir)
    file_path = dir + "fredgraph.csv?mode=fred&id=NASDAQCOM&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05"
    data = np.genfromtxt(file_path, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'nasdaqcom'), 'formats': (np.str, np.float)})
    #print(type(data), data.shape, data)
    data = data['nasdaqcom']
    data = data[np.logical_not(np.isnan(data))]
    #data = data[1:20]

    url = 'https://fred.stlouisfed.org/graph/fredgraph.csv?mode=fred&id=DJIA&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05'
    torchvision.datasets.utils.download_url(url, dir)
    file_path2 = dir + "fredgraph.csv?mode=fred&id=DJIA&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05"
    data2 = np.genfromtxt(file_path2, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'djia'), 'formats': (np.str, np.float)})
    #print(type(data2), data2.shape, data2)
    data2 = data2['djia']
    data2 = data2[np.logical_not(np.isnan(data2))]
    #data = data[1:20]

    #print(type(data), len(data), data)
    #print(type(data2), len(data2), data2)

    data3 = 100 * data / data[0]
    data4 = 100 * data2 / data2[0]
    #print(data3[0:10])
    
    data = [ data ]
    data2 = [ data2 ]

    return data, None, data, None, myobj.size, myobj.classes, False

def getnumber(myobj, config):
    data = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40 ]
    data1 = [ 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140 ]

    data = [ data, data1 ]

    return data, None, data, None, myobj.size, myobj.classes, False

class DictToObject:
    def __init__(self, dictionary):
        for key, value in dictionary.items():
            setattr(self, key, value)

class Dummy:
    def __init__(self, v):
        vars(self).update(v)


def getlmdfull2(myobj, config):
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
    ds = DictToObject(dsdict)
    return ds


def getsod(myobj, config):
    import pathlib
    import glob
    import os
    import mmt.convert_sod
    import mmt.extract
    import mmt.split
    import logging
    import mmt.dataset
    dir = getpath(myobj)
    if not pathlib.Path(dir + "sod").exists():
        url = 'https://qsdfo.github.io/LOP/database/SOD.zip'
        torchvision.datasets.utils.download_url(url, dir)
        torchvision.datasets.utils.extract_archive(dir + "SOD.zip", dir + "sod")
        midi_files = glob.glob(os.path.join(dir + "sod", '**/*.mid'), recursive=True)
        xml_files = glob.glob(os.path.join(dir + "sod", '**/*.xml'), recursive=True)
        midi_files.append(xml_files)
        offset = 1 + len(dir) + len("sod/SOD")
        for i in range(len(midi_files)):
            midi_files[i] = midi_files[i][offset:]
        if hasattr(config, 'take'):
            midi_files = midi_files[:config.take]
        pathlib.Path(dir + "sod").mkdir(exist_ok = True)
        with open(dir + 'sod/original-names.txt', 'w') as f:
            for line in midi_files:
                    f.write(f"{line}\n")
        mmt.convert_sod.main(dir, None)
        mmt.extract.main(dir, ["-d", "sod"])
        mmt.split.main(dir, ["-d", "sod"])
    return getmmtcommon(myobj, config)


def getlmdfull(myobj, config):
    import pathlib
    import glob
    import os
    import mmt.convert_sod
    import mmt.extract
    import mmt.split
    import logging
    import mmt.dataset
    import mmt.convert_lmd_full
    dir = getpath(myobj)
    if not pathlib.Path(dir + "lmd_full").exists():
        url = 'http://hog.ee.columbia.edu/craffel/lmd/lmd_full.tar.gz'
        torchvision.datasets.utils.download_url(url, dir)
        torchvision.datasets.utils.extract_archive(dir + "lmd_full.tar.gz", dir + "lmd_full")
        midi_files = glob.glob(os.path.join(dir + "lmd_full", '**/*.mid'), recursive=True)
        xml_files = glob.glob(os.path.join(dir + "lmd_full", '**/*.xml'), recursive=True)
        midi_files.append(xml_files)
        print("mi", midi_files[0])
        offset = 1 + len(dir) + len("lmd_full/lmd_full") + 2
        for i in range(len(midi_files)):
            midi_files[i] = midi_files[i][offset:]
        if hasattr(config, 'take'):
            midi_files = midi_files[:config.take]
        pathlib.Path(dir + "lmd_full").mkdir(exist_ok = True)
        with open(dir + 'lmd_full/original-names.txt', 'w') as f:
            for line in midi_files:
                    f.write(f"{line}\n")
        print("dir", dir)
        mmt.convert_lmd_full.main(dir, [ "-e"])
        mmt.extract.main(dir, ["-d", "lmd_full"])
        mmt.split.main(dir, ["-d", "lmd_full"])
    return getmmtcommon(myobj, config)


def getsnd(myobj, config):
    import pathlib
    import glob
    import os
    import mmt.convert_sod
    import mmt.extract
    import mmt.split
    import logging
    import mmt.dataset
    import gdown
    import mmt.convert_snd
    from zipfile import ZipFile
    dir = getpath(myobj)
    if not pathlib.Path(dir + "snd").exists():
        pathlib.Path(dir + "snd").mkdir(exist_ok = True, parents = True)
        url = "https://drive.google.com/u/0/uc?id=1j9Pvtzaq8k_QIPs8e2ikvCR-BusPluTb"
        output = dir + "snd.tar"
        gdown.download(url, output, quiet=True)
        torchvision.datasets.utils.extract_archive(dir + "snd.tar", dir + "snd")
        midi_files = glob.glob(os.path.join(dir + "snd", '**/*.mid'), recursive=True)
        xml_files = glob.glob(os.path.join(dir + "snd", '**/*.xml'), recursive=True)
        midi_files.append(xml_files)
        offset = 1 + len(dir) + len("snd/SymphonyNet_Dataset")
        for i in range(len(midi_files)):
            midi_files[i] = midi_files[i][offset:]
        if hasattr(config, 'take'):
            midi_files = midi_files[:config.take]
        with open(dir + 'snd/original-names.txt', 'w') as f:
            for line in midi_files:
                    f.write(f"{line}\n")
        mmt.convert_snd.main(dir, None)
        mmt.extract.main(dir, ["-d", "snd"])
        mmt.split.main(dir, ["-d", "snd"])
    return getmmtcommon(myobj, config)


def getmmtcommon(myobj, config):
    import logging
    import mmt.args
    import mmt.dataset
    import mmt.representation
    # Create the dataset and data loader
    logging.info(f"Creating the data loader...")
    dir = getpath(myobj)
    args = mmt.args.Args(myobj.dataset, dir, config)
    # Load the encoding
    encoding = mmt.representation.load_encoding(args.in_dir / "encoding.json")

    train_dataset = mmt.dataset.MusicDataset(
        args.train_names,
        args.in_dir,
        encoding,
        max_seq_len=args.max_seq_len,
        max_beat=args.max_beat,
        use_augmentation=args.aug,
        use_csv=args.use_csv,
    )
    train_loader = torch.utils.data.DataLoader(
        train_dataset,
        args.batch_size,
        shuffle=True,
        num_workers=args.jobs,
        collate_fn=mmt.dataset.MusicDataset.collate,
    )
    valid_dataset = mmt.dataset.MusicDataset(
        args.valid_names,
        args.in_dir,
        encoding,
        max_seq_len=args.max_seq_len,
        max_beat=args.max_beat,
        use_csv=args.use_csv,
    )
    valid_loader = torch.utils.data.DataLoader(
        valid_dataset,
        args.batch_size,
        num_workers=args.jobs,
        collate_fn=mmt.dataset.MusicDataset.collate,
    )
    dsdict = { "train_loader" : train_loader, "val_loader" : valid_loader, "test_loader" :None }
    ds = DictToObject(dsdict)
    return ds

def getmaestro(myobj, config):
    import pathlib
    import os
    import json
    import util.processor as midi_processor
    dir = getpath(myobj)
    if not pathlib.Path(dir + "maestro").exists():
        url = 'https://storage.googleapis.com/magentadata/datasets/maestro/v2.0.0/maestro-v2.0.0-midi.zip'
        torchvision.datasets.utils.download_url(url, dir)
        torchvision.datasets.utils.extract_archive(dir + "maestro-v2.0.0-midi.zip", dir + "maestro")
    maestro_root = dir + "maestro/maestro-v2.0.0"
    JSON_FILE = "maestro-v2.0.0.json"
    maestro_json_file = os.path.join(maestro_root, JSON_FILE)
    if(not os.path.isfile(maestro_json_file)):
        print("ERROR: Could not find file:", maestro_json_file)
        return False

    maestro_json = json.load(open(maestro_json_file, "r"))
    print("Found", len(maestro_json), "pieces")
    print("Preprocessing...")
    # todo

    train_count = 0
    val_count   = 0
    test_count  = 0

    train = []
    val = []
    test = []

    for piece in maestro_json:
        mid         = os.path.join(maestro_root, piece["midi_filename"])
        split_type  = piece["split"]
        prepped = midi_processor.encode_midi(mid)

        if(split_type == "train"):
            train_count += 1
            train.append(prepped)
        elif(split_type == "validation"):
            val_count += 1
            val.append(prepped)
        elif(split_type == "test"):
            test_count += 1
            test.append(prepped)
        else:
            print("ERROR: Unrecognized split type:", split_type)
            return False

        if hasattr(config, 'take'):
            if config.take <= train_count:
                break

    print("Num Train:", train_count)
    print("Num Val:", val_count)
    print("Num Test:", test_count)

    train_dataset, val_dataset, test_dataset = create_epiano_datasets_from_maestro(config, train, val, test, max_sequence)
    train_loader = DataLoader(train_dataset, batch_size=batch_size, num_workers=n_workers, shuffle=True)
    val_loader = DataLoader(val_dataset, batch_size=batch_size, num_workers=n_workers)
    test_loader = DataLoader(test_dataset, batch_size=batch_size, num_workers=n_workers)

    dsdict = { "train_loader" : train_loader, "val_loader" : val_loader, "test_loader" :test_loader }
    ds = DictToObject(dsdict)
    return ds

def create_epiano_datasets_from_maestro(config, train, val, test, max_seq, random_seq=True):
    train_dataset = EPianoDatasetFromPrepped(config, train, max_seq, random_seq)
    val_dataset = EPianoDatasetFromPrepped(config, val, max_seq, random_seq)
    test_dataset = EPianoDatasetFromPrepped(config, test, max_seq, random_seq)

    return train_dataset, val_dataset, test_dataset

def create_epiano_datasets_from_dir(config, root, max_seq, random_seq=True):
    train_dataset = EPianoDatasetFromDir(config, root + "/train", max_seq, random_seq)
    val_dataset = EPianoDatasetFromDir(config, root + "/val", max_seq, random_seq)
    test_dataset = EPianoDatasetFromDir(config, root + "/test", max_seq, random_seq)

    return train_dataset, val_dataset, test_dataset

def do_dir2(myobj, config, directories):
    import random
    import glob

    batch_size = 128
    filenames = glob.glob(str(config.dataset/'**/*.mid*'))
    print('Number of files:', len(filenames))

    # Create a dataset from text files
    random.shuffle(filenames)
    text_ds = None #tf_data.TextLineDataset(filenames)
    text_ds = text_ds.shuffle(buffer_size=256)
    text_ds = text_ds.batch(batch_size)

    dsdict = { "train_ds" : text_ds, "val_ds" : None, "test_ds" : None }
    ds = DictToObject(dsdict)
    return ds

def do_dir(myobj, config):
    print("Preprocessing...")

    root = myobj.dataset
    train_dataset, val_dataset, test_dataset = create_epiano_datasets_from_dir(config, root, max_sequence)
    train_loader = DataLoader(train_dataset, batch_size=batch_size, num_workers=n_workers, shuffle=True)
    val_loader = DataLoader(val_dataset, batch_size=batch_size, num_workers=n_workers)
    test_loader = DataLoader(test_dataset, batch_size=batch_size, num_workers=n_workers)

    return train_loader, val_loader, test_loader
    dsdict = { "train_loader" : train_loader, "val_loader" : val_loader, "test_loader" :test_loader }
    ds = DictToObject(dsdict)
    return ds

def filenamedir(myobj, config):
    return [ myobj.dataset ]

def getdatapath(myobj):
    return getpath(myobj) + "data/"

def getdownloadpath(myobj):
    return getpath(myobj) + "download/"

def getpath(myobj):
    if hasattr(myobj, 'path') and not myobj.path is None:
        return myobj.path + '/data/'
    return '/tmp/data/'

class EPianoDatasetFromPrepped(Dataset):
    def __init__(self, config, preps, max_seq=2048, random_seq=True):
        self.preps      = preps
        self.max_seq    = max_seq
        self.random_seq = random_seq

    def __len__(self):
        return len(self.preps)

    def __getitem__(self, idx):
        raw_mid     = torch.tensor(self.preps[idx], dtype=TORCH_LABEL_TYPE
                                   , device=cpu_device())

        x, tgt = process_midi(raw_mid, self.max_seq, self.random_seq)

        return x, tgt


import util.processor as midi_processor


class EPianoDatasetFromDir(Dataset):
    def __init__(self, config, root, max_seq=2048, random_seq=True):
        import os
        self.root       = root
        self.max_seq    = max_seq
        self.random_seq = random_seq
        fs = [os.path.join(root, f) for f in os.listdir(self.root)]
        self.data_files = [f for f in fs if os.path.isfile(f)]
        if hasattr(config, 'take'):
            self.data_files = self.data_files[:config.take]


    def __len__(self):
        return len(self.data_files)

    def __getitem__(self, idx):
        prepped = midi_processor.encode_midi(self.data_files[idx])
        raw_mid     = torch.tensor(prepped, dtype=TORCH_LABEL_TYPE, device=cpu_device())

        x, tgt = process_midi(raw_mid, self.max_seq, self.random_seq)

        return x, tgt


batch_size=2
n_workers=1
max_sequence=2048
