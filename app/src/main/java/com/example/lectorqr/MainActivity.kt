package com.example.lectorqr

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.lectorqr.ui.theme.LectorQRTheme
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LectorQRTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QRScannerScreen()
                }
            }
        }
    }
}

@Composable
fun QRScannerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { VirusTotalRepository() }

    var hasCameraPermission by remember { mutableStateOf(false) }
    var scannedUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var showSecurityWarning by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Mostramos la cámara solo si no hay un proceso de escaneo o alerta activo
            if (scannedUrl == null) {
                CameraPreview { url ->
                    scannedUrl = url
                    isLoading = true
                    scope.launch {
                        val result = repository.checkUrl(url)
                        isLoading = false
                        
                        when (result) {
                            is ScanResult.Safe -> {
                                infoMessage = "URL validada, redirigiendo..."
                                delay(5000)
                                openUrl(context, url)
                                infoMessage = null
                                scannedUrl = null
                            }
                            is ScanResult.NotFound -> {
                                infoMessage = "Esta página no está en la base de datos, actúa con responsabilidad"
                                delay(5000)
                                openUrl(context, url)
                                infoMessage = null
                                scannedUrl = null
                            }
                            is ScanResult.Malicious -> {
                                showSecurityWarning = url
                            }
                            is ScanResult.Error -> {
                                Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                                scannedUrl = null
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                StatusOverlay("Verificando seguridad con VirusTotal...") {
                    CircularProgressIndicator()
                }
            }

            infoMessage?.let { msg ->
                val isWarning = msg.contains("responsabilidad")
                StatusOverlay(
                    message = msg,
                    icon = if (isWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                    iconColor = if (isWarning) Color(0xFFFFA000) else Color(0xFF4CAF50)
                )
            }

            showSecurityWarning?.let { url ->
                AlertDialog(
                    onDismissRequest = { 
                        showSecurityWarning = null
                        scannedUrl = null
                    },
                    title = { Text("⚠️ ¡Alerta de Seguridad!") },
                    text = { Text("VirusTotal ha detectado posibles amenazas en esta URL:\n\n$url\n\n¿Deseas abrirla de todos modos?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                openUrl(context, url)
                                showSecurityWarning = null
                                scannedUrl = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Abrir (Riesgo)")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showSecurityWarning = null
                            scannedUrl = null
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se requiere permiso de cámara para escanear códigos QR")
        }
    }
}

@Composable
fun StatusOverlay(
    message: String,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = iconColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                content?.invoke()
                if (content != null) Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir la URL", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CameraPreview(onUrlDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val scanner = BarcodeScanning.getClient()
                val analyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                var isProcessing = false

                analyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null && !isProcessing) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.url?.url?.let { url ->
                                        if (!isProcessing) {
                                            isProcessing = true
                                            onUrlDetected(url)
                                        }
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analyzer)
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Error al vincular la cámara", e)
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
