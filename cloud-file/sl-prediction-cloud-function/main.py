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

## Global variable
model = None  #model
db = None     #database-firestore
folder = '/tmp/'
image_path = None
date = None
file_name = None
value = None

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
    
    if not os.path.exists(folder):
        os.makedirs(folder)
    # Download the file to a destination
    blob.download_to_filename(folder + "model.h5")

def download_image(event, context):

    from google.cloud import storage

    file = event
    # Model Bucket details
    BUCKET_NAME        = "prototype-testing-sl-1"
    PROJECT_ID         = "white-device-312612"
    GCS_IMAGE_FILE     = file['name']

    # Initialise a client
    client   = storage.Client(PROJECT_ID)
    
    # Create a bucket object for our bucket
    bucket   = client.get_bucket(BUCKET_NAME)
    
    # Create a blob object from the filepath
    blob     = bucket.blob(GCS_IMAGE_FILE)
    
    if not os.path.exists(folder):
        os.makedirs(folder)
    # Download the file to a destination
    image_path = folder + GCS_IMAGE_FILE
    blob.download_to_filename(image_path)

    #adding function split name
    file_name = file['name']
    date = re.split("[\- | /]", file_name) #use date[-2]


def firestore_initi():

    # Use the application default credentials
    cred = credentials.ApplicationDefault()
    firebase_admin.initialize_app(cred, {
      'projectId': 'white-device-312612',
    })
    
    db = firestore.client()


def sl_predict(event, context):

    #stop when wrong file uploaded
    if '.jpg' not in file_name:
        return 0

    #deploy model locally
    global model
    if not model:
        download_model_file()
        model = tf.keras.models.load_model('/tmp/model.h5')
    
    #download image to predict
    download_image(event, context)

    #initialize firestore
    firestore_initi()

    #image process
    img = image.load_img(image_path, grayscale=True, target_size=(28, 28))
    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)

    #predict
    images = np.vstack([x])
    classes = model.predict(images, batch_size=10)
    index_max = np.argmax(classes)

    print("\n")
    print(fn)
    print(classes)
    print(index_max)
    if index_max == 0:
        value = 'A'
        print("A")
    elif index_max == 1:
        value = 'B'
        print("B")
    elif index_max == 2:
        value = 'C'
        print("C")
    elif index_max == 3:
        value = 'D'
        print("D")
    elif index_max == 4:
        value = 'E'
        print("E")
    elif index_max == 5:
        value = 'F'
        print("F")
    elif index_max == 6:
        value = 'G'
        print("G")
    elif index_max == 7:
        value = 'H'
        print("H")
    elif index_max == 8:
        value = 'I'
        print("I")
    elif index_max == 9:
        value = 'J'
        print("J")
    elif index_max == 10:
        value = 'K'
        print("K")
    elif index_max == 11:
        value = 'L'
        print("L")
    elif index_max == 12:
        value = 'M'
        print("M")
    elif index_max == 13:
        value = 'N'
        print("N")
    elif index_max == 14:
        value = 'O'
        print("O")
    elif index_max == 15:
        value = 'P'
        print("P")
    elif index_max == 16:
        value = 'Q'
        print("Q")
    elif index_max == 17:
        value = 'R'
        print("R")
    elif index_max == 18:
        value = 'S'
        print("S")
    elif index_max == 19:
        value = 'T'
        print("T")
    elif index_max == 20:
        value = 'U'
        print("U")
    elif index_max == 21:
        value = 'V'
        print("V")
    elif index_max == 22:
        value = 'W'
        print("W")
    elif index_max == 23:
        value = 'X'
        print("X")
    elif index_max == 24:
        value = 'Y'
        print("Y")
    elif index_max == 25:
        value = 'Z'
        print("Z")
    print("\n")

    #update firestore
    doc_ref = db.collection(u'sign-language').document(date)
    doc_ref.set({
        file_name: value
    }, merge=True)
