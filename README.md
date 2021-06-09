# Sign Language Repository
This repository is for Bangkit Capstone Project. We make an application using machine learning for Sign Language Translation. The goal of this project is we want to make communication with someone using sign language better and make things easier.

# Documentation
Here is our documentation.
All used files are on the above folder.

# Machine Learning
1. Download the dataset, then perfom reading on the Notebook (colaboratory)
2. Make sure the dataset is divided into two, training data labels and validation data labels
3. Using ImageDataGenerator, prepare the dataset pipeline and perform image augmentation
4. Create a sequential model of Convolution Neural Network
5. Compile the model, so that it is ready for being trained and validated
6. Train and validate the model
7. Save the model in hdf5 format

# Mobile Development
1. Implement CameraX
2. Add camera request permission
3. Implement Firebase to use Cloud Storage for media storage and Cloud Firestore to contain predictions output
4. Add wifi request permission and internet access
5. Move the implementation of firebase to view model

# Cloud Computing
1. Make Google Cloud Storage Bucket to store image input from Android
2. Enable Google Cloud Firestore to store output data from Machine Learning model
3. Make Google Cloud Function to deploy a Serverless Machine Learning
4. Test Cloud Function, manually upload image to GCS Bucket
5. Make a monitoring dashboard to check activity of Cloud Function
