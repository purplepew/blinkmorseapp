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

        val p1 = getPoint(faceLandmarks[eyeIndices[0]], imageWidth, imageHeight)
        val p2 = getPoint(faceLandmarks[eyeIndices[1]], imageWidth, imageHeight)
        val p3 = getPoint(faceLandmarks[eyeIndices[2]], imageWidth, imageHeight)
        val p4 = getPoint(faceLandmarks[eyeIndices[3]], imageWidth, imageHeight)
        val p5 = getPoint(faceLandmarks[eyeIndices[4]], imageWidth, imageHeight)
        val p6 = getPoint(faceLandmarks[eyeIndices[5]], imageWidth, imageHeight)

        val vertical1 = hypot(p2.first - p6.first, p2.second - p6.second)
        val vertical2 = hypot(p3.first - p5.first, p3.second - p5.second)
        val horizontal = hypot(p1.first - p4.first, p1.second - p4.second)

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

    private fun getPoint(
        landmark: NormalizedLandmark,
        width: Int,
        height: Int
    ): Pair<Float, Float> {
        return Pair(landmark.x() * width, landmark.y() * height)
    }
}
