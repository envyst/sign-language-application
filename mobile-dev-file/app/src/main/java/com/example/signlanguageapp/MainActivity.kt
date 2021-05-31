package com.example.signlanguageapp

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ExifInterface
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.Formatter
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.signlanguageapp.databinding.ActivityMainBinding
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null

    private lateinit var binding: ActivityMainBinding
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var ipAdd: String

    //Firebase
    private lateinit var storageRef: StorageReference
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar?.setDisplayShowHomeEnabled(false)

        storageRef = FirebaseStorage.getInstance().reference

        val wm: WifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        ipAdd = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        Log.d("Get IP", ipAdd)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listener for take photo
        binding.btnCapture.setOnClickListener { takePhoto() }
        
//        binding.switchCamera.setOnClickListener { swapCamera() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun takePhoto() {
        val randomNumber = (10000000..99999999).random()
        val fieldFormat = SimpleDateFormat(FILENAME_FORMAT, Locale.ROOT
        ).format(System.currentTimeMillis()) + "-" + randomNumber.toString()

        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(outputDirectory, "$fieldFormat.jpg")
        Log.d("FileCapture", photoFile.toString())


        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d("File Uri", savedUri.toString())

                    try {
                        val bitmap = BitmapFactory.decodeFile(savedUri.path)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, FileOutputStream(photoFile))
                        Log.d("File Compressed", photoFile.toString())
                    }catch (e: Throwable){
                        Log.e("Error Compressing", "File compressing error", e)
                    }

                    val riversRef: StorageReference = storageRef.child("$fieldFormat.jpg")
                    riversRef.putFile(savedUri)
                        .addOnSuccessListener { uri ->
                            val downloadUrl: UploadTask.TaskSnapshot? = uri
                            Log.d("Image on Storage", "URL: $downloadUrl")
                            Toast.makeText(baseContext, "Upload success!", Toast.LENGTH_SHORT)
                                .show();
                        }
                        .addOnFailureListener{
                            Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                        }

                    val msg = "Photo capture succeeded: $savedUri"
                    Log.d(TAG, msg)
                }
            })

//        Handler(Looper.getMainLooper()).postDelayed({
//            db.collection("sign-language")
//                .document(SimpleDateFormat(FILENAME_FORMAT, Locale.ROOT).format(System.currentTimeMillis()))
//                .get()
//                .addOnCompleteListener {
//                    if(it.isSuccessful){
//                        val doc = it.result
//
//                        if(doc?.exists() == true){
//                            textResult = doc.get(fieldFormat)
//                            binding.txtResult.text = textResult.toString()
//                            Log.d("Read Firestore", textResult.toString())
//                            Log.d("Field", fieldFormat)
//                            Log.d("Document Result", doc.toString())
//                        }
//                    }
//                }
//                .addOnFailureListener {
//                    Log.e("Error Read", "Error read data from firestore", it)
//                }
//        }, 3200)

        db.collection("sign-language")
            .addSnapshotListener { value, error ->
            if(error != null){
                Log.e("Snapshot Error", "Error snapshot", error)
            }

            for (dc: DocumentChange in value?.documentChanges!!){
                when(dc.type){
                    DocumentChange.Type.MODIFIED -> {
                        binding.txtResult.text = dc.document.get(fieldFormat).toString()
                        Log.d("Field", fieldFormat)
                        Log.d("Firestore changed", dc.document.get(fieldFormat).toString())
                    }
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun rotatedImage(bitmap: Bitmap, angle: Float): Bitmap{
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun convertUriToBitmap(imageUri: Uri): Bitmap?{
        contentResolver.notifyChange(imageUri, null)
        val cr: ContentResolver = contentResolver

        return try {
            android.provider.MediaStore.Images.Media.getBitmap(cr, imageUri)
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }

//    private fun swapCamera() {
//        var camFacing = CameraCharacteristics.LENS_FACING_BACK
//        camFacing = if(camFacing == CameraCharacteristics.LENS_FACING_BACK){
//            CameraCharacteristics.LENS_FACING_FRONT
//        } else{
//            CameraCharacteristics.LENS_FACING_BACK
//        }
//        closeCamera()
//        connectCamera()
//    }
//
//    private fun connectCamera() {
//        val cameraManager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
//        val cameraID = cameraManager.cameraIdList
//
//        try{
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
//                    cameraManager.openCamera(cameraID, cameraCallback, bgHandler)
//                } else{
//                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
//                        Toast.makeText(this, "Required access to use camera", Toast.LENGTH_SHORT).show()
//                    }
//                    requestPermissions(
//                        arrayOf(
//                            Manifest.permission.CAMERA,
//                            Manifest.permission.RECORD_AUDIO
//                        ), REQUEST_CAMERA_PERMISSION)
//                }
//            } else{
//                cameraManager.openCamera(cameraID, cameraCallback, bgHandler)
//            }
//        } catch (e: CameraAccessException){
//            e.printStackTrace()
//        }
//    }
//
//    private fun closeCamera() {
//        if(deviceCamera != null){
//            cameraDevice.close()
//            cameraDevice = null
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "ddMMyyyy"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}