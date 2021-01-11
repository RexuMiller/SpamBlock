import numpy as np
from Model import Classifier

from Model import Classifier
X = np.load('ProcData/x.npy')
Y = np.load('ProcData/y.npy')
test_X = np.load('ProcData/test_x.npy')
test_Y = np.load('ProcData/test_y.npy')

print(X.shape)
print(Y.shape)
print(test_X.shape)
print(test_Y.shape)

classifier = Classifier(number_of_class=2, maxlen=171)
parameters = {
    'batch_size': 100,
    'epochs': 1,
    'callbacks': None,
    'val_data': (test_X, test_Y)
}
classifier.fit(X, Y, parameters)
classifier.save_model('models/model.h5')

loss, accuracy = classifier.evaluate(test_X, test_Y)
print("Loss of {}".format(loss), "Accuracy of {} %".format(accuracy * 100))
