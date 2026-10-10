package com.example.blinkmorse

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class FaceLandmarkDetector(
    private val context: Context,
    private val listener: Listener
) {
    interface Listener {
        fun onResults(result: FaceLandmarkerResult, isFaceDetected: Boolean, ear: Float?)
        fun onError(error: String)
    }

    private var faceLandmarker: FaceLandmarker? = null
    private var lastFrameTimeMs: Long = -1L

    init {
        setupFaceLandmarker()
    }

    private fun setupFaceLandmarker() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("face_landmarker.task")
                .build()

            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinFaceDetectionConfidence(0.5f)
                .setMinFacePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setNumFaces(1)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result: FaceLandmarkerResult, inputImage: MPImage ->
                    val faceLandmarksList = result.faceLandmarks()
                    val faceDetected = faceLandmarksList.isNotEmpty()
                    val ear = if (faceDetected) {
                        val firstFace = faceLandmarksList[0]
                        EarCalculator.calculateAverageEar(
                            firstFace,
                            inputImage.width,
                            inputImage.height
                        )
                    } else null

                    listener.onResults(result, faceDetected, ear)
                }
                .setErrorListener { error: RuntimeException ->
                    listener.onError(error.message ?: "Unknown MediaPipe error")
                }
                .build()

            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            listener.onError("Failed to initialize FaceLandmarker: ${e.message}")
        }
    }

    fun detectLiveStream(imageProxy: ImageProxy) {
        val landmarker = faceLandmarker
        if (landmarker == null) {
            imageProxy.close()
            return
        }

        try {
            var frameTimeMs = SystemClock.uptimeMillis()
            if (frameTimeMs <= lastFrameTimeMs) {
                frameTimeMs = lastFrameTimeMs + 1
            }
            lastFrameTimeMs = frameTimeMs

            val bitmap = imageProxy.toBitmap()
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            val rotatedBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix().apply {
                    postRotate(rotationDegrees.toFloat())
                }
                Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
            } else {
                bitmap
            }

            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            landmarker.detectAsync(mpImage, frameTimeMs)
        } catch (e: Exception) {
            listener.onError("Error processing frame: ${e.message}")
        } finally {
            imageProxy.close()
        }
    }

    fun clear() {
        faceLandmarker?.close()
        faceLandmarker = null
    }
}
