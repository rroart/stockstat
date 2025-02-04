import os

os.environ["KERAS_BACKEND"] = "tensorflow"

import keras
from keras import layers
from keras import ops
import numpy as np
import tensorflow.data as tf_data

VOCAB_SIZE = 20000
seq_len = 80
SEQ_LEN = 80  # duplicate, borrowed

embed_dim = 256
num_heads = 2
feed_forward_dim = 256

class Model:
    def __init__(self, myobj, config, dataset):
        self.myobj = myobj
        self.config = config
        self.dataset = dataset

        train_ds, val_ds, vocab_size, vocab = self.tokenize(self.dataset)
        self.train_ds = train_ds
        self.val_db = val_ds
        self.vocab = vocab

        self.model = create_model(vocab_size)
        self.vectorize_layer = None

        self.word_to_index = {}
        for index, word in enumerate(vocab):
            self.word_to_index[word] = index

    def tokenize(self, dataset):
        vocab_size = VOCAB_SIZE
        if hasattr(self.config, 'vocab'):
            vocab_size = self.config.vocab
        train_ds = dataset.train_ds
        if hasattr(self.config, 'take'):
            train_ds = train_ds.take(self.config.take)

        self.vectorize_layer = keras.layers.TextVectorization(
            standardize=self.custom_standardization,
            max_tokens=vocab_size - 1,
            output_mode="int",
            output_sequence_length=seq_len + 1,
        )
        self.vectorize_layer.adapt(train_ds)
        vocab = self.vectorize_layer.get_vocabulary()

        train_ds = train_ds.map(self.prepare_lm_inputs_labels, num_parallel_calls=tf_data.AUTOTUNE)
        train_ds = train_ds.prefetch(tf_data.AUTOTUNE)
        return train_ds, None, vocab_size, vocab

    def fit(self):
        self.model.fit(self.train_ds, verbose=2, epochs=self.config.steps)

    def generate(self, model):
        start_prompt = self.myobj.classifyarray[0]
        start_tokens = [self.word_to_index.get(_, 1) for _ in start_prompt.split()]
        num_tokens_generated = self.myobj.classes
        #self.text_gen_callback = TextGenerator(num_tokens_generated, start_tokens, vocab, None)
        generator = TextGenerator2(num_tokens_generated, start_tokens, self.vocab, None)

        return generator.gen(model);

    def localsave(self):
       return True

    def save(self, filename):
        print("type", type(self.model))
        print(filename)
        self.model.save(filename)

    def custom_standardization(self, input_string):
        import string
        import tensorflow.strings as tf_strings
        lowercased = tf_strings.lower(input_string)
        stripped_html = tf_strings.regex_replace(lowercased, "<br />", " ")
        return tf_strings.regex_replace(stripped_html, f"([{string.punctuation}])", r" \1")

    def prepare_lm_inputs_labels(self, text):
        import tensorflow
        text = tensorflow.expand_dims(text, -1)
        tokenized_sentences = self.vectorize_layer(text)
        x = tokenized_sentences[:, :-1]
        y = tokenized_sentences[:, 1:]
        return x, y

    @property
    def metrics(self):
        return self.model.evaluate(self.train_ds)

def causal_attention_mask(batch_size, n_dest, n_src, dtype):
    i = ops.arange(n_dest)[:, None]
    j = ops.arange(n_src)
    m = i >= j - n_src + n_dest
    mask = ops.cast(m, dtype)
    mask = ops.reshape(mask, [1, n_dest, n_src])
    mult = ops.concatenate(
        [ops.expand_dims(batch_size, -1), ops.convert_to_tensor([1, 1])], 0
    )
    return ops.tile(mask, mult)


class TransformerBlock(layers.Layer):
    # TODO had to add trainable and dtype for serializing
    def __init__(self, embed_dim, num_heads, ff_dim, rate=0.1, trainable = True, dtype = 'float32'):
        super().__init__()
        self.att = layers.MultiHeadAttention(num_heads, embed_dim)
        self.ffn = keras.Sequential(
            [
                layers.Dense(ff_dim, activation="relu"),
                layers.Dense(embed_dim),
            ]
        )
        self.layernorm1 = layers.LayerNormalization(epsilon=1e-6)
        self.layernorm2 = layers.LayerNormalization(epsilon=1e-6)
        self.dropout1 = layers.Dropout(rate)
        self.dropout2 = layers.Dropout(rate)

    def call(self, inputs):
        input_shape = ops.shape(inputs)
        batch_size = input_shape[0]
        seq_len = input_shape[1]
        causal_mask = causal_attention_mask(batch_size, seq_len, seq_len, "bool")
        attention_output = self.att(inputs, inputs, attention_mask=causal_mask)
        attention_output = self.dropout1(attention_output)
        out1 = self.layernorm1(inputs + attention_output)
        ffn_output = self.ffn(out1)
        ffn_output = self.dropout2(ffn_output)
        return self.layernorm2(out1 + ffn_output)

class TokenAndPositionEmbedding(layers.Layer):
    # TODO due to serializing
    def __init__(self, seq_len, vocab_size, embed_dim, trainable = True, dtype = 'float32'):
        super().__init__()
        self.token_emb = layers.Embedding(input_dim=vocab_size, output_dim=embed_dim)
        self.pos_emb = layers.Embedding(input_dim=seq_len, output_dim=embed_dim)

    def call(self, x):
        seq_len = ops.shape(x)[-1]
        positions = ops.arange(0, seq_len, 1)
        positions = self.pos_emb(positions)
        x = self.token_emb(x)
        return x + positions

def create_model(vocab_size):
    inputs = layers.Input(shape=(SEQ_LEN,), dtype="int32")
    embedding_layer = TokenAndPositionEmbedding(SEQ_LEN, vocab_size, embed_dim)
    x = embedding_layer(inputs)
    transformer_block = TransformerBlock(embed_dim, num_heads, feed_forward_dim)
    x = transformer_block(x)
    outputs = layers.Dense(vocab_size)(x)
    model = keras.Model(inputs=inputs, outputs=[outputs, x])
    loss_fn = keras.losses.SparseCategoricalCrossentropy(from_logits=True)
    model.compile(
        "adam",
        loss=[loss_fn, None],
    )
    return model

class TextGenerator(keras.callbacks.Callback):
    def __init__(
        self, max_tokens, start_tokens, index_to_word, md, top_k=10, print_every=1
    ):
        self.max_tokens = max_tokens
        self.start_tokens = start_tokens
        self.index_to_word = index_to_word
        self.print_every = print_every
        self.k = top_k

    def sample_from(self, logits):
        logits, indices = ops.top_k(logits, k=self.k, sorted=True)
        indices = np.asarray(indices).astype("int32")
        preds = keras.activations.softmax(ops.expand_dims(logits, 0))[0]
        preds = np.asarray(preds).astype("float32")
        return np.random.choice(indices, p=preds)

    def detokenize(self, number):
        return self.index_to_word[number]

    def on_epoch_end(self, epoch, logs=None):
        return
        start_tokens = [_ for _ in self.start_tokens]
        if (epoch + 1) % self.print_every != 0:
            return
        num_tokens_generated = 0
        tokens_generated = []
        while num_tokens_generated <= self.max_tokens:
            pad_len = SEQ_LEN - len(start_tokens)
            sample_index = len(start_tokens) - 1
            if pad_len < 0:
                x = start_tokens[:SEQ_LEN]
                sample_index = SEQ_LEN - 1
            elif pad_len > 0:
                x = start_tokens + [0] * pad_len
            else:
                x = start_tokens
            x = np.array([x])
            y, _ = self.model.predict(x, verbose=0)
            sample_token = self.sample_from(y[0][sample_index])
            tokens_generated.append(sample_token)
            start_tokens.append(sample_token)
            num_tokens_generated = len(tokens_generated)
        txt = " ".join(
            [self.detokenize(_) for _ in self.start_tokens + tokens_generated]
        )
        print(f"generated text:\n{txt}\n")

class TextGenerator2:
    def __init__(
        self, max_tokens, start_tokens, index_to_word, md, top_k=10, print_every=1
    ):
        self.max_tokens = max_tokens
        self.start_tokens = start_tokens
        self.index_to_word = index_to_word
        self.print_every = print_every
        self.k = top_k

    def sample_from(self, logits):
        logits, indices = ops.top_k(logits, k=self.k, sorted=True)
        indices = np.asarray(indices).astype("int32")
        preds = keras.activations.softmax(ops.expand_dims(logits, 0))[0]
        preds = np.asarray(preds).astype("float32")
        return np.random.choice(indices, p=preds)

    def detokenize(self, number):
        #print("det", number, len(self.index_to_word))
        return self.index_to_word[number]

    def gen(self, model):
        start_tokens = [_ for _ in self.start_tokens]
        num_tokens_generated = 0
        tokens_generated = []
        while num_tokens_generated <= self.max_tokens:
            pad_len = SEQ_LEN - len(start_tokens)
            sample_index = len(start_tokens) - 1
            if pad_len < 0:
                x = start_tokens[SEQ_LEN]
                sample_index = SEQ_LEN - 1
            elif pad_len > 0:
                x = start_tokens + [0] * pad_len
            else:
                x = start_tokens
            x = np.array([x])
            y, _ = model.predict(x, verbose=0)
            sample_token = self.sample_from(y[0][sample_index])
            tokens_generated.append(sample_token)
            start_tokens.append(sample_token)
            num_tokens_generated = len(tokens_generated)
        txt = " ".join(
            [self.detokenize(_) for _ in self.start_tokens + tokens_generated]
        )
        print(f"generated text:\n{txt}\n")
        return txt





