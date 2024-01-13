package com.practicalvideorecoding

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.Manifest.permission.RECORD_AUDIO
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.practicalvideorecoding.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {


    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null
    lateinit var rpl: ActivityResultLauncher<Array<String>>
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    var recording: Boolean = false
    var finalStoprecording: Boolean = true


    private var mBinding: ActivityMainBinding? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var clipDurationMs: Long = 10000 // 10 seconds
    private var outputFilePath: String = "" // Set your desired output file path here

    private var startTime: Long = 0
    private var clipNumber = 1
    val mainHandler = Handler(Looper.getMainLooper())

    var backCamera = true

    private var timeDuration = 0.0

    var minitTime = 0
    var secoundTime = 0
    var secoundTime2 = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {

                if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH_SPEED_HIGH)) {
//                    startRecording()
                    startCamera()
                } else {
                    Toast.makeText(this, "Device can't support 60FPS", Toast.LENGTH_SHORT).show()
                    // Handle case when the device does not support 60 FPS recording
                }

            } else {

            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding?.root)

        checkAndRequestPermissions()

        mBinding?.cameraCaptureButton?.setOnClickListener {
            if (!recording) {
                finalStoprecording = false
                takeVideo()
                mBinding?.coundDown?.isVisible = true

                runOnUiThread {
                    if (!finalStoprecording) {
                         Handler().postDelayed({
                             mBinding?.cameraCaptureButton?.text = "Stop Rec"
                             val timeInMilSeconds = 100
                             mBinding?.coundDown?.base = SystemClock.elapsedRealtime() - timeInMilSeconds
                            // mBinding?.coundDown?.start()
                         }, 100)



                    }
                }

            } else {
                takeVideo()
                finalStoprecording = true
//                mBinding?.coundDown?.stop()
                mBinding?.coundDown?.isVisible = false
                mBinding?.cameraCaptureButton?.text = "Start Rec"
            }

//            startRecording()
        }

        /* mBinding?.imgSwipe?.setOnClickListener {
             if (backCamera){
                 backCamera = false
             }else{
                 backCamera = true
             }
         }*/


    }

    private fun checkAndRequestPermissions() {

        var arraylist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(CAMERA, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(CAMERA, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, RECORD_AUDIO)
        } else {
            arrayOf(CAMERA, READ_EXTERNAL_STORAGE, RECORD_AUDIO)
        }

        val permissions = arraylist
        val permissionsToRequest = ArrayList<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // All permissions already granted, start recording
            // Check if the device supports 60 FPS recording
            if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH_SPEED_HIGH)) {
                // Start recording at 60 FPS
                val contentResolver: ContentResolver = contentResolver
                //  outputFilePath = createOutputFilePath(contentResolver)
//                startRecording()
                startCamera()
            } else {
                Toast.makeText(this, "Device can't support 60FPS", Toast.LENGTH_SHORT).show()
                // Handle case when the device does not support 60 FPS recording
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider = cameraProviderFuture.get() as ProcessCameraProvider
                    val preview = Preview.Builder().apply {
                        setTargetResolution(Size(1080, 1920))
                    }.build()
                    preview.setSurfaceProvider(mBinding?.viewFinder?.surfaceProvider)
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    val recorder = Recorder.Builder()
                        .setQualitySelector(
                            QualitySelector.from(
                                Quality.HIGHEST,
                                FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                            )
                        )
                        .build()
                    videoCapture = VideoCapture.withOutput(recorder)
                    val imageCatpure = ImageCapture.Builder().build()
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        this@MainActivity, cameraSelector, preview, imageCatpure, videoCapture
                    )
                } catch (e: Exception) {
                    Log.e("TAG", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(this)
        )
    }

    private fun takeVideo() {
        Log.d("TAG", "start")
        // Get a stable reference of the modifiable image capture use case
        val videoCapture = videoCapture ?: return

        if (recording) {
            //ie already started.
            Log.wtf("TAG", "StopVideo")

            currentRecording?.stop()
            recording = false
            //    mBinding?.cameraCaptureButton?.text = "Start Rec"
        } else {
            Log.wtf("TAG", "StartVideo")
            val name = "CameraX-" + SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(System.currentTimeMillis()) + ".mp4"
            val cv = ContentValues()
            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            cv.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                cv.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }

            val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
                contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            ).setContentValues(cv).build()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            currentRecording = videoCapture.output
                .prepareRecording(this@MainActivity, mediaStoreOutputOptions)
                .withAudioEnabled()
                .start(
                    cameraExecutor
                ) { videoRecordEvent ->



                    val duration = TimeUnit.SECONDS.convert(
                        videoRecordEvent.recordingStats.recordedDurationNanos,
                        TimeUnit.NANOSECONDS
                    )
                    val minittime = TimeUnit.MINUTES.convert(
                        videoRecordEvent.recordingStats.recordedDurationNanos,
                        TimeUnit.NANOSECONDS
                    )
                    runOnUiThread {


                        println("olde rSecound  :- "+duration.toInt())
                        if (secoundTime2 == duration.toInt()){

                        }else{

                              secoundTime++
                            if (secoundTime > 59) {
                                minitTime++
                                secoundTime = 0
                            }

                            println("SEcound :- "+secoundTime.toInt())

                            secoundTime2 = duration.toInt()
                        }
                        mBinding?.coundDown?.text = minitTime.toInt().toString()+":"+secoundTime.toInt()
                    }

                    if (duration.toInt() == 9) {
                        runOnUiThread {
                            currentRecording?.stop()
                            recording = false
                        }
                    }
                    if (videoRecordEvent is VideoRecordEvent.Finalize) {
                        saveVideoClip(startTime, 0, videoRecordEvent)
                        if (!finalStoprecording) {
                            takeVideo()
                        }
                    }
                }

            recording = true
            //    mBinding?.cameraCaptureButton?.text = "Stop Rec"

        }
    }


    private fun saveVideoClip(
        startTime: Long,
        endTime: Long,
        videoRecordEvent: VideoRecordEvent.Finalize
    ) {
        val savedUri = videoRecordEvent.outputResults.outputUri
        //convert uri to useful name.

        var cursor: Cursor? = null
        var path: String
        try {
            cursor = contentResolver.query(
                savedUri,
                arrayOf(MediaStore.MediaColumns.DATA),
                null,
                null,
                null
            )
            cursor!!.moveToFirst()
            path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
        } finally {
            cursor!!.close()
        }
        Log.wtf("TAG", path)
        if (path == "") {
            path = savedUri.toString()
        }
        val msg = "Video capture succeeded: $path"
        println("save video $msg")
        runOnUiThread {
            Toast.makeText(
                baseContext,
                msg,
                Toast.LENGTH_LONG
            ).show()
        }
        Log.d("TAG", msg)


    }


    override fun onDestroy() {
        super.onDestroy()
        //  stopRecording()
    }


}