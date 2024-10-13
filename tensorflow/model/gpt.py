import os
import keras_nlp
import keras

import tensorflow.data as tf_data


EMBED_DIM = 256
FEED_FORWARD_DIM = 128
NUM_HEADS = 3
NUM_LAYERS = 2

NUM_TOKENS_TO_GENERATE = 80

class Model:
    def __init__(self, myobj, config, md):
        self.myobj = myobj
        self.config = config
        self.md = md

        inputs = keras.layers.Input(shape=(None,), dtype="int32")

        embedding_layer = keras_nlp.layers.TokenAndPositionEmbedding(
            vocabulary_size=md.vocab_size,
            sequence_length=md.seq_len,
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

        outputs = keras.layers.Dense(md.vocab_size)(x)
        model = keras.Model(inputs=inputs, outputs=outputs)
        loss_fn = keras.losses.SparseCategoricalCrossentropy(from_logits=True)
        perplexity = keras_nlp.metrics.Perplexity(from_logits=True, mask_token_id=0)
        model.compile(optimizer="adam", loss=loss_fn, metrics=[perplexity])

        model.summary()

        self.model = model


    def fit(self, train_ds, val_ds, test_ds):
        self.model.fit(train_ds, validation_data=val_ds, epochs=self.config.steps)

    # TODO model.generate

    def generate(self, model):
        text_generation_callback = self.TopKTextGenerator(k=10)
        # dummy
        model.fit(self.md.train_ds.take(1), verbose=2, epochs=2, callbacks=[text_generation_callback])
        return self.txt

    def generate2(self, model):
        start_prompt = self.myobj.classifyarray[0]
        prompt_tokens = self.md.start_packer(self.md.tokenizer([start_prompt]))
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
        txt = self.md.tokenizer.detokenize(output_tokens)
        return txt

    def localsave(self):
       return True

    def save(self, filename):
        print("type", type(self.model))
        print(filename)
        self.model.save(filename)

    def next(self, prompt, cache, index):
        logits = self.model(prompt)[:, index - 1, :]
        hidden_states = None
        return logits, hidden_states, cache

    class TopKTextGenerator(keras.callbacks.Callback):

        def __init__(self, k):
            start_prompt = self.myobj.classifyarray[0]
            self.sampler = keras_nlp.samplers.TopKSampler(k)
            self.prompt_tokens = self.md.start_packer(self.md.tokenizer([start_prompt]))
            self.txt = None

        def on_epoch_end(self, epoch, logs=None):
            output_tokens = self.sampler(
                next=next,
                prompt=self.prompt_tokens,
                index=1,
            )
            self.txt = self.md.tokenizer.detokenize(output_tokens)

#prompt_tokens = start_packer(tokenizer([""]))
#prompt_tokens


