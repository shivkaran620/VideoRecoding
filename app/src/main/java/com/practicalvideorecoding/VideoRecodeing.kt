package com.practicalvideorecoding

import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class VideoRecordingActivity : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var clipDurationMs: Long = 10000 // 10 seconds
    private var outputFilePath: String = "" // Set your desired output file path here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_video_recording)

        // Check if the device supports 60 FPS recording
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH_SPEED_HIGH)) {
            // Start recording at 60 FPS
            startRecording()
        } else {
            // Handle case when the device does not support 60 FPS recording
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.CAMERA)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT)
            setVideoFrameRate(60)
            setVideoSize(1280, 720) // Set your desired video size here
            setOutputFile(outputFilePath)
            setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording()
                }
            }
            setMaxDuration(clipDurationMs.toInt())
            try {
                prepare()
                start()

                // Stop recording after clipDurationMs milliseconds
                Handler().postDelayed({
                    if (isRecording) {
                        stopRecording()
                    }
                }, clipDurationMs)
                isRecording = true
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            if (isRecording) {
                stop()
                release()
                isRecording = false
            }
        }
        mediaRecorder = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}