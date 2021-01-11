from tensorflow.keras import models, optimizers, losses, activations
from tensorflow.keras.layers import *
import tensorflow as tf
import time


class Classifier(object):

    def __init__(self, number_of_class,maxlen):
        dropout_rate = 0.5
        input_shape = (maxlen,)
        target_shape = (maxlen, 1)
        self.model_scheme = [
            Reshape(input_shape=input_shape, target_shape=target_shape),
            Conv1D(128, kernel_size=2, strides=1, activation=activations.relu, kernel_regularizer='l1'),
            MaxPooling1D(pool_size=2),
            Flatten(),
            Dense(64, activation=activations.relu),
            BatchNormalization(),
            Dropout(dropout_rate),
            Dense(number_of_class, activation=tf.nn.softmax)
        ]
        self.__model = tf.keras.Sequential(self.model_scheme)
        self.__model.compile(
            optimizer=optimizers.Adam(lr=0.0001),
            loss=losses.categorical_crossentropy,
            metrics=['accuracy'],
        )

    def fit(self, x, y, hyper_parameters):
        initial_time = time.time()
        self.__model.fit(x, y,
                         batch_size=hyper_parameters['batch_size'],
                         epochs=hyper_parameters['epochs'],
                         callbacks=hyper_parameters['callbacks'],
                         validation_data=hyper_parameters['val_data'])
        final_time = time.time()
        eta = (final_time - initial_time)
        time_unit = 'seconds'
        if eta >= 60:
            eta = eta / 60
            time_unit = 'minutes'
        self.__model.summary()
        print('Elapsed time acquired for {} epoch(s) -> {} {}'.format(hyper_parameters['epochs'], eta, time_unit))

    def evaluate(self, test_x, test_y):
        return self.__model.evaluate(test_x, test_y)

    def predict(self, x):
        predictions = self.__model.predict(x)
        return predictions

    def save_model(self, file_path):
        self.__model.save(file_path)

    def load_model(self, file_path):
        self.__model = models.load_model(file_path)
