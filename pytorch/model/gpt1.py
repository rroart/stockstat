from music_transformer1 import MusicTransformer
import torch
import torch.optim as optim
import time
from model.criterion import SmoothCrossEntropyLoss, CustomSchedule
from model.metrics import *
from util.processor import decode_midi, encode_midi

batch_size = 8
debug = True

embedding_dim=256
vocab_size=388+2
num_layers=6
max_seq=2048
dropout=0.2
label_smooth=0.1
event_dim=388
pad_token=event_dim

class Model:
    def __init__(self, myobj, config, dataset):
        self.myobj = myobj
        self.config = config
        self.dataset = dataset
        self.mt = MusicTransformer(
            embedding_dim=embedding_dim,
            vocab_size=vocab_size,
            num_layer=num_layers,
            max_seq=max_seq,
            dropout=dropout,
            debug=debug, loader_path=config.load_path)
        self.opt = optim.Adam(self.mt.parameters(), lr=0, betas=(0.9, 0.98), eps=1e-9)
        self.scheduler = CustomSchedule(config.embedding_dim, optimizer=self.opt)
        self.device = None

    def localsave(self):
       return True

    def fit(self):
        metric_set = MetricsSet({
            'accuracy': CategoricalAccuracy(),
            'loss': SmoothCrossEntropyLoss(label_smooth, vocab_size, pad_token),
            'bucket':  LogitsBucketting(vocab_size)
        })

        idx = 0
        for e in range(self.config.steps):
            print(">>> [Epoch was updated]")
            for batch_num, batch in enumerate(self.dataset.train_dataset):
                #for batch_num in range(len(dataset.files) // self.config.batch_size):
                self.scheduler.optimizer.zero_grad()
                try:
                    batch_x, batch_y = batch[0], batch[1]
                    batch_x = torch.from_numpy(batch_x).contiguous().to(self.device, non_blocking=True, dtype=torch.int)
                    batch_y = torch.from_numpy(batch_y).contiguous().to(self.device, non_blocking=True, dtype=torch.int)
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
                    batch = next(iter(self.dataset.val_dataset))
                    eval_x, eval_y = batch[0], batch[1]
                    eval_x = torch.from_numpy(eval_x).contiguous().to(self.device, dtype=torch.int)
                    eval_y = torch.from_numpy(eval_y).contiguous().to(self.device, dtype=torch.int)

                    eval_preiction, weights = single_mt.forward(eval_x)

                    eval_metrics = metric_set(eval_preiction, eval_y)
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

    def gen(self):
        inputs = np.array([[24, 28, 31]])
        inputs = torch.from_numpy(inputs)
        length=500
        result = self.mt(inputs, length, None)
        decode_midi(result, file_path="/tmp")
