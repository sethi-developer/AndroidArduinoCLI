package com.demo.myarduinodroid

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.demo.myarduinodroid.ui.theme.MyArduinodroidTheme
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var arduinoCLI: ArduinoCLIBridge
    private lateinit var usbManager: UsbManager
    private var usbDevice: UsbDevice? = null
    private var usbConnection: UsbDeviceConnection? = null
    
    companion object {
        private const val TAG = "MainActivity"
        private const val ACTION_USB_PERMISSION = "com.demo.myarduinodroid.USB_PERMISSION"
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let {
                            usbDevice = it
                            usbConnection = usbManager.openDevice(it)
                            if (usbConnection != null) {
                                Toast.makeText(this@MainActivity, "USB device connected", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "USB permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Arduino CLI and USB manager
        initializeArduinoCLI()
        initializeUSBManager()
        checkPermissions()
        
        setContent {
            MyArduinodroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ArduinoCLIScreen(
                        modifier = Modifier.padding(innerPadding),
                        arduinoCLI = arduinoCLI,
                        onConnect = { requestUsbPermission() },
                        onDisconnect = { disconnectUSB() }
                    )
                }
            }
        }
    }
    
    private fun initializeArduinoCLI() {
        arduinoCLI = ArduinoCLIBridge()
        Thread {
            try {
                val result = arduinoCLI.initArduinoCLI()
                runOnUiThread {
                    if (result == 0) {
                        Log.d(TAG, "Arduino CLI initialized successfully")
                    } else {
                        Log.e(TAG, "Failed to initialize Arduino CLI")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Arduino CLI", e)
            }
        }.start()
    }
    
    private fun initializeUSBManager() {
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)
    }
    
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    
    private fun requestUsbPermission() {
        // For demo purposes, we'll simulate a connection
        Toast.makeText(this, "USB connection simulated", Toast.LENGTH_SHORT).show()
    }
    
    private fun disconnectUSB() {
        usbConnection?.close()
        usbConnection = null
        usbDevice = null
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
        usbConnection?.close()
    }
}

@Composable
fun ArduinoCLIScreen(
    modifier: Modifier = Modifier,
    arduinoCLI: ArduinoCLIBridge,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedBoard by remember { mutableStateOf("arduino:avr:uno") }
    var selectedPort by remember { mutableStateOf("No ports available") }
    var outputText by remember { mutableStateOf("Arduino CLI ready. Click 'List Boards' to start.") }
    var isConnected by remember { mutableStateOf(false) }
    
    val boards = remember { arduinoCLI.getCommonBoards() }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Arduino CLI Integration",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Board Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Board: ",
                modifier = Modifier.width(80.dp)
            )
            DropdownMenu(
                expanded = false,
                onDismissRequest = { },
                modifier = Modifier.weight(1f)
            ) {
                boards.forEach { board ->
                    DropdownMenuItem(
                        text = { Text(board) },
                        onClick = { selectedBoard = board }
                    )
                }
            }
            Text(
                text = selectedBoard,
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray)
                    .padding(8.dp)
            )
        }
        
        // Port Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Port: ",
                modifier = Modifier.width(80.dp)
            )
            Text(
                text = selectedPort,
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray)
                    .padding(8.dp)
            )
        }
        
        // Control Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Button(
                onClick = { 
                    isConnected = true
                    onConnect()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Text("Connect")
            }
            Button(
                onClick = { 
                    isConnected = false
                    onDisconnect()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text("Disconnect")
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        outputText = "Listing boards..."
                        try {
                            val result = withContext(Dispatchers.IO) {
                                arduinoCLI.listBoards()
                            }
                            outputText = "Available Boards:\n$result"
                        } catch (e: Exception) {
                            outputText = "Error listing boards: ${e.message}"
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Text("List Boards")
            }
            Button(
                onClick = {
                    scope.launch {
                        outputText = "Listing cores..."
                        try {
                            val result = withContext(Dispatchers.IO) {
                                arduinoCLI.listCores()
                            }
                            outputText = "Installed Cores:\n$result"
                        } catch (e: Exception) {
                            outputText = "Error listing cores: ${e.message}"
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text("List Cores")
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        outputText = "Compiling sketch for $selectedBoard..."
                        try {
                            // First check if the library is available
                            if (!arduinoCLI.isNativeLibraryAvailable()) {
                                outputText = "❌ Cannot compile: Arduino CLI native library not available!\n\n" +
                                           "Please click 'Check Library Status' to see what needs to be implemented.\n\n" +
                                           "The .hex file cannot be generated until the native Arduino CLI library is implemented."
                                return@launch
                            }
                            
                            val sketchDir = createTestSketch(context)
                            // The compileSketch method now automatically creates the build directory
                            val result = withContext(Dispatchers.IO) {
                                arduinoCLI.compileSketch(selectedBoard, sketchDir, "")
                            }
                            outputText = "Compilation Result:\n$result"
                        } catch (e: Exception) {
                            outputText = "❌ Compilation Error:\n${e.message}\n\n" +
                                       "This error occurs because the Arduino CLI native library is not implemented yet.\n" +
                                       "Click 'Check Library Status' for more information."
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Text("Compile")
            }
            Button(
                onClick = {
                    if (selectedPort == "No ports available") {
                        Toast.makeText(context, "Please connect a board first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        outputText = "Uploading to $selectedBoard via $selectedPort..."
                        try {
                            val sketchDir = context.getExternalFilesDir(null)?.absolutePath + "/test_sketch"
                            val hexFile = "$sketchDir/build/test_sketch.ino.hex"
                            val result = withContext(Dispatchers.IO) {
                                arduinoCLI.uploadHex(hexFile, selectedPort, selectedBoard)
                            }
                            outputText = "Upload Result:\n$result"
                        } catch (e: Exception) {
                            outputText = "Error uploading sketch: ${e.message}"
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text("Upload")
            }
        }
        
        // Direct Hex File Creation Test Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        outputText = "Checking Arduino CLI library status..."
                        try {
                            val result = withContext(Dispatchers.IO) {
                                arduinoCLI.getLibraryStatus()
                            }
                            outputText = "Library Status:\n$result"
                        } catch (e: Exception) {
                            outputText = "Error checking library status: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check Library Status")
            }
        }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                outputText = "Listing libraries..."
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        arduinoCLI.listLibraries()
                                    }
                                    outputText = "Installed Libraries:\n$result"
                                } catch (e: Exception) {
                                    outputText = "Error listing libraries: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("List Libraries")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                outputText = "Updating package index..."
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        arduinoCLI.updateIndex()
                                    }
                                    outputText = "Update Result:\n$result"
                                } catch (e: Exception) {
                                    outputText = "Error updating index: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Update Index")
                    }
                }

                // Library Management Section
                Text(
                    text = "Library Management",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                outputText = "Installing WiFi library..."
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        arduinoCLI.installLibrary("WiFi")
                                    }
                                    outputText = "Install Result:\n$result"
                                } catch (e: Exception) {
                                    outputText = "Error installing library: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("Install WiFi")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                outputText = "Searching for libraries..."
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        arduinoCLI.searchLibrary("sensor")
                                    }
                                    outputText = "Search Result:\n$result"
                                } catch (e: Exception) {
                                    outputText = "Error searching libraries: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Search Sensor")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                outputText = "Getting WiFi library info..."
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        arduinoCLI.getLibraryInfo("WiFi")
                                    }
                                    outputText = "Library Info:\n$result"
                                } catch (e: Exception) {
                                    outputText = "Error getting library info: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("Get WiFi Info")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                outputText = "Uninstalling WiFi library..."
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        arduinoCLI.uninstallLibrary("WiFi")
                                    }
                                    outputText = "Uninstall Result:\n$result"
                                } catch (e: Exception) {
                                    outputText = "Error uninstalling library: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Uninstall WiFi")
                    }
                }

                // Install from ZIP Section
                Text(
                    text = "Install from ZIP",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = {
                        scope.launch {
                            outputText = "Installing library from ZIP..."
                            try {
                                // Example ZIP path - in real app, this would come from file picker
                                val zipPath = context.getExternalFilesDir(null)?.absolutePath + "/example_library.zip"
                                val result = withContext(Dispatchers.IO) {
                                    arduinoCLI.installLibraryFromZip(zipPath)
                                }
                                outputText = "ZIP Install Result:\n$result"
                            } catch (e: Exception) {
                                outputText = "Error installing from ZIP: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Install Library from ZIP")
                }
        
        // Output Text
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Text(
                text = outputText,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Start
            )
        }
    }
}

private fun createTestSketch(context: Context): String {
    val sketchDir = context.getExternalFilesDir(null)?.absolutePath + "/test_sketch"
    val dir = File(sketchDir)
    
    if (!dir.exists()) {
        dir.mkdirs()
    }
    
    // Also create the build directory
    val buildDir = File(sketchDir, "build")
    if (!buildDir.exists()) {
        buildDir.mkdirs()
    }
    
    val sketchFile = File(dir, "test_sketch.ino")
    
    try {
        // Use PrintWriter for more reliable file writing
        java.io.PrintWriter(sketchFile, "UTF-8").use { writer ->
            writer.println("void setup() {")
            writer.println("  Serial.begin(9600);")
            writer.println("  pinMode(13, OUTPUT);")
            writer.println("}")
            writer.println("")
            writer.println("void loop() {")
            writer.println("  digitalWrite(13, HIGH);")
            writer.println("  delay(1000);")
            writer.println("  digitalWrite(13, LOW);")
            writer.println("  delay(1000);")
            writer.println("  Serial.println(\"Hello from Arduino!\");")
            writer.println("}")
        }
        
    } catch (e: IOException) {
        Log.e("MainActivity", "Error creating test sketch", e)
    }
    
    return sketchDir
}