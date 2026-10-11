package com.example.blinkmorse

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.hypot

object EarCalculator {

    val LEFT_EYE_INDICES = intArrayOf(33, 160, 158, 133, 153, 144)
    val RIGHT_EYE_INDICES = intArrayOf(362, 385, 384, 263, 373, 380)

    fun calculateEyeEar(
        faceLandmarks: List<NormalizedLandmark>,
        eyeIndices: IntArray,
        imageWidth: Int,
        imageHeight: Int
    ): Float {
        val maxIndex = eyeIndices.maxOrNull() ?: return 0f
        if (faceLandmarks.size <= maxIndex) return 0f

        val p1x = faceLandmarks[eyeIndices[0]].x() * imageWidth
        val p1y = faceLandmarks[eyeIndices[0]].y() * imageHeight
        
        val p2x = faceLandmarks[eyeIndices[1]].x() * imageWidth
        val p2y = faceLandmarks[eyeIndices[1]].y() * imageHeight
        
        val p3x = faceLandmarks[eyeIndices[2]].x() * imageWidth
        val p3y = faceLandmarks[eyeIndices[2]].y() * imageHeight
        
        val p4x = faceLandmarks[eyeIndices[3]].x() * imageWidth
        val p4y = faceLandmarks[eyeIndices[3]].y() * imageHeight
        
        val p5x = faceLandmarks[eyeIndices[4]].x() * imageWidth
        val p5y = faceLandmarks[eyeIndices[4]].y() * imageHeight
        
        val p6x = faceLandmarks[eyeIndices[5]].x() * imageWidth
        val p6y = faceLandmarks[eyeIndices[5]].y() * imageHeight

        val vertical1 = hypot(p2x - p6x, p2y - p6y)
        val vertical2 = hypot(p3x - p5x, p3y - p5y)
        val horizontal = hypot(p1x - p4x, p1y - p4y)

        if (horizontal == 0f) return 0f

        return (vertical1 + vertical2) / (2.0f * horizontal)
    }

    fun calculateAverageEar(
        faceLandmarks: List<NormalizedLandmark>,
        imageWidth: Int,
        imageHeight: Int
    ): Float {
        val leftEar = calculateEyeEar(faceLandmarks, LEFT_EYE_INDICES, imageWidth, imageHeight)
        val rightEar = calculateEyeEar(faceLandmarks, RIGHT_EYE_INDICES, imageWidth, imageHeight)
        return (leftEar + rightEar) / 2.0f
    }

}
