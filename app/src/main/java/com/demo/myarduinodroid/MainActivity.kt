package com.demo.myarduinodroid

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demo.myarduinodroid.ui.theme.MyArduinodroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var arduinoCLI: ArduinoCLIBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Arduino CLI
        arduinoCLI = ArduinoCLIBridge()
        
        // Set Arduino data directory to use Android emulated storage
        arduinoCLI.setArduinoDataDir(this)
        
        // Initialize the Arduino CLI system
        arduinoCLI.nativeInitArduinoCLI()
        
        setContent {
            MyArduinodroidTheme {
                var currentScreen by remember { mutableStateOf("main") }
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "main" -> MainScreen(
                            arduinoCLI = arduinoCLI,
                            onNavigateToLibraryManagement = { currentScreen = "library" }
                        )
                        "library" -> LibraryManagementScreen(
                            arduinoCLI = arduinoCLI,
                            onBackPressed = { currentScreen = "main" }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    arduinoCLI: ArduinoCLIBridge,
    onNavigateToLibraryManagement: () -> Unit
) {
    var selectedBoard by remember { mutableStateOf("arduino:avr:uno") }
    var outputText by remember { mutableStateOf("Arduino CLI Integration\nClick 'Check Library Status' to begin...") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Arduino CLI Integration",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = onNavigateToLibraryManagement,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Library Management")
            }
            
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
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Check Status")
            }
        }

        // Board Selection
        Text(
            text = "Select Board:",
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val boards = listOf(
            "arduino:avr:uno",
            "arduino:avr:nano",
            "arduino:avr:mega",
            "esp32:esp32:esp32",
            "esp8266:esp8266:nodemcuv2"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(boards) { board ->
                FilterChip(
                    selected = selectedBoard == board,
                    onClick = { selectedBoard = board },
                    label = { Text(board) }
                )
            }
        }

        // Core Functions
        Text(
            text = "Core Functions",
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
                        outputText = "Getting board info..."
                        try {
                            val result = withContext(Dispatchers.IO) {
                                arduinoCLI.getBoardInfo(selectedBoard)
                            }
                            outputText = "Board Info:\n$result"
                        } catch (e: Exception) {
                            outputText = "Error getting board info: ${e.message}"
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text("Board Info")
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
                    .padding(end = 4.dp)
            ) {
                Text("List Cores")
            }
            Button(
                onClick = {
                    scope.launch {
                        outputText = "Installing Arduino AVR core..."
                        try {
                            val result = withContext(Dispatchers.IO) {
                                arduinoCLI.installCore("arduino:avr")
                            }
                            outputText = "Core Install Result:\n$result"
                        } catch (e: Exception) {
                            outputText = "Error installing core: ${e.message}"
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text("Install Core")
            }
        }

        // Compilation Section
        Text(
            text = "Sketch Compilation",
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

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
                        // Pass the sketch directory as output directory to avoid nested build folders
                        val result = withContext(Dispatchers.IO) {
                            arduinoCLI.compileSketch(selectedBoard, sketchDir, sketchDir)
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
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Build, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compile Sketch")
        }

        // Output Text
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Output",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing...")
                    }
                }
                
                Text(
                    text = outputText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
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
                            // Pass the sketch directory as output directory to avoid nested build folders
                            val result = withContext(Dispatchers.IO) {
                                arduinoCLI.compileSketch(selectedBoard, sketchDir, sketchDir)
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