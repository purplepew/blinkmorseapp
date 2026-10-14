package com.example.blinkmorse

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.blinkmorse.ui.theme.BlinkMorseTheme
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors

const val TEST_1_TARGET_LENGTH = 59

enum class AppState {
    CALIBRATING_SHORT,
    CALIBRATING_LONG,
    CALIBRATION_FAILED,
    READY_TYPING
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlinkMorseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black // Enforce Dark Theme
                ) {
                    CameraScreen()
                }
            }
        }
    }
}

// Extension function to show the BottomSheetDialog with the XML layout
fun showCheatSheetBottomSheet(context: Context) {
    val bottomSheetDialog = BottomSheetDialog(context)
    val view = LayoutInflater.from(context).inflate(R.layout.layout_cheat_sheet, null)

    // Remove background dimming so the live buffer and camera remain visible behind it
    bottomSheetDialog.window?.setDimAmount(0f)

    // Setup Close Button
    view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
        bottomSheetDialog.dismiss()
    }

    // Populate Alphabet Grid (4 columns)
    val gridAlphabet = view.findViewById<GridLayout>(R.id.gridAlphabet)
    val alphabetMap = mapOf(
        "A" to ".-", "B" to "-...", "C" to "-.-.", "D" to "-..", "E" to ".",
        "F" to "..-.", "G" to "--.", "H" to "....", "I" to "..", "J" to ".---",
        "K" to "-.-", "L" to ".-..", "M" to "--", "N" to "-.", "O" to "---",
        "P" to ".--.", "Q" to "--.-", "R" to ".-.", "S" to "...", "T" to "-",
        "U" to "..-", "V" to "...-", "W" to ".--", "X" to "-..-", "Y" to "-.--", "Z" to "--.."
    )
    alphabetMap.forEach { (letter, code) ->
        val tv = TextView(context).apply {
            text = String.format(Locale.US, "%s: %s", letter, code)
            setTextColor(AndroidColor.WHITE)
            textSize = 14f // Condensed text size
            setPadding(0, 4, 8, 4) // Condensed padding
        }
        val params = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        )
        gridAlphabet.addView(tv, params)
    }

    // Populate Numbers Grid (5 columns)
    val gridNumbers = view.findViewById<GridLayout>(R.id.gridNumbers)
    val numbersMap = mapOf(
        "1" to ".----", "2" to "..---", "3" to "...--", "4" to "....-", "5" to ".....",
        "6" to "-....", "7" to "--...", "8" to "---..", "9" to "----.", "0" to "-----"
    )
    numbersMap.forEach { (number, code) ->
        val tv = TextView(context).apply {
            text = String.format(Locale.US, "%s: %s", number, code)
            setTextColor(AndroidColor.WHITE)
            textSize = 14f // Condensed text size
            setPadding(0, 4, 8, 4) // Condensed padding
        }
        val params = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        )
        gridNumbers.addView(tv, params)
    }

    bottomSheetDialog.setContentView(view)

    // Limit Height to max 45% of screen
    bottomSheetDialog.setOnShowListener { dialog ->
        val d = dialog as BottomSheetDialog
        val bottomSheet = d.findViewById<android.view.View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            behavior.maxHeight = (screenHeight * 0.45).toInt()
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
        }
    }

    bottomSheetDialog.show()
}

fun showDataManagerBottomSheet(context: Context) {
    val bottomSheetDialog = BottomSheetDialog(context)
    val view = LayoutInflater.from(context).inflate(R.layout.layout_data_manager, null)
    bottomSheetDialog.setContentView(view)

    // Ensure camera background is visible
    bottomSheetDialog.window?.setDimAmount(0f)

    val cbSelectAll = view.findViewById<CheckBox>(R.id.cbSelectAll)
    val btnShareSelected = view.findViewById<android.widget.Button>(R.id.btnShareSelected)
    val btnDeleteSelected = view.findViewById<android.widget.Button>(R.id.btnDeleteSelected)
    val llFileList = view.findViewById<LinearLayout>(R.id.llFileList)
    val btnClose = view.findViewById<ImageButton>(R.id.btnCloseManager)

    btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

    fun loadFiles() {
        llFileList.removeAllViews()
        val files = context.filesDir.listFiles { _, name -> name.endsWith(".csv") }
            ?.sortedBy { it.name } ?: emptyList()

        val checkBoxes = mutableListOf<CheckBox>()
        val fileMap = mutableMapOf<CheckBox, File>()

        for (file in files) {
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 24, 0, 24) // Added vertical padding for table spacing
            }

            val cb = CheckBox(context).apply {
                buttonTintList = android.content.res.ColorStateList.valueOf(AndroidColor.WHITE)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = 16
                }
            }
            
            // Add custom document icon next to checkbox
            val iconView = android.widget.ImageView(context).apply {
                setImageResource(R.drawable.ic_test_data)
                layoutParams = LinearLayout.LayoutParams(64, 64).apply {
                    marginEnd = 32
                }
                setColorFilter(AndroidColor.WHITE)
            }

            // File Name Text
            val tvFileName = TextView(context).apply {
                text = file.name
                setTextColor(AndroidColor.WHITE)
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            checkBoxes.add(cb)
            fileMap[cb] = file

            val btnShare = ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_share)
                setBackgroundColor(AndroidColor.TRANSPARENT)
                setColorFilter(AndroidColor.WHITE)
                setPadding(16, 16, 16, 16)
                setOnClickListener {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Data"))
                }
            }

            row.addView(cb)
            row.addView(iconView)
            row.addView(tvFileName)
            row.addView(btnShare)
            llFileList.addView(row)
        }

        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            checkBoxes.forEach { it.isChecked = isChecked }
        }

        btnShareSelected.setOnClickListener {
            val selectedFiles = checkBoxes.filter { it.isChecked }.mapNotNull { fileMap[it] }
            if (selectedFiles.isNotEmpty()) {
                val uris = selectedFiles.map {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
                }
                val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "text/csv"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Selected Data"))
            } else {
                Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
            }
        }

        btnDeleteSelected.setOnClickListener {
            val selectedFiles = checkBoxes.filter { it.isChecked }.mapNotNull { fileMap[it] }
            if (selectedFiles.isNotEmpty()) {
                selectedFiles.forEach { it.delete() }
                cbSelectAll.isChecked = false
                loadFiles() // Refresh list
                Toast.makeText(context, "Deleted ${selectedFiles.size} files", Toast.LENGTH_SHORT).show()
            }
        }
    }

    loadFiles()

    bottomSheetDialog.setOnShowListener { dialog ->
        val d = dialog as BottomSheetDialog
        val bottomSheet = d.findViewById<android.view.View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            behavior.maxHeight = (screenHeight * 0.60).toInt()
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
        }
    }

    bottomSheetDialog.show()
}

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        BlinkMorseUI()
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Request Camera Permission", color = Color.White)
            }
        }
    }
}

@Composable
fun BlinkMorseUI() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var appState by remember { mutableStateOf(AppState.CALIBRATING_SHORT) }
    var promptMessage by remember { mutableStateOf("Perform 10 short blinks (dots) [0/10]") }

    var shortCalibCount by remember { mutableIntStateOf(0) }
    var longCalibCount by remember { mutableIntStateOf(0) }

    var currentSymbols by remember { mutableStateOf("") }
    var decodedText by remember { mutableStateOf("") }
    var thresholdInfo by remember { mutableStateOf("Threshold: --- | Short: --- | Long: ---") }
    var faceDetectedState by remember { mutableStateOf<Boolean?>(null) }
    var isTest1Active by remember { mutableStateOf(false) }
    var currentFps by remember { mutableIntStateOf(0) }
    
    // UI Animation State
    var isFlashing by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val blinkDetector = remember { BlinkDetector() }
    val adaptiveThreshold = remember { AdaptiveThreshold() }
    val sessionLogger = remember { SessionLogger() }
    
    val sharedPrefs = context.getSharedPreferences("blinkmorse_prefs", Context.MODE_PRIVATE)
    var hasSavedCalibration by remember { mutableStateOf(adaptiveThreshold.hasSavedCalibration(sharedPrefs)) }
    
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
            }
        }
        tts = textToSpeech

        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }
    
    // Helper to transition UI between CALIBRATING and READY phases
    fun updateUIState(newState: AppState, message: String) {
        appState = newState
        promptMessage = message
    }

    val morseDecoder = remember {
        MorseDecoder(
            coroutineScope = coroutineScope,
            onSymbolUpdated = { currentSymbols = it },
            onAbort = {
                adaptiveThreshold.clearStagedBlinks()
                coroutineScope.launch(Dispatchers.Default) {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300) // Distinct error tone
                }
                Log.d("BlinkMorse", "Panic flutter detected! Aborting character.")
            }
        ) { decodedChar ->
            if (isTest1Active) {
                // STRICT EVALUATION MODE
                // Force learning from all garbage to capture raw fatigue drift
                adaptiveThreshold.commitStagedBlinks() 
                
                // Disable prosigns. Treat invalid strings ("") or disabled commands as "*"
                val isGarbageOrCommand = decodedChar.isEmpty() || decodedChar == "<BS>" || decodedChar == "<SPEAK>"
                val charToAppend = if (isGarbageOrCommand) "*" else decodedChar
                
                decodedText += charToAppend
                sessionLogger.attachDecodedChar(charToAppend)

                coroutineScope.launch(Dispatchers.Default) {
                    if (isGarbageOrCommand) {
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 150)
                    } else {
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                    }
                }

                // Auto-finish Test 1 using the strict length limit
                if (decodedText.length >= TEST_1_TARGET_LENGTH) {
                    coroutineScope.launch(Dispatchers.Main) {
                        val fileName = sessionLogger.exportSession(context)
                        if (fileName != null) {
                            Toast.makeText(context, "Saved as $fileName", Toast.LENGTH_LONG).show()
                        }
                        isTest1Active = false
                        updateUIState(AppState.READY_TYPING, "Typing Mode")
                    }
                }
            } else {
                // NORMAL TYPING MODE
                if (decodedChar.isNotEmpty()) {
                    // Strict match successful -> Commit staged blinks to baseline
                    adaptiveThreshold.commitStagedBlinks()

                    when (decodedChar) {
                        "<BS>" -> {
                            if (decodedText.isNotEmpty()) {
                                decodedText = decodedText.dropLast(1)
                                Log.d("BlinkMorse", "BACKSPACE executed. Text is now: '$decodedText'")
                            }
                        }
                        "<SPEAK>" -> {
                            if (decodedText.isNotEmpty()) {
                                tts?.speak(decodedText, TextToSpeech.QUEUE_FLUSH, null, null)
                                decodedText = ""
                            }
                        }
                        else -> {
                            decodedText += decodedChar
                        }
                    }
                    
                    sessionLogger.attachDecodedChar(decodedChar)

                    coroutineScope.launch(Dispatchers.Default) {
                        when (decodedChar) {
                            "<BS>" -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 200)
                            "<SPEAK>" -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
                            else -> toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                        }
                    }
                } else {
                    // Empty string means strict match failed (invalid sequence)
                    adaptiveThreshold.clearStagedBlinks()
                    coroutineScope.launch(Dispatchers.Default) {
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 150)
                    }
                }
            }
        }
    }

    val detector = remember {
        FaceLandmarkDetector(
            context = context,
            listener = object : FaceLandmarkDetector.Listener {
                override fun onResults(
                    result: FaceLandmarkerResult,
                    isFaceDetected: Boolean,
                    ear: Float?
                ) {
                    coroutineScope.launch(Dispatchers.Main) {
                        if (faceDetectedState != isFaceDetected) {
                            faceDetectedState = isFaceDetected
                        }

                        if (isFaceDetected && ear != null) {
                            val blinkResult = blinkDetector.update(ear)

                            if (blinkResult.blinkEnded && blinkResult.durationMs != null) {
                                val duration = blinkResult.durationMs

                                when (appState) {
                                    AppState.CALIBRATING_SHORT -> {
                                        adaptiveThreshold.seedShortSample(duration.toFloat())
                                        shortCalibCount++
                                        currentSymbols += "." // Accumulate short blinks in live buffer
                                        sessionLogger.logBlink(
                                            durationMs = duration,
                                            shortCenter = adaptiveThreshold.shortCenter,
                                            longCenter = adaptiveThreshold.longCenter,
                                            threshold = adaptiveThreshold.threshold,
                                            symbol = ".",
                                            adaptation = "calibration_short"
                                        )
                                        coroutineScope.launch(Dispatchers.Default) {
                                            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                                        }
                                        updateUIState(AppState.CALIBRATING_SHORT, "Perform 10 short blinks (dots) [$shortCalibCount/10]")
                                        
                                        // Trigger Visual Flash
                                        isFlashing = true
                                        coroutineScope.launch {
                                            delay(150)
                                            isFlashing = false
                                        }
                                        
                                        if (shortCalibCount >= 10) {
                                            currentSymbols = "" // Clear for next phase
                                            updateUIState(AppState.CALIBRATING_LONG, "Now perform 10 long blinks (dashes) [0/10]")
                                        }
                                    }
                                    AppState.CALIBRATING_LONG -> {
                                        adaptiveThreshold.seedLongSample(duration.toFloat())
                                        longCalibCount++
                                        currentSymbols += "-" // Accumulate long blinks in live buffer
                                        sessionLogger.logBlink(
                                            durationMs = duration,
                                            shortCenter = adaptiveThreshold.shortCenter,
                                            longCenter = adaptiveThreshold.longCenter,
                                            threshold = adaptiveThreshold.threshold,
                                            symbol = "-",
                                            adaptation = "calibration_long"
                                        )
                                        coroutineScope.launch(Dispatchers.Default) {
                                            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
                                        }
                                        updateUIState(AppState.CALIBRATING_LONG, "Now perform 10 long blinks (dashes) [$longCalibCount/10]")
                                        
                                        // Trigger Visual Flash
                                        isFlashing = true
                                        coroutineScope.launch {
                                            delay(150)
                                            isFlashing = false
                                        }
                                        
                                        if (longCalibCount >= 10) {
                                            currentSymbols = "" // Clear for typing phase
                                            if (adaptiveThreshold.hasValidCalibration()) {
                                                adaptiveThreshold.saveToPreferences(sharedPrefs)
                                                hasSavedCalibration = true
                                                val prompt = if (isTest1Active) "TEST 1: Spell 'SOS' 15 times" else "Typing Mode"
                                                updateUIState(AppState.READY_TYPING, prompt)
                                                coroutineScope.launch(Dispatchers.Default) {
                                                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300)
                                                }
                                            } else {
                                                updateUIState(AppState.CALIBRATION_FAILED, "Calibration Failed: Blinks not distinct enough. Please Recalibrate.")
                                                coroutineScope.launch(Dispatchers.Default) {
                                                    toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 300)
                                                }
                                            }
                                        }
                                    }
                                    AppState.CALIBRATION_FAILED -> {
                                        // Block further blinks until user resets
                                    }
                                    AppState.READY_TYPING -> {
                                        val symbol = adaptiveThreshold.classifyAndAdapt(duration)

                                        val symbolAccepted = morseDecoder.addSymbol(symbol)

                                        if (!symbolAccepted) {
                                            // The blink caused a 6-symbol abort. 
                                            // Clear the staged medians so the math doesn't corrupt.
                                            // (Sound buzz handled inside onAbort)
                                            adaptiveThreshold.clearStagedBlinks()
                                        } else {
                                            // Normal Blink -> Play Dot/Dash
                                            coroutineScope.launch(Dispatchers.Default) {
                                                if (symbol == ".") {
                                                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                                                } else if (symbol == "-") {
                                                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
                                                }
                                            }
                                            
                                            // Trigger Visual Flash
                                            isFlashing = true
                                            coroutineScope.launch {
                                                delay(150)
                                                isFlashing = false
                                            }
                                        }

                                        sessionLogger.logBlink(
                                            durationMs = duration,
                                            shortCenter = adaptiveThreshold.shortCenter,
                                            longCenter = adaptiveThreshold.longCenter,
                                            threshold = adaptiveThreshold.threshold,
                                            symbol = symbol,
                                            adaptation = adaptiveThreshold.lastAdaptation
                                        )

                                        thresholdInfo = String.format(
                                            Locale.US,
                                            "Threshold: %.0f ms | Short: %.0f ms | Long: %.0f ms",
                                            adaptiveThreshold.threshold,
                                            adaptiveThreshold.shortCenter,
                                            adaptiveThreshold.longCenter
                                        )
                                    }
                                }
                            }
                        } else {
                            blinkDetector.reset()
                        }
                    }
                }

                override fun onError(error: String) {
                    coroutineScope.launch(Dispatchers.Main) {
                        Log.e("BlinkMorse", "Detector error: $error")
                    }
                }
            }
        )
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            detector.clear()
            cameraExecutor.shutdown()
            morseDecoder.clear()
            toneGenerator.release()
        }
    }

    var currentScreen by rememberSaveable { mutableStateOf("intro") }

    val cameraViewComposable: @Composable () -> Unit = {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = surfaceProvider
                        }

                        val resolutionSelector = androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                androidx.camera.core.resolutionselector.ResolutionStrategy(
                                    android.util.Size(480, 640),
                                    androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                                )
                            )
                            .build()

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setResolutionSelector(resolutionSelector)
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        var lastFpsTime = System.currentTimeMillis()
                        var frameCount = 0

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            frameCount++
                            val now = System.currentTimeMillis()
                            if (now - lastFpsTime >= 1000) {
                                currentFps = frameCount
                                frameCount = 0
                                lastFpsTime = now
                            }
                            detector.detectLiveStream(imageProxy)
                        }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )
    }

    LaunchedEffect(shortCalibCount, longCalibCount, currentScreen) {
        if (currentScreen == "intro" && shortCalibCount >= 10 && longCalibCount >= 10) {
            delay(800)
            currentScreen = "main"
        }
    }

    when (currentScreen) {
        "intro" -> IntroPanelScreen(
            shortCount = shortCalibCount,
            longCount = longCalibCount,
            currentBuffer = currentSymbols,
            promptMessage = promptMessage,
            isFlashing = isFlashing,
            faceDetectedState = faceDetectedState,
            cameraView = cameraViewComposable,
            canSkip = hasSavedCalibration,
            onNavigateToMain = { 
                if (shortCalibCount < 10 || longCalibCount < 10) {
                    if (adaptiveThreshold.loadFromPreferences(sharedPrefs)) {
                        appState = AppState.READY_TYPING
                        promptMessage = if (isTest1Active) "TEST 1: Spell 'SOS' 15 times" else "Typing Mode"
                        currentScreen = "main"
                    }
                } else {
                    appState = AppState.READY_TYPING
                    promptMessage = if (isTest1Active) "TEST 1: Spell 'SOS' 15 times" else "Typing Mode"
                    currentScreen = "main"
                }
            }
        )
        "main" -> MainPanelScreen(
            currentSymbols = currentSymbols,
            decodedText = decodedText,
            faceDetectedState = faceDetectedState,
            isTest1Active = isTest1Active,
            isFlashing = isFlashing,
            cameraView = cameraViewComposable,
            onTextChange = { newText -> decodedText = newText },
            onNavigateToHelp = { currentScreen = "help" },
            onNavigateToMenu = { currentScreen = "menu" }
        )
        "menu" -> MenuPanelScreen(
            currentSymbols = currentSymbols,
            isTest1Active = isTest1Active,
            onClearClick = { decodedText = "" },
            onSpeakClick = {
                if (decodedText.isNotEmpty()) {
                    tts?.speak(decodedText, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            },
            onTest1Click = {
                if (isTest1Active) {
                    isTest1Active = false
                    if (adaptiveThreshold.hasValidCalibration()) {
                        updateUIState(AppState.READY_TYPING, "Typing Mode")
                    } else {
                        adaptiveThreshold.reset()
                        shortCalibCount = 0
                        longCalibCount = 0
                        currentSymbols = ""
                        updateUIState(AppState.CALIBRATING_SHORT, "Perform 10 short blinks (dots) [0/10]")
                    }
                } else {
                    isTest1Active = true
                    sessionLogger.startNewSession()
                    decodedText = ""
                    adaptiveThreshold.reset()
                    shortCalibCount = 0
                    longCalibCount = 0
                    currentSymbols = ""
                    updateUIState(AppState.CALIBRATING_SHORT, "Perform 10 short blinks (dots) [0/10]")
                }
            },
            onDataManagerClick = { showDataManagerBottomSheet(context) },
            onRecalibrateClick = {
                isTest1Active = false
                adaptiveThreshold.reset()
                shortCalibCount = 0
                longCalibCount = 0
                currentSymbols = ""
                updateUIState(AppState.CALIBRATING_SHORT, "Perform 10 short blinks (dots) [0/10]")
                decodedText = ""
                morseDecoder.clear()
                currentScreen = "intro"
            },
            onClose = { currentScreen = "main" }
        )
        "help" -> HelpPanelScreen(
            currentSymbols = currentSymbols,
            onClose = { currentScreen = "main" }
        )
    }
}
