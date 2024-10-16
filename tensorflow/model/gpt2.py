import keras_nlp
import keras
import tensorflow as tf
import time

keras.mixed_precision.set_global_policy("mixed_float16")

class Model:
    def __init__(self, myobj, config, md):
        self.myobj = myobj
        self.config = config
        self.md = md

        preprocessor = keras_nlp.models.GPT2CausalLMPreprocessor.from_preset(
            "gpt2_base_en",
            sequence_length=128,
        )
        self.model = keras_nlp.models.GPT2CausalLM.from_preset(
            "gpt2_base_en", preprocessor=preprocessor
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
        return self.model.generate(start_prompt, max_length=200)

    def localsave(self):
       return True

    def save(self, filename):
        print("type", type(self.model))
        print(filename)
        self.model.save(filename)


