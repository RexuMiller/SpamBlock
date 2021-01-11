import tensorflow as tf

keras_model = tf.keras.models.load_model('models/model.h5')
converter = tf.lite.TFLiteConverter.from_keras_model(keras_model)
converter.post_training_quantize = True
tflite_buffer = converter.convert()
open('android/model.tflite', 'wb').write(tflite_buffer)
