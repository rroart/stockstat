import subprocess

import torch
import os
import logging
import mmt.representation
import model.mmt.music_x_transformers
import tqdm
import numpy as np
import shutil
import matplotlib.pyplot as plt
import mmt.args

# must be citing Hao-Wen Dong, Ke Chen, Shlomo Dubnov, Julian McAuley, and Taylor Berg-Kirkpatrick, "Multitrack Music Transformer," _IEEE International Conference on Acoustics, Speech and Signal Processing (ICASSP)_, 2023.

class Model:
    def __init__(self, myobj, config, dataset):
        self.myobj = myobj
        self.config = config
        self.dataset = dataset
        dir = getpath(myobj)
        self.args = mmt.args.Args(myobj.dataset, dir, config)

        # Get the specified device
        device = torch.device(
            f"cuda:{self.args.gpu}" if self.args.gpu is not None else "cpu"
        )
        device="cpu:0"
        logging.info(f"Using device: {device}")

        # Load the encoding
        encoding = mmt.representation.load_encoding(self.args.in_dir / "encoding.json")

        # Create the model
        logging.info(f"Creating model...")
        self.model = model.mmt.music_x_transformers.MusicXTransformer(
            dim=self.args.dim,
            encoding=encoding,
            depth=self.args.layers,
            heads=self.args.heads,
            max_seq_len=self.args.max_seq_len,
            max_beat=self.args.max_beat,
            rotary_pos_emb=self.args.rel_pos_emb,
            use_abs_pos_emb=self.args.abs_pos_emb,
            emb_dropout=self.args.dropout,
            attn_dropout=self.args.dropout,
            ff_dropout=self.args.dropout,
        ).to(device)

        # Summarize the model
        n_parameters = sum(p.numel() for p in self.model.parameters())
        n_trainables = sum(
            p.numel() for p in self.model.parameters() if p.requires_grad
        )
        logging.info(f"Number of parameters: {n_parameters}")
        logging.info(f"Number of trainable parameters: {n_trainables}")

        # Create the optimizer
        self.optimizer = torch.optim.Adam(self.model.parameters(), self.args.learning_rate)
        self.scheduler = torch.optim.lr_scheduler.LambdaLR(
            self.optimizer,
            lr_lambda=lambda step: get_lr_multiplier(
                step,
                self.args.lr_warmup_steps,
                self.args.lr_decay_steps,
                self.args.lr_decay_multiplier,
            ),
        )

        # Create a file to record losses
        self.args.out_dir.mkdir(exist_ok=True, parents=True)
        self.loss_csv = open(self.args.out_dir / "loss.csv", "w")
        self.loss_csv.write(
            "step,train_loss,valid_loss,type_loss,beat_loss,position_loss,"
            "pitch_loss,duration_loss,instrument_loss\n"
        )


    def localsave(self):
       return True

    def fit(self):
        import os
        #self.trainer.fit(self.model, self.datamodule)
        # Get the specified device
        device = torch.device(
            f"cuda:{self.args.gpu}" if self.args.gpu is not None else "cpu"
        )
        # Initialize variables
        step = 0
        min_val_loss = float("inf")
        if self.args.early_stopping:
            count_early_stopping = 0
        # Iterate for the specified number of steps
        train_iterator = iter(self.dataset.train_loader)
        while step < self.args.steps:

            # Training
            logging.info(f"Training...")
            self.model.train()
            recent_losses = []

            for batch in (pbar := tqdm.tqdm(range(self.args.valid_steps), ncols=80)):
                # Get next batch
                try:
                    batch = next(train_iterator)
                except StopIteration:
                    # Reinitialize dataset iterator
                    train_iterator = iter(self.dataset.train_loader)
                    batch = next(train_iterator)

                # Get input and output pair
                seq = batch["seq"].to(device)
                mask = batch["mask"].to(device)

                # Update the model parameters
                self.optimizer.zero_grad()
                loss = self.model(seq, mask=mask)
                loss.backward()
                torch.nn.utils.clip_grad_norm_(
                    self.model.parameters(), self.args.grad_norm_clip
                )
                self.optimizer.step()
                self.scheduler.step()

                # Compute the moving average of the loss
                recent_losses.append(float(loss))
                if len(recent_losses) > 10:
                    del recent_losses[0]
                train_loss = np.mean(recent_losses)
                pbar.set_postfix(loss=f"{train_loss:8.4f}")

                step += 1

            # Release GPU memory right away
            del seq, mask

            # Validation
            logging.info(f"Validating...")
            self.model.eval()
            with torch.no_grad():
                total_loss = 0
                total_losses = [0] * 6
                count = 0
                for batch in self.dataset.val_loader:
                    # Get input and output pair
                    seq = batch["seq"].to(device)
                    mask = batch["mask"].to(device)

                    # Pass through the model
                    loss, losses = self.model(seq, return_list=True, mask=mask)

                    # Accumulate validation loss
                    count += len(batch)
                    total_loss += len(batch) * float(loss)
                    for idx in range(6):
                        total_losses[idx] += float(losses[idx])
            val_loss = total_loss / count
            individual_losses = [l / count for l in total_losses]
            logging.info(f"Validation loss: {val_loss:.4f}")
            logging.info(
                f"Individual losses: type={individual_losses[0]:.4f}, "
                f"beat: {individual_losses[1]:.4f}, "
                f"position: {individual_losses[2]:.4f}, "
                f"pitch: {individual_losses[3]:.4f}, "
                f"duration: {individual_losses[4]:.4f}, "
                f"instrument: {individual_losses[5]:.4f}"
            )

            # Release GPU memory right away
            del seq, mask

            # Write losses to file
            self.loss_csv.write(
                f"{step},{train_loss},{val_loss},{individual_losses[0]},"
                f"{individual_losses[1]},{individual_losses[2]},"
                f"{individual_losses[3]},{individual_losses[4]},"
                f"{individual_losses[5]}\n"
            )

            # Save the model
            import pathlib
            pathlib.Path(self.args.out_dir / "checkpoints").mkdir(exist_ok=True, parents=True)
            checkpoint_filename = self.args.out_dir / "checkpoints" / f"model_{step}.pt"
            torch.save(self.model.state_dict(), checkpoint_filename)
            logging.info(f"Saved the model to: {checkpoint_filename}")

            # Copy the model if it is the best model so far
            if val_loss < min_val_loss:
                min_val_loss = val_loss
                shutil.copyfile(
                    checkpoint_filename,
                    self.args.out_dir / "checkpoints" / "best_model.pt",
                )
                # Reset the early stopping counter if we found a better model
                if self.args.early_stopping:
                    count_early_stopping = 0
            elif self.args.early_stopping:
                # Increment the early stopping counter if no improvement is found
                count_early_stopping += 1

            # Early stopping
            if (
                self.args.early_stopping
                and count_early_stopping > self.args.early_stopping_tolerance
            ):
                logging.info(
                    "Stopped the training for no improvements in "
                    f"{self.args.early_stopping_tolerance} rounds."
                )
                break
        # Log minimum validation loss
        logging.info(f"Minimum validation loss achieved: {min_val_loss}")


    def save(self):
        #torch.save( { 'model' : trainer }, "/tmp/e.ckpt")
        #torch.save( trainer, "/tmp/e2.ckpt")
        #torch.save( model.state_dict(), "/tmp/e3.ckpt")
        #torch.save( { 'state_dict' : self.model.state_dict() }, "/tmp/e4.ckpt")
        # Save the optimizer states
        step = 0
        optimizer_filename = self.args.out_dir / "checkpoints" / f"optimizer_{step}.pt"
        torch.save(self.optimizer.state_dict(), optimizer_filename)
        logging.info(f"Saved the optimizer state to: {optimizer_filename}")

        # Save the scheduler states
        scheduler_filename = self.args.out_dir / "checkpoints" / f"scheduler_{step}.pt"
        torch.save(self.scheduler.state_dict(), scheduler_filename)
        logging.info(f"Saved the scheduler state to: {scheduler_filename}")

        # Close the file
        self.loss_csv.close()

    def generate(self, filename):
        device = torch.device(
            f"cuda:{self.args.gpu}" if self.args.gpu is not None else "cpu"
        )
        sample_dir = self.args.out_dir / "samples"
        sample_dir.mkdir(exist_ok=True)
        (sample_dir / "npy").mkdir(exist_ok=True)
        (sample_dir / "csv").mkdir(exist_ok=True)
        (sample_dir / "txt").mkdir(exist_ok=True)
        (sample_dir / "json").mkdir(exist_ok=True)
        (sample_dir / "png").mkdir(exist_ok=True)
        (sample_dir / "mid").mkdir(exist_ok=True)
        (sample_dir / "wav").mkdir(exist_ok=True)
        (sample_dir / "mp3").mkdir(exist_ok=True)
        (sample_dir / "png-trimmed").mkdir(exist_ok=True)
        (sample_dir / "wav-trimmed").mkdir(exist_ok=True)
        (sample_dir / "mp3-trimmed").mkdir(exist_ok=True)
        # Load the encoding
        encoding = mmt.representation.load_encoding(self.args.in_dir / "encoding.json")

        #TODO move
        # Create the dataset and data loader
        logging.info(f"Creating the data loader...")
        test_dataset = mmt.dataset.MusicDataset(
            self.args.train_names,
            self.args.in_dir,
            encoding,
            max_seq_len=self.args.max_seq_len,
            max_beat=self.args.max_beat,
            use_csv=self.args.use_csv,
        )
        test_loader = torch.utils.data.DataLoader(
            test_dataset,
            shuffle=self.args.shuffle,
            num_workers=self.args.jobs,
            collate_fn=mmt.dataset.MusicDataset.collate,
        )

        # Load the checkpoint
        checkpoint_dir = self.args.out_dir / "checkpoints"
        if self.args.model_steps is None:
            checkpoint_filename = checkpoint_dir / "best_model.pt"
        else:
            checkpoint_filename = checkpoint_dir / f"model_{self.args.model_steps}.pt"
        print("filename", checkpoint_filename)
        self.model.load_state_dict(torch.load(checkpoint_filename, map_location=device))
        logging.info(f"Loaded the model weights from: {checkpoint_filename}")
        self.model.eval()

        # Get special tokens
        sos = encoding["type_code_map"]["start-of-song"]
        eos = encoding["type_code_map"]["end-of-song"]
        beat_0 = encoding["beat_code_map"][0]
        beat_4 = encoding["beat_code_map"][4]
        beat_16 = encoding["beat_code_map"][16]
        # Iterate over the dataset
        with torch.no_grad():
            data_iter = iter(test_loader)
            for i in tqdm.tqdm(range(self.args.n_samples), ncols=80):
                print("Iter", i)
                batch = next(data_iter)

                # ------------
                # Ground truth
                # ------------
                truth_np = batch["seq"][0].numpy()
                self.save_result(f"{i}_truth", truth_np, sample_dir, encoding)

                # ------------------------
                # Unconditioned generation
                # ------------------------

                # Get output start tokens
                tgt_start = torch.zeros((1, 1, 6), dtype=torch.long, device=device)
                tgt_start[:, 0, 0] = sos

                # Generate new samples
                generated = self.model.generate(
                    tgt_start,
                    self.args.seq_len,
                    eos_token=eos,
                    temperature=self.args.temperature,
                    filter_logits_fn=self.args.filter,
                    filter_thres=self.args.filter_threshold,
                    monotonicity_dim=("type", "beat"),
                )
                generated_np = torch.cat((tgt_start, generated), 1).cpu().numpy()

                # Save the results
                self.save_result(
                    f"{i}_unconditioned", generated_np[0], sample_dir, encoding
                )

                # ------------------------------
                # Instrument-informed generation
                # ------------------------------

                # Get output start tokens
                prefix_len = int(np.argmax(batch["seq"][0, :, 1] >= beat_0))
                tgt_start = batch["seq"][:1, :prefix_len].to(device)

                # Generate new samples
                generated = self.model.generate(
                    tgt_start,
                    self.args.seq_len,
                    eos_token=eos,
                    temperature=self.args.temperature,
                    filter_logits_fn=self.args.filter,
                    filter_thres=self.args.filter_threshold,
                    monotonicity_dim=("type", "beat"),
                )
                generated_np = torch.cat((tgt_start, generated), 1).cpu().numpy()

                # Save the results
                self.save_result(
                    f"{i}_instrument-informed",
                    generated_np[0],
                    sample_dir,
                    encoding,
                )

                # -------------------
                # 4-beat continuation
                # -------------------

                # Get output start tokens
                cond_len = int(np.argmax(batch["seq"][0, :, 1] >= beat_4))
                tgt_start = batch["seq"][:1, :cond_len].to(device)
                # Generate new samples
                generated = self.model.generate(
                    tgt_start,
                    self.args.seq_len,
                    eos_token=eos,
                    temperature=self.args.temperature,
                    filter_logits_fn=self.args.filter,
                    filter_thres=self.args.filter_threshold,
                    monotonicity_dim=("type", "beat"),
                )
                generated_np = torch.cat((tgt_start, generated), 1).cpu().numpy()

                # Save the results
                self.save_result(
                    f"{i}_4-beat-continuation",
                    generated_np[0],
                    sample_dir,
                    encoding,
                )

                # --------------------
                # 16-beat continuation
                # --------------------

                # Get output start tokens
                cond_len = int(np.argmax(batch["seq"][0, :, 1] >= beat_16))
                tgt_start = batch["seq"][:1, :cond_len].to(device)

                # Generate new samples
                generated = self.model.generate(
                    tgt_start,
                    self.args.seq_len,
                    eos_token=eos,
                    temperature=self.args.temperature,
                    filter_logits_fn=self.args.filter,
                    filter_thres=self.args.filter_threshold,
                    monotonicity_dim=("type", "beat"),
                )
                generated_np = torch.cat((tgt_start, generated), 1).cpu().numpy()

                # Save results
                self.save_result(
                    f"{i}_16-beat-continuation",
                    generated_np[0],
                    sample_dir,
                    encoding,
                )

    def get_lr_multiplier(
        step, warmup_steps, decay_end_steps, decay_end_multiplier
    ):
        """Return the learning rate multiplier with a warmup and decay schedule.

        The learning rate multiplier starts from 0 and linearly increases to 1
        after `warmup_steps`. After that, it linearly decreases to
        `decay_end_multiplier` until `decay_end_steps` is reached.

        """
        if step < warmup_steps:
            return (step + 1) / warmup_steps
        if step > decay_end_steps:
            return decay_end_multiplier
        position = (step - warmup_steps) / (decay_end_steps - warmup_steps)
        return 1 - (1 - decay_end_multiplier) * position

    def save_pianoroll(self, filename, music, size=None, **kwargs):
        """Save the piano roll to file."""
        # todo temp workaround
        try:
            music.show_pianoroll(track_label="program", **kwargs)
        except:
            print("pianoroll crash")
        if size is not None:
            plt.gcf().set_size_inches(size)
        plt.savefig(filename)
        plt.close()


    def save_result(self, filename, data, sample_dir, encoding):
        """Save the results in multiple formats."""
        # Save as a numpy array
        np.save(sample_dir / "npy" / f"{filename}.npy", data)

        # Save as a CSV file
        mmt.representation.save_csv_codes(sample_dir / "csv" / f"{filename}.csv", data)

        # Save as a TXT file
        mmt.representation.save_txt(
            sample_dir / "txt" / f"{filename}.txt", data, encoding
        )

        # Convert to a MusPy Music object
        music = mmt.representation.decode(data, encoding)

        # Save as a MusPy JSON file
        music.save(sample_dir / "json" / f"{filename}.json")

        # Save as a piano roll
        self.save_pianoroll(
            sample_dir / "png" / f"{filename}.png", music, (20, 5), preset="frame"
        )

        # Save as a MIDI file
        music.write(sample_dir / "mid" / f"{filename}.mid")
        # Save as a WAV file
        # todo temp workaround
        try:
          music.write(
            sample_dir / "wav" / f"{filename}.wav",
            # TODO options="-o synth.polyphony=4096",
          )
        except:
            print("music write crash")

        # Save also as a MP3 file
        subprocess.check_output(
            ["ffmpeg", "-loglevel", "error", "-y", "-i"]
            + [str(sample_dir / "wav" / f"{filename}.wav")]
            + ["-b:a", "192k"]
            + [str(sample_dir / "mp3" / f"{filename}.mp3")]
        )

        # Trim the music
        music.trim(music.resolution * 64)

        # Save the trimmed version as a piano roll
        self.save_pianoroll(
            sample_dir / "png-trimmed" / f"{filename}.png", music, (10, 5)
        )

        # Save as a WAV file
        # todo temp workaround
        try:
          music.write(
            sample_dir / "wav-trimmed" / f"{filename}.wav",
            # TODO options="-o synth.polyphony=4096",
          )
        except:
            print("music write crash")

        # Save also as a MP3 file
        subprocess.check_output(
            ["ffmpeg", "-loglevel", "error", "-y", "-i"]
            + [str(sample_dir / "wav-trimmed" / f"{filename}.wav")]
            + ["-b:a", "192k"]
            + [str(sample_dir / "mp3-trimmed" / f"{filename}.mp3")]
        )


def get_lr_multiplier(
    step, warmup_steps, decay_end_steps, decay_end_multiplier
):
    """Return the learning rate multiplier with a warmup and decay schedule.

    The learning rate multiplier starts from 0 and linearly increases to 1
    after `warmup_steps`. After that, it linearly decreases to
    `decay_end_multiplier` until `decay_end_steps` is reached.

    """
    if step < warmup_steps:
        return (step + 1) / warmup_steps
    if step > decay_end_steps:
        return decay_end_multiplier
    position = (step - warmup_steps) / (decay_end_steps - warmup_steps)
    return 1 - (1 - decay_end_multiplier) * position


def getpath(myobj):
    if hasattr(myobj, 'path') and not myobj.path is None:
        return myobj.path + '/data/'
    return '/tmp/data/'


