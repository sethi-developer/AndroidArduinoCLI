package com.demo.myarduinodroid

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.size
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryManagementScreen(
    arduinoCLI: ArduinoCLIBridge,
    onBackPressed: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Official Libraries", "ZIP Libraries", "Installed Libraries")
    
    var outputText by remember { mutableStateOf("Library Management\nSelect a tab to get started...") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // State for Official Libraries tab
    var libraryName by remember { mutableStateOf("") }
    
    // State for ZIP Libraries tab
    var selectedZipUri by remember { mutableStateOf<Uri?>(null) }
    var selectedZipName by remember { mutableStateOf("") }
    
    // State for Installed Libraries tab
    var installedLibraries by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Load installed libraries when the screen is first displayed
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            try {
                val result = arduinoCLI.listLibraries()
                val libraries = parseLibraryResult(result)
                installedLibraries = libraries
                outputText = "Loaded ${libraries.size} installed libraries"
            } catch (e: Exception) {
                outputText = "Error loading libraries: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    val zipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedZipUri = uri
        selectedZipName = uri?.lastPathSegment ?: ""
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Library Management",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Tabs
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (selectedTabIndex) {
            0 -> OfficialLibrariesTab(
                libraryName = libraryName,
                onLibraryNameChange = { libraryName = it },
                onInstallClick = {
                    scope.launch {
                        isLoading = true
                        outputText = "Installing library: $libraryName..."
                        try {
                            val result = arduinoCLI.installLibrary(libraryName)
                            outputText = "Install Result:\n$result"
                            libraryName = "" // Clear input after installation
                        } catch (e: Exception) {
                            outputText = "Error installing library: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
            1 -> ZipLibrariesTab(
                selectedZipName = selectedZipName,
                onPickZipClick = { zipLauncher.launch("application/zip") },
                onInstallClick = {
                    selectedZipUri?.let { uri ->
                        scope.launch {
                            isLoading = true
                            outputText = "Installing library from ZIP..."
                            try {
                                // Copy ZIP to app's external files directory
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val zipFile = context.getExternalFilesDir(null)?.resolve("temp_library.zip")
                                
                                if (inputStream != null && zipFile != null) {
                                    zipFile.outputStream().use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                    
                                    val result = arduinoCLI.installLibraryFromZip(zipFile.absolutePath)
                                    outputText = "ZIP Install Result:\n$result"
                                    
                                    // Clean up temp file
                                    zipFile.delete()
                                    selectedZipUri = null
                                    selectedZipName = ""
                                } else {
                                    outputText = "Error: Could not process ZIP file"
                                }
                            } catch (e: Exception) {
                                outputText = "Error installing from ZIP: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            )
            2 -> InstalledLibrariesTab(
                installedLibraries = installedLibraries,
                onRefreshClick = {
                    scope.launch {
                        isLoading = true
                        outputText = "Refreshing installed libraries..."
                        try {
                            val result = arduinoCLI.listLibraries()
                            val libraries = parseLibraryResult(result)
                            installedLibraries = libraries
                            outputText = "Found ${libraries.size} installed libraries:\n$result"
                        } catch (e: Exception) {
                            outputText = "Error listing libraries: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                onUninstallClick = { libraryName ->
                    scope.launch {
                        isLoading = true
                        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                        outputText = "🕐 [$timestamp] Starting uninstall process for library: $libraryName...\nPlease wait..."
                        try {
                            // Call the uninstall function
                            outputText += "\n\n🔄 Calling Arduino CLI uninstall function..."
                            val result = arduinoCLI.uninstallLibrary(libraryName)
                            outputText += "\n\n📋 UNINSTALL RESULT:\n$result"
                            
                            // Wait a moment for the operation to complete
                            outputText += "\n\n⏳ Waiting for file system operations to complete..."
                            delay(500)
                            
                            // Reload libraries from file system to ensure consistency
                            outputText += "\n\n🔄 Reloading libraries from file system..."
                            val reloadResult = arduinoCLI.reloadLibraries()
                            outputText += "\n\n📋 RELOAD RESULT:\n$reloadResult"
                            
                            // Get the updated library list
                            outputText += "\n\n🔄 Getting updated library list..."
                            val refreshResult = arduinoCLI.listLibraries()
                            val libraries = parseLibraryResult(refreshResult)
                            
                            // Update the UI state
                            installedLibraries = libraries
                            
                            // Show final result
                            outputText += "\n\n✅ FINAL RESULT: Library list updated. Found ${libraries.size} libraries."
                            
                            // Debug: Show the current library list
                            if (libraries.isNotEmpty()) {
                                outputText += "\n\n📚 Current libraries:\n" + libraries.joinToString("\n")
                            } else {
                                outputText += "\n\n📚 No libraries currently installed."
                            }
                            
                        } catch (e: Exception) {
                            outputText = "Error uninstalling library: ${e.message}\n\nStack trace: ${e.stackTraceToString()}"
                        } finally {
                            Log.d("Arduino CLI", outputText)
                            isLoading = false
                        }
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Output Section
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
fun OfficialLibrariesTab(
    libraryName: String,
    onLibraryNameChange: (String) -> Unit,
    onInstallClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Install Official Library",
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = libraryName,
            onValueChange = onLibraryNameChange,
            label = { Text("Library Name") },
            placeholder = { Text("e.g., WiFi, Adafruit_GFX, FastLED") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onInstallClick,
            enabled = libraryName.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Install Library")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Popular Libraries",
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val popularLibraries = listOf("WiFi", "Adafruit_GFX", "FastLED", "Servo", "Wire")
                
                popularLibraries.forEach { lib ->
                    Text(
                        text = "• $lib",
                        modifier = Modifier.padding(vertical = 2.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ZipLibrariesTab(
    selectedZipName: String,
    onPickZipClick: () -> Unit,
    onInstallClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Install from ZIP File",
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedButton(
            onClick = onPickZipClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pick ZIP File")
        }
        
        if (selectedZipName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Selected File:",
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = selectedZipName,
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onInstallClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Install from ZIP")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ZIP File Requirements",
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "• Must contain a valid Arduino library structure",
                    modifier = Modifier.padding(vertical = 2.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Should include library.properties file",
                    modifier = Modifier.padding(vertical = 2.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Source files should be in the root directory",
                    modifier = Modifier.padding(vertical = 2.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun InstalledLibrariesTab(
    installedLibraries: List<String>,
    onRefreshClick: () -> Unit,
    onUninstallClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Installed Libraries",
                fontSize = 20.sp,
                style = MaterialTheme.typography.titleLarge
            )
            
                        IconButton(onClick = onRefreshClick) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
        
        if (installedLibraries.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No libraries installed",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Install libraries from the Official Libraries tab or ZIP files",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            // Debug: Show the count of libraries
            Text(
                text = "Found ${installedLibraries.size} installed libraries",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(installedLibraries) { libraryName ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = libraryName,
                                        fontSize = 18.sp,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = "Installed library",
                                        fontSize = 14.sp,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    // Library details row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Version info
                                        Column {
                                            Text(
                                                text = "Version",
                                                fontSize = 12.sp,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Latest",
                                                fontSize = 14.sp,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        
                                        // Author info
                                        Column {
                                            Text(
                                                text = "Author",
                                                fontSize = 12.sp,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Arduino",
                                                fontSize = 14.sp,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        
                                        // Category info
                                        Column {
                                            Text(
                                                text = "Category",
                                                fontSize = 12.sp,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Communication",
                                                fontSize = 14.sp,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                                
                                // Uninstall button
                                OutlinedButton(
                                    onClick = { onUninstallClick(libraryName) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Uninstall")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to parse library results from the Go backend
private fun parseLibraryResult(result: String): List<String> {
    val lines = result.split("\n")
    val libraries = mutableListOf<String>()
    
    for (line in lines) {
        val trimmedLine = line.trim()
        
        // Try different patterns that the Go backend might return
        when {
            // Pattern: "Library: WiFi"
            trimmedLine.startsWith("Library:") -> {
                val libraryName = trimmedLine.substringAfter("Library:").trim()
                if (libraryName.isNotEmpty()) {
                    libraries.add(libraryName)
                }
            }
            
                    // Pattern: "- Servo 1.1.8 (by Arduino)" -> extract just "Servo"
        trimmedLine.startsWith("- ") && trimmedLine.contains(" (by ") -> {
            val fullName = trimmedLine.substringAfter("- ").substringBefore(" (by ").trim()
            // Extract just the library name without version (e.g., "Servo 1.1.8" -> "Servo")
            val libraryName = fullName.split(" ").firstOrNull() ?: fullName
            if (libraryName.isNotEmpty()) {
                libraries.add(libraryName)
            }
        }
            
            // Pattern: "WiFi - Arduino library for WiFi functionality"
            trimmedLine.contains(" - ") && !trimmedLine.startsWith("-") -> {
                val libraryName = trimmedLine.substringBefore(" - ").trim()
                if (libraryName.isNotEmpty() && !libraryName.contains(":")) {
                    libraries.add(libraryName)
                }
            }
            
            // Pattern: Just the library name (if it's a simple list)
            trimmedLine.isNotEmpty() && !trimmedLine.startsWith("Installed") && 
            !trimmedLine.startsWith("Libraries") && !trimmedLine.startsWith("Error") &&
            !trimmedLine.startsWith("No") && !trimmedLine.contains(":") -> {
                if (trimmedLine.length > 1 && trimmedLine.length < 50) {
                    libraries.add(trimmedLine)
                }
            }
        }
    }
    
    return libraries.distinct().filter { it.isNotEmpty() }
}
