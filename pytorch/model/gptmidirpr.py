import torch
import torch.nn as nn
import os
from model.midirpr.music_transformer import MusicTransformer
# borrowed
from model.midi.criterion import SmoothCrossEntropyLoss
from torch.optim import Adam
from util.processor import decode_midi, encode_midi
from util.midi import process_midi, TOKEN_PAD, VOCAB_SIZE, TORCH_LABEL_TYPE, TORCH_FLOAT
from model.midirpr.utils import get_device

n_layers = 6
num_heads = 8
d_model = 512
dim_feedforward = 1024
dropout = 0.1
max_sequence = 2048
ADAM_BETA_1             = 0.9
ADAM_BETA_2             = 0.98
ADAM_EPSILON            = 10e-9

class Model:
    def __init__(self, myobj, config, dataset):
        self.myobj = myobj
        self.config = config
        self.dataset = dataset
        self.model = MusicTransformer(n_layers=n_layers, num_heads=num_heads,
                d_model=d_model, dim_feedforward=dim_feedforward, dropout=dropout,
                max_sequence=max_sequence, rpr=config.rpr).to(get_device())

    def localsave(self):
       return True

    def fit(self):
        import os
        weight_modulus = 1
        weights_folder = os.path.join("/tmp", "weights")
        os.makedirs(weights_folder, exist_ok=True)

        results_folder = os.path.join("/tmp", "results")
        os.makedirs(results_folder, exist_ok=True)

        results_folder = os.path.join("/tmp", "results")
        best_loss_file = os.path.join(results_folder, "best_loss_weights.pickle")
        best_acc_file = os.path.join(results_folder, "best_acc_weights.pickle")
        best_text = os.path.join(results_folder, "best_epochs.txt")
        LR_DEFAULT_START        = 1.0

        lr = None
        if(lr is None):
            if(None is None):
                init_step = 0
            else:
                init_step = 0 * len(self.dataset.train_loader)

            lr = LR_DEFAULT_START
            #lr_stepper = LrStepTracker(args.d_model, SCHEDULER_WARMUP_STEPS, init_step)
        else:
            lr = lr
        ce_smoothing = None
        eval_loss_func = nn.CrossEntropyLoss(ignore_index=TOKEN_PAD)
        if (ce_smoothing is None):
            train_loss_func = eval_loss_func
        else:
            train_loss_func = SmoothCrossEntropyLoss(ce_smoothing, VOCAB_SIZE, ignore_index=TOKEN_PAD)
        opt = Adam(self.model.parameters(), lr=lr, betas=(ADAM_BETA_1, ADAM_BETA_2), eps=ADAM_EPSILON)
        best_eval_acc        = 0.0
        best_eval_acc_epoch  = -1
        best_eval_loss       = float("inf")
        best_eval_loss_epoch = -1
        BASELINE_EPOCH = -1
        SEPERATOR               = "========================="
        start_epoch = BASELINE_EPOCH
        for epoch in range(start_epoch, self.config.steps):
            # Baseline has no training and acts as a base loss and accuracy (epoch 0 in a sense)
            if(epoch > BASELINE_EPOCH):
                print(SEPERATOR)
                print("NEW EPOCH:", epoch+1)
                print(SEPERATOR)
                print("")

                # Train
                print_modulus = 1
                lr_scheduler = None
                train_epoch(epoch+1, self.model, self.dataset.train_loader, train_loss_func, opt, lr_scheduler, print_modulus)

                print(SEPERATOR)
                print("Evaluating:")
            else:
                print(SEPERATOR)
                print("Baseline model evaluation (Epoch 0):")

            # Eval
            train_loss, train_acc = eval_model(self.model, self.dataset.train_loader, train_loss_func)
            eval_loss, eval_acc = eval_model(self.model, self.dataset.test_loader, eval_loss_func)

            # Learn rate
            lr = get_lr(opt)

            print("Epoch:", epoch+1)
            print("Avg train loss:", train_loss)
            print("Avg train acc:", train_acc)
            print("Avg eval loss:", eval_loss)
            print("Avg eval acc:", eval_acc)
            print(SEPERATOR)
            print("")

            new_best = False

            if(eval_acc > best_eval_acc):
                best_eval_acc = eval_acc
                best_eval_acc_epoch  = epoch+1
                torch.save(self.model.state_dict(), best_acc_file)
                new_best = True

            if(eval_loss < best_eval_loss):
                best_eval_loss       = eval_loss
                best_eval_loss_epoch = epoch+1
                torch.save(self.model.state_dict(), best_loss_file)
                new_best = True

            # Writing out new bests
            if(new_best):
                with open(best_text, "w") as o_stream:
                    print("Best eval acc epoch:", best_eval_acc_epoch, file=o_stream)
                    print("Best eval acc:", best_eval_acc, file=o_stream)
                    print("")
                    print("Best eval loss epoch:", best_eval_loss_epoch, file=o_stream)
                    print("Best eval loss:", best_eval_loss, file=o_stream)


            #if(False):
            #    tensorboard_summary.add_scalar("Avg_CE_loss/train", train_loss, global_step=epoch+1)
            #    tensorboard_summary.add_scalar("Avg_CE_loss/eval", eval_loss, global_step=epoch+1)
            #    tensorboard_summary.add_scalar("Accuracy/train", train_acc, global_step=epoch+1)
            #    tensorboard_summary.add_scalar("Accuracy/eval", eval_acc, global_step=epoch+1)
            #    tensorboard_summary.add_scalar("Learn_rate/train", lr, global_step=epoch+1)
            #    tensorboard_summary.flush()

            if((epoch+1) % weight_modulus == 0):
                PREPEND_ZEROS_WIDTH     = 4
                epoch_str = str(epoch+1).zfill(PREPEND_ZEROS_WIDTH)
                path = os.path.join(weights_folder, "epoch_" + epoch_str + ".pickle")
                torch.save(self.model.state_dict(), path)

            #with open(results_file, "a", newline="") as o_stream:
            #    writer = csv.writer(o_stream)
            #    writer.writerow([epoch+1, lr, train_loss, train_acc, eval_loss, eval_acc])

    def generate(self, filename):
        num_prime = 256
        target_seq_length = 1024
        import random
        #.isdigit()f = str(random.randrange(len(self.dataset.train_loader)))
        if(filename is None):
            batch = next(iter(self.dataset.val_loader))
            primer, _ = batch[0], batch[1]
            print("tt", type(primer), len(primer), primer)
            primer = primer[0]
            print("tt", type(primer), len(primer), primer)

            #idx = int(f)
            #primer, _  = self.dataset.train_loader[idx]
            #primer = primer.to(get_device())

            #print("Using primer index:", idx)

        else:
            raw_mid = encode_midi(filename)
            if(len(raw_mid) == 0):
                print("Error: No midi messages in primer file:", filename)
                return

            primer, _  = process_midi(raw_mid, num_prime, random_seq=False)
            print("tt", type(primer), len(primer), primer)
            primer = torch.tensor(primer, dtype=TORCH_LABEL_TYPE, device=get_device())
            print("tt", type(primer), len(primer), primer)

        self.model.eval()
        with torch.set_grad_enabled(False):
            os.makedirs("/tmp/download", 0o777, True)
            beam = 0
            if(beam > 0):
                print("BEAM:", beam)
                beam_seq = self.model.generate(primer[:num_prime], target_seq_length, beam=beam)
                afile = "beam.mid"
                decode_midi(beam_seq[0].cpu().numpy(), file_path="/tmp/download/" + afile)
            else:
                print("RAND DIST")
                rand_seq = self.model.generate(primer[:num_prime], target_seq_length, beam=0)
                afile = "rand.mid"
                decode_midi(rand_seq[0].cpu().numpy(), file_path="/tmp/download/" + afile)
        return [ afile ]

def train_epoch(cur_epoch, model, dataloader, loss, opt, lr_scheduler=None, print_modulus=1):
    import time
    out = -1
    model.train()
    for batch_num, batch in enumerate(dataloader):
        time_before = time.time()

        opt.zero_grad()

        x   = batch[0].to(get_device())
        tgt = batch[1].to(get_device())

        y = model(x)

        y   = y.reshape(y.shape[0] * y.shape[1], -1)
        tgt = tgt.flatten()

        out = loss.forward(y, tgt)

        out.backward()
        opt.step()

        if(lr_scheduler is not None):
            lr_scheduler.step()

        time_after = time.time()
        time_took = time_after - time_before

        if((batch_num+1) % print_modulus == 0):
            #print(SEPERATOR)
            print("Epoch", cur_epoch, " Batch", batch_num+1, "/", len(dataloader))
            print("LR:", get_lr(opt))
            print("Train loss:", float(out))
            print("")
            print("Time (s):", time_took)
            #print(SEPERATOR)
            print("")

    return

def get_lr(optimizer):
    for param_group in optimizer.param_groups:
        return param_group['lr']

def eval_model(model, dataloader, loss):
    model.eval()

    avg_acc     = -1
    avg_loss    = -1
    with torch.set_grad_enabled(False):
        n_test      = len(dataloader)
        sum_loss   = 0.0
        sum_acc    = 0.0
        for batch in dataloader:
            x   = batch[0].to(get_device())
            tgt = batch[1].to(get_device())

            y = model(x)

            sum_acc += float(compute_epiano_accuracy(y, tgt))

            y   = y.reshape(y.shape[0] * y.shape[1], -1)
            tgt = tgt.flatten()

            out = loss.forward(y, tgt)

            sum_loss += float(out)

        avg_loss    = sum_loss / n_test
        avg_acc     = sum_acc / n_test

    return avg_loss, avg_acc

def compute_epiano_accuracy(out, tgt):
    softmax = nn.Softmax(dim=-1)
    out = torch.argmax(softmax(out), dim=-1)

    out = out.flatten()
    tgt = tgt.flatten()

    mask = (tgt != TOKEN_PAD)

    out = out[mask]
    tgt = tgt[mask]

    # Empty
    if(len(tgt) == 0):
        return 1.0

    num_right = (out == tgt)
    num_right = torch.sum(num_right).type(TORCH_FLOAT)

    acc = num_right / len(tgt)

    return acc
