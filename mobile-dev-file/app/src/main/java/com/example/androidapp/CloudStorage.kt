package com.example.androidapp

import android.util.Log
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.storage.StorageScopes
import com.google.api.services.storage.model.Bucket
import com.google.api.services.storage.model.StorageObject
import java.io.*
import java.net.URLConnection
import java.util.*

class CloudStorage {
    companion object{
        private var properties: Properties? = null
        private var storage: Storage? = null

        private val PROJECT_ID_PROPERTY = "0000000001"
        private val APPLICATION_NAME_PROPERTY = "Sign Language Recognition"
        private val ACCOUNT_ID_PROPERTY = "akunnya Ardli"
        private val PRIVATE_KEY_PATH_PROPERTY = "private.key.path"

        @Throws(Exception::class)
        fun uploadFile(bucketName: String?, filePath: String?) {
            val storage: Storage? = getStorage()
            val sObj = StorageObject()
            sObj.bucket = bucketName
            val file = File(filePath)
            val stream: InputStream = FileInputStream(file)
            try {
                val contentType: String = URLConnection
                    .guessContentTypeFromStream(stream)
                val content = InputStreamContent(
                    contentType,
                    stream
                )
                val insert = storage?.objects()?.insert(
                    bucketName, null, content
                )
                insert?.name = file.name
                insert?.execute()
            } finally {
                stream.close()
            }
        }

        @Throws(java.lang.Exception::class)
        fun downloadFile(bucketName: String?, fileName: String, destinationDirectory: String?) {
            val directory = File(destinationDirectory)
            if (!directory.isDirectory) {
                throw java.lang.Exception("Provided destinationDirectory path is not a directory")
            }
            val file = File(directory.absolutePath + "/" + fileName)
            Log.d("Absolute", file.toString())
            val storage: Storage? = getStorage()
            val get = storage?.objects()?.get(bucketName, fileName)
            val stream = FileOutputStream(file)
            try {
                get?.executeAndDownloadTo(stream)
            } finally {
                stream.close()
            }
        }

        @Throws(java.lang.Exception::class)
        fun createBucket(bucketName: String?) {
            val storage: Storage? = getStorage()
            val bucket = Bucket()
            bucket.name = bucketName
            storage?.buckets()?.insert(
                getProperties().getProperty(PROJECT_ID_PROPERTY), bucket
            )?.execute()
        }

        @Throws(java.lang.Exception::class)
        private fun getProperties(): Properties {
            if (properties == null) {
                properties = Properties()
                val stream = CloudStorage::class.java
                    .getResourceAsStream("/cloudstorage.properties")
                try {
                    properties!!.load(stream)
                } catch (e: IOException) {
                    throw RuntimeException(
                        "cloudstorage.properties must be present in classpath",
                        e
                    )
                } finally {
                    stream.close()
                }
            }
            return properties as Properties
        }

        @Throws(java.lang.Exception::class)
        private fun getStorage(): Storage? {
            if (storage == null) {
                val httpTransport: HttpTransport = NetHttpTransport()
                val jsonFactory: JsonFactory = JacksonFactory()
                val scopes: MutableList<String> = ArrayList()
                scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL)
                val credential: Credential = GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(
                        getProperties().getProperty(ACCOUNT_ID_PROPERTY)
                    )
                    .setServiceAccountPrivateKeyFromP12File(
                        File(
                            getProperties().getProperty(
                                PRIVATE_KEY_PATH_PROPERTY
                            )
                        )
                    )
                    .setServiceAccountScopes(scopes).build()
                storage = Storage.Builder(
                    httpTransport, jsonFactory,
                    credential
                ).setApplicationName(
                    getProperties().getProperty(APPLICATION_NAME_PROPERTY)
                )
                    .build()
            }
            return storage
        }
    }
}