import os
import keras_nlp
import keras
import keras_nlp

import tensorflow.data as tf_data

BATCH_SIZE = 64
MIN_STRING_LEN = 512
SEQ_LEN = 128

EMBED_DIM = 256
FEED_FORWARD_DIM = 128
NUM_HEADS = 3
NUM_LAYERS = 2
VOCAB_SIZE = 5000

class Model:
    def __init__(self, myobj, config, dataset):
        self.myobj = myobj
        self.config = config
        self.dataset = dataset

        train_ds, val_ds, vocab_size, vocab = self.tokenize(dataset)
        self.train_ds = train_ds
        self.val_ds = val_ds

        inputs = keras.layers.Input(shape=(None,), dtype="int32")

        embedding_layer = keras_nlp.layers.TokenAndPositionEmbedding(
            vocabulary_size=vocab_size,
            sequence_length=SEQ_LEN,
            embedding_dim=EMBED_DIM,
            mask_zero=True,
        )
        x = embedding_layer(inputs)

        for _ in range(NUM_LAYERS):
            decoder_layer = keras_nlp.layers.TransformerDecoder(
                num_heads=NUM_HEADS,
                intermediate_dim=FEED_FORWARD_DIM,
            )
            x = decoder_layer(x)

        outputs = keras.layers.Dense(vocab_size)(x)
        model = keras.Model(inputs=inputs, outputs=outputs)
        loss_fn = keras.losses.SparseCategoricalCrossentropy(from_logits=True)
        perplexity = keras_nlp.metrics.Perplexity(from_logits=True, mask_token_id=0)
        model.compile(optimizer="adam", loss=loss_fn, metrics=[perplexity])

        model.summary()

        self.model = model

    def tokenize(self, dataset):
        vocab_size = VOCAB_SIZE
        if hasattr(self.config, 'vocab'):
            vocab_size = self.config.vocab
        train_ds = dataset.train_ds
        val_ds = dataset.val_ds
        if hasattr(self.config, 'take'):
            train_ds = train_ds.take(self.config.take)
            if val_ds is not None:
                val_ds = val_ds.take(self.config.take)

        vocab = keras_nlp.tokenizers.compute_word_piece_vocabulary(
            train_ds,
            vocabulary_size=vocab_size,
            lowercase=True,
            reserved_tokens=["[PAD]", "[UNK]", "[BOS]"],
        )

        tokenizer = keras_nlp.tokenizers.WordPieceTokenizer(
            vocabulary=vocab,
            sequence_length=SEQ_LEN,
            lowercase=True,
        )

        start_packer = keras_nlp.layers.StartEndPacker(
            sequence_length=SEQ_LEN,
            start_value=tokenizer.token_to_id("[BOS]"),
        )

        def preprocess(inputs):
            outputs = tokenizer(inputs)
            features = start_packer(outputs)
            labels = outputs
            return features, labels

        train_ds = train_ds.map(preprocess, num_parallel_calls=tf_data.AUTOTUNE).prefetch(
            tf_data.AUTOTUNE
        )

        if val_ds is not None:
            val_ds = val_ds.map(preprocess, num_parallel_calls=tf_data.AUTOTUNE).prefetch(
                tf_data.AUTOTUNE
            )

        return train_ds, val_ds, vocab_size, vocab

    def fit(self):
        self.model.fit(self.train_ds, validation_data=self.val_ds, epochs=self.config.steps)

    # TODO model.generate

    def generate(self, model):
        text_generation_callback = self.TopKTextGenerator(self.myobj, self.model, self, k=10)
        # dummy
        model.fit(self.dataset.train_ds.take(1), verbose=2, epochs=2, callbacks=[text_generation_callback])
        return text_generation_callback.txt

    def generate2(self, model):
        start_prompt = self.myobj.classifyarray[0]
        prompt_tokens = self.start_packer(self.tokenizer([start_prompt]))
        sampler = keras_nlp.samplers.GreedySampler()
        sampler = keras_nlp.samplers.BeamSampler(num_beams=10)
        sampler = keras_nlp.samplers.RandomSampler()
        sampler = keras_nlp.samplers.TopKSampler(k=10)
        sampler = keras_nlp.samplers.TopPSampler(p=0.5)
        output_tokens = sampler(
            next=next,
            prompt=prompt_tokens,
            index=1,  # Start sampling immediately after the [BOS] token.
        )
        txt = self.tokenizer.detokenize(output_tokens)
        return txt

    def localsave(self):
       return True

    def save(self, filename):
        print("type", type(self.model))
        print(filename)
        self.model.save(filename)

    class TopKTextGenerator(keras.callbacks.Callback):

        def __init__(self, myobj,  model, allmodel, k):
            self.myobj = myobj
            self.amodel = model
            self.allmodel = allmodel
            start_prompt = self.myobj.classifyarray[0]
            self.sampler = keras_nlp.samplers.TopKSampler(k)
            self.prompt_tokens = self.allmodel.start_packer(self.allmodel.tokenizer([start_prompt]))
            self.txt = None

        def on_epoch_end(self, epoch, logs=None):
            def next(prompt, cache, index):
                logits = self.amodel(prompt)[:, index - 1, :]
                hidden_states = None
                return logits, hidden_states, cache

            output_tokens = self.sampler(
                next=next,
                prompt=self.prompt_tokens,
                index=1,
            )
            self.txt = self.allmodel.tokenizer.detokenize(output_tokens)

#prompt_tokens = start_packer(tokenizer([""]))
#prompt_tokens


