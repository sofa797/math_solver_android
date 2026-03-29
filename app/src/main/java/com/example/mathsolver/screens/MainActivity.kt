package com.example.mathsolver.screens

import android.Manifest
import android.graphics.*
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mathsolver.domain.BackendApi
import com.example.mathsolver.ui.theme.MathSolverTheme
import com.example.mathsolver.viewmodel.MainViewModel
import com.google.accompanist.permissions.*
import java.io.ByteArrayOutputStream
import java.util.UUID
import android.content.Context
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MathSolverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EquationScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EquationScreen(viewModel: MainViewModel = viewModel()) {

    val result by viewModel.result.observeAsState("")
    var showCamera by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val userId = remember { getOrCreateUserId(context) }
    val api = remember { BackendApi(userId) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (showCamera) {
        if (cameraPermissionState.status.isGranted) {
            CameraView(
                viewModel = viewModel,
                onClose = { showCamera = false }
            )
        } else {
            SideEffect { cameraPermissionState.launchPermissionRequest() }
        }
    } else {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Math Solver") }) }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    "Enter a linear equation",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(16.dp))

                val input by viewModel.input.observeAsState("")

                OutlinedTextField(
                    value = input,
                    onValueChange = { viewModel.setInput(it) },
                    label = { Text("For instance: 2x+4=10") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
//                    trailingIcon = {
//                        if (input.isNotEmpty()) {
//                            IconButton(onClick = { input = "" }) {
//                                Icon(Icons.Default.Clear, contentDescription = "Clear")
//                            }
//                        }
//                    }
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        api.solveFromText(input) { result ->
                            viewModel.setResult(result)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solve")
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showCamera = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Scan from camera")
                }

                OutlinedButton(
                    onClick = {
                        api.getHistory { history ->
                            viewModel.setResult(history)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("History")
                }

                Spacer(Modifier.height(32.dp))

                if (result.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text("Result:", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(8.dp))
                            Text(result, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraView(
    viewModel: MainViewModel,
    onClose: () -> Unit,
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val userId = remember { getOrCreateUserId(context) }
    val api = remember { BackendApi(userId) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    data class Corner(
        val x: Float,
        val y: Float,
    )
    var corners by remember {
        mutableStateOf(
            listOf(
                Corner(0.2f, 0.3f),
                Corner(0.8f, 0.3f),
                Corner(0.8f, 0.6f),
                Corner(0.2f, 0.6f),
            )
        )
    }

    val cropRect = remember(corners) {
        val xs = corners.map { it.x }
        val ys = corners.map { it.y }

        RectF(
            xs.minOrNull() ?: 0f,
            ys.minOrNull() ?: 0f,
            xs.maxOrNull() ?: 1f,
            ys.maxOrNull() ?: 1f
        )
    }

    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        var canvasSize by remember { mutableStateOf(Size.Zero) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    canvasSize = Size(it.width.toFloat(), it.height.toFloat())
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->

                        change.consume()

                        val touch = change.position

                        val index = corners.indexOfFirst { corner ->
                            val px = corner.x * canvasSize.width
                            val py = corner.y * canvasSize.height

                            val dx = px - touch.x
                            val dy = py - touch.y

                            (dx * dx + dy * dy) < 4000
                        }

                        if (index != -1) {
                            val dx = dragAmount.x / canvasSize.width
                            val dy = dragAmount.y / canvasSize.height

                            val updated = corners.toMutableList()

                            val old = updated[index]

                            when (index) {

                                0 -> {
                                    updated[0] = Corner(
                                        (old.x + dx).coerceIn(0f, updated[1].x - 0.05f),
                                        (old.y + dy).coerceIn(0f, updated[3].y - 0.05f)
                                    )
                                    updated[1] = updated[1].copy(y = updated[0].y)
                                    updated[3] = updated[3].copy(x = updated[0].x)
                                }

                                1 -> {
                                    updated[1] = Corner(
                                        (old.x + dx).coerceIn(updated[0].x + 0.05f, 1f),
                                        (old.y + dy).coerceIn(0f, updated[2].y - 0.05f)
                                    )
                                    updated[0] = updated[0].copy(y = updated[1].y)
                                    updated[2] = updated[2].copy(x = updated[1].x)
                                }

                                2 -> {
                                    updated[2] = Corner(
                                        (old.x + dx).coerceIn(updated[3].x + 0.05f, 1f),
                                        (old.y + dy).coerceIn(updated[1].y + 0.05f, 1f)
                                    )
                                    updated[1] = updated[1].copy(x = updated[2].x)
                                    updated[3] = updated[3].copy(y = updated[2].y)
                                }

                                3 -> {
                                    updated[3] = Corner(
                                        (old.x + dx).coerceIn(0f, updated[2].x - 0.05f),
                                        (old.y + dy).coerceIn(updated[0].y + 0.05f, 1f)
                                    )
                                    updated[0] = updated[0].copy(x = updated[3].x)
                                    updated[2] = updated[2].copy(y = updated[3].y)
                                }
                            }

                            corners = updated
                        }
                    }
                }
        ) {
            for (i in corners.indices) {
                val current = corners[i]
                val next = corners[(i + 1) % corners.size]

                drawLine(
                    color = Color.Red,
                    start = Offset(current.x * size.width, current.y * size.height),
                    end = Offset(next.x * size.width, next.y * size.height),
                    strokeWidth = 4f
                )
            }

            corners.forEach { corner ->
                drawCircle(
                    color = Color.White,
                    radius = 18f,
                    center = Offset(
                        corner.x * size.width,
                        corner.y * size.height
                    )
                )
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Button(
            onClick = {
                val executor = ContextCompat.getMainExecutor(context)

                imageCapture.takePicture(
                    executor,
                    object : ImageCapture.OnImageCapturedCallback() {

                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = image.toBitmap()
                            image.close()

                            val xs = corners.map { it.x }
                            val ys = corners.map { it.y }

                            val rect = RectF(
                                xs.minOrNull()!!,
                                ys.minOrNull()!!,
                                xs.maxOrNull()!!,
                                ys.maxOrNull()!!
                            )

                            val cropped = bitmap.crop(rect)

                            api.solveFromImage(
                                cropped.toByteArray()
                            ) { equation, result ->
                                (context as ComponentActivity).runOnUiThread {
                                    viewModel.setInput(equation)
                                    viewModel.setResult(result)
                                    onClose()
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Toast.makeText(
                                context,
                                "Capture error: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Text("Take a picture")
        }
    }
}


fun Bitmap.crop(rect: RectF): Bitmap {
    val x = (rect.left * width).toInt().coerceIn(0, width - 1)
    val y = (rect.top * height).toInt().coerceIn(0, height - 1)
    val w = ((rect.right - rect.left) * width).toInt().coerceAtLeast(1)
    val h = ((rect.bottom - rect.top) * height).toInt().coerceAtLeast(1)

    return Bitmap.createBitmap(this, x, y, w, h)
}

fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}

fun getOrCreateUserId(context: Context): String {
    val prefs = context.getSharedPreferences("math_solver", Context.MODE_PRIVATE)
    var userId = prefs.getString("user_id", null)
    if (userId == null) {
        userId = UUID.randomUUID().toString()
        prefs.edit().putString("user_id", userId).apply()
    }
    return  userId
}