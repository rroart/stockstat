import keras_nlp
import keras
import tensorflow as tf
import time

keras.mixed_precision.set_global_policy("mixed_float16")

# TODO not from preset
class Model:
    def __init__(self, myobj, config, md):
        self.myobj = myobj
        self.config = config
        self.md = md
        if isinstance(myobj.dataset, list):
            preset = myobj.dataset[0]
        else:
            preset = None

        if preset is not None:
            preprocessor = keras_nlp.models.GPT2CausalLMPreprocessor.from_preset(
                preset,
                sequence_length=128,
            )
            self.model = keras_nlp.models.GPT2CausalLM.from_preset(
                preset, preprocessor=preprocessor
            )
        else:
            vocab = md.vocab
            vocab = dict([(str(token), i) for i, token in enumerate(vocab)])
            tokenizer = keras_nlp.models.GPT2Tokenizer(
                vocabulary=vocab,
                merges=[],
            )
            preprocessor = keras_nlp.models.GPT2CausalLMPreprocessor(
                tokenizer=tokenizer,
                sequence_length=128,
            )
            backbone = keras_nlp.models.GPT2Backbone(
                vocabulary_size=md.vocab_size,
                num_layers=4,
                num_heads=4,
                hidden_dim=256,
                intermediate_dim=512,
                max_sequence_length=128,
            )
            self.model = keras_nlp.models.GPT2CausalLM(
                backbone=backbone,
                preprocessor=preprocessor,
            )

    def fit(self, train_ds, val_ds, test_ds):
        learning_rate = keras.optimizers.schedules.PolynomialDecay(
            5e-5,
            decay_steps=train_ds.cardinality() * self.config.steps,
            end_learning_rate=0.0,
        )
        loss = keras.losses.SparseCategoricalCrossentropy(from_logits=True)
        self.model.compile(
            optimizer=keras.optimizers.Adam(learning_rate),
            loss=loss,
            weighted_metrics=["accuracy"],
        )

        self.model.fit(train_ds, epochs=self.config.steps)


    def generate(self, model):
        #self.model.compile(sampler="top_k")
        start_prompt = self.myobj.classifyarray[0]
        return self.model.generate(start_prompt, max_length=self.myobj.classes)

    def localsave(self):
       return True

    def save(self, filename):
        print("type", type(self.model))
        print(filename)
        self.model.save(filename)


