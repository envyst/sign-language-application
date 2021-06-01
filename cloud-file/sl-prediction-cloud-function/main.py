import csv
import os
import re
import numpy as np
import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from keras.preprocessing import image
import cv2
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import PIL.Image

## Global variable
model = None  #model
db = None
image_path = ""
datee = ""
file_nama = ""
value = ""

# Download model file from cloud storage bucket
def download_model_file():

    from google.cloud import storage

    # Model Bucket details
    BUCKET_NAME        = "sl-ml-file-000"
    PROJECT_ID         = "white-device-312612"
    GCS_MODEL_FILE     = "sl-model-prototype-v1.h5"

    # Initialise a client
    client   = storage.Client(PROJECT_ID)
    
    # Create a bucket object for our bucket
    bucket   = client.get_bucket(BUCKET_NAME)
    
    # Create a blob object from the filepath
    blob     = bucket.blob(GCS_MODEL_FILE)
    
    folder = '/tmp/'
    if not os.path.exists(folder):
        os.makedirs(folder)
    # Download the file to a destination
    blob.download_to_filename(folder + "model.h5")

def download_image(event, context):

    from google.cloud import storage

    file = event
    # Model Bucket details
    BUCKET_NAME        = "white-device-312612.appspot.com"
    PROJECT_ID         = "white-device-312612"
    GCS_IMAGE_FILE     = file['name']

    # Initialise a client
    client   = storage.Client(PROJECT_ID)
    
    # Create a bucket object for our bucket
    bucket   = client.get_bucket(BUCKET_NAME)
    
    # Create a blob object from the filepath
    blob     = bucket.blob(GCS_IMAGE_FILE)
    
    folder = '/tmp/'
    if not os.path.exists(folder):
        os.makedirs(folder)
    # Download the file to a destination
    global image_path
    image_path = folder + GCS_IMAGE_FILE
    blob.download_to_filename(image_path)

    #adding function split name
    global file_nama
    global datee
    file_nama = file['name']
    datee = file_nama.split('-') #use datee[-2]


def sl_predict(event, context):
    
    #download image to predict
    download_image(event, context)

    #stop when wrong file uploaded
    if '.jpg' not in file_nama:
        return 0

    #deploy model locally
    global model
    if not model:
        download_model_file()
        model = tf.keras.models.load_model('/tmp/model.h5')
    
    
    #initialize firestore
    global db
    if not db:
        # Use the application default credentials
        cred = credentials.ApplicationDefault()
        firebase_admin.initialize_app(cred, {
            'projectId': 'white-device-312612',
        })
    
    db = firestore.client()

    #image process
    img = image.load_img(image_path, grayscale=True, target_size=(28, 28))
    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)

    #predict
    images = np.vstack([x])
    classes = model.predict(images, batch_size=10)
    index_max = np.argmax(classes)
    global value

    if index_max == 0:
        value = 'A'
    elif index_max == 1:
        value = 'B'
    elif index_max == 2:
        value = 'C'
    elif index_max == 3:
        value = 'D'
    elif index_max == 4:
        value = 'E'
    elif index_max == 5:
        value = 'F'
    elif index_max == 6:
        value = 'G'
    elif index_max == 7:
        value = 'H'
    elif index_max == 8:
        value = 'I'
    elif index_max == 9:
        value = 'J'
    elif index_max == 10:
        value = 'K'
    elif index_max == 11:
        value = 'L'
    elif index_max == 12:
        value = 'M'
    elif index_max == 13:
        value = 'N'
    elif index_max == 14:
        value = 'O'
    elif index_max == 15:
        value = 'P'
    elif index_max == 16:
        value = 'Q'
    elif index_max == 17:
        value = 'R'
    elif index_max == 18:
        value = 'S'
    elif index_max == 19:
        value = 'T'
    elif index_max == 20:
        value = 'U'
    elif index_max == 21:
        value = 'V'
    elif index_max == 22:
        value = 'W'
    elif index_max == 23:
        value = 'X'
    elif index_max == 24:
        value = 'Y'
    elif index_max == 25:
        value = 'Z'

    #update firestore
    doc_ref = db.collection(u'sign-language').document(datee[-2])
    doc_ref.set({
        file_nama.strip('.jpg'): value
    }, merge=True)
    print(file_nama + '\n')
    print(datee[-2] + '\n')
    print(value + '\n')
