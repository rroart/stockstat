from model.midi.music_transformer import MusicTransformer
import torch.optim as optim
import numpy as np
import time
import os
from model.midi.criterion import SmoothCrossEntropyLoss, CustomSchedule
from model.midi.metrics import *
from util.processor import decode_midi, encode_midi
from util.midi import TOKEN_PAD, VOCAB_SIZE, process_midi, TORCH_LABEL_TYPE
#borrow
from model.midirpr.utils import get_device

batch_size = 8
debug = True

embedding_dim=256
num_layers=6
max_seq=2048
dropout=0.2
label_smooth=0.1

class Model:
    def __init__(self, myobj, config, dataset):
        self.myobj = myobj
        self.config = config
        self.dataset = dataset
        self.mt = MusicTransformer(
            embedding_dim=embedding_dim,
            vocab_size=VOCAB_SIZE,
            num_layer=num_layers,
            max_seq=max_seq,
            dropout=dropout,
            debug=debug, loader_path=None)
        self.opt = optim.Adam(self.mt.parameters(), lr=0, betas=(0.9, 0.98), eps=1e-9)
        self.scheduler = CustomSchedule(embedding_dim, optimizer=self.opt)
        self.device = None

    def localsave(self):
       return True

    def fit(self):
        metric_set = MetricsSet({
            'accuracy': CategoricalAccuracy(),
            'loss': SmoothCrossEntropyLoss(label_smooth, VOCAB_SIZE, TOKEN_PAD),
            'bucket':  LogitsBucketting(VOCAB_SIZE)
        })

        idx = 0
        for e in range(self.config.steps):
            print(">>> [Epoch was updated]")
            for batch_num, batch in enumerate(self.dataset.train_loader):
                #for batch_num in range(len(dataset.files) // self.config.batch_size):
                self.scheduler.optimizer.zero_grad()
                try:
                    batch_x, batch_y = batch[0], batch[1]
                    #batch_x = torch.from_numpy(batch_x).contiguous().to(self.device, non_blocking=True, dtype=torch.int)
                    #batch_y = torch.from_numpy(batch_y).contiguous().to(self.device, non_blocking=True, dtype=torch.int)
                except IndexError:
                    print("indexerror")
                    continue

                start_time = time.time()
                self.mt.train()
                sample = self.mt.forward(batch_x)
                metrics = metric_set(sample, batch_y)
                loss = metrics['loss']
                loss.backward()
                self.scheduler.step()
                end_time = time.time()

                if debug:
                    print("[Loss]: {}".format(loss))

                #train_summary_writer.add_scalar('loss', metrics['loss'], global_step=idx)
                #train_summary_writer.add_scalar('accuracy', metrics['accuracy'], global_step=idx)
                #train_summary_writer.add_scalar('learning_rate', scheduler.rate(), global_step=idx)
                #train_summary_writer.add_scalar('iter_p_sec', end_time-start_time, global_step=idx)

                # result_metrics = metric_set(sample, batch_y)
                if batch_num % 100 == 0:
                    single_mt = self.mt
                    single_mt.eval()
                    batch = next(iter(self.dataset.val_loader))
                    #eval_x, eval_y = batch[0], batch[1]
                    eval_x, eval_y = batch[0], batch[1]
                    #eval_x = torch.from_numpy(eval_x).contiguous().to(self.device, dtype=torch.int)
                    #eval_y = torch.from_numpy(eval_y).contiguous().to(self.device, dtype=torch.int)

                    eval_prediction, weights = single_mt.forward(eval_x)

                    eval_metrics = metric_set(eval_prediction, eval_y)
                    torch.save(single_mt.state_dict(), '/tmp/train-{}.pth'.format(e))
                    if batch_num == 0:
                        #train_summary_writer.add_histogram("target_analysis", batch_y, global_step=e)
                        #train_summary_writer.add_histogram("source_analysis", batch_x, global_step=e)
                        for i, weight in enumerate(weights):
                            attn_log_name = "attn/layer-{}".format(i)
                            #utils.attention_image_summary(
                            #    attn_log_name, weight, step=idx, writer=eval_summary_writer)

                    #eval_summary_writer.add_scalar('loss', eval_metrics['loss'], global_step=idx)
                    #eval_summary_writer.add_scalar('accuracy', eval_metrics['accuracy'], global_step=idx)
                    #eval_summary_writer.add_histogram("logits_bucket", eval_metrics['bucket'], global_step=idx)

                    print('\n====================================================')
                    print('Epoch/Batch: {}/{}'.format(e, batch_num))
                    print('Train >>>> Loss: {:6.6}, Accuracy: {}'.format(metrics['loss'], metrics['accuracy']))
                    print('Eval >>>> Loss: {:6.6}, Accuracy: {}'.format(eval_metrics['loss'], eval_metrics['accuracy']))
                torch.cuda.empty_cache()
                idx += 1

                # switch output device to: gpu-1 ~ gpu-n
                sw_start = time.time()
                if torch.cuda.device_count() > 1:
                    self.mt.output_device = idx % (torch.cuda.device_count() -1) + 1
                sw_end = time.time()
                if debug:
                    print('output switch time: {}'.format(sw_end - sw_start) )

    def generate(self, filename):
        num_prime = 256
        target_seq_length = 1024
        #inputs = np.array([[24, 28, 31]])
        #inputs = torch.from_numpy(inputs)
        #length=4000
        #result = self.mt(inputs, length, None)
        #decode_midi(result, file_path="/tmp")
        #return
        if filename is None:
            batch = next(iter(self.dataset.val_loader))
            primer, _ = batch[0], batch[1]
            print("tt", type(primer), len(primer), primer)
            primer = primer[0]
            print("tt", type(primer), len(primer), primer)
        else:
            raw_mid = encode_midi(filename)
            if(len(raw_mid) == 0):
                print("Error: No midi messages in primer file:", filename)
                return
            primer, _  = process_midi(raw_mid, num_prime, random_seq=False)
            print("tt", type(primer), len(primer), primer)
            primer = torch.tensor(primer, dtype=TORCH_LABEL_TYPE, device=get_device())
            print("tt", type(primer), len(primer), primer)

        os.makedirs("/tmp/download", 0o777, True)
        #rand_seq = self.model.generate(primer[:num_prime], target_seq_length, beam=0)
        length=4000
        length=target_seq_length
        primer = primer[:2048]
        primer = primer.numpy()
        primer = torch.from_numpy(np.array([primer]))
        result = self.mt(primer, length, None)
        print("result", result)
        afile = "rand.mid"
        decode_midi(result[0][0].detach().numpy(), file_path="/tmp/download/" + afile)
        return [ afile ]
