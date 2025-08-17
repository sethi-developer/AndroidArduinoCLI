# 🚀 Arduino CLI Android Quick Reference

## ⚡ Quick Start

### 1. Create Android Project
```bash
# Use the automated setup script
./setup_android_project.sh MyArduinoApp com.example.myarduinoapp

# Or manually copy files from the template
cp -r android_project_template/* MyArduinoApp/
```

### 2. Open in Android Studio
- Open the project folder in Android Studio
- Sync Gradle files
- Build the project

### 3. Test Basic Functionality
```java
// Initialize Arduino CLI
ArduinoCLIBridge arduinoCLI = new ArduinoCLIBridge();
int result = arduinoCLI.initArduinoCLI();

if (result == 0) {
    // Success! Arduino CLI is ready
    String boards = arduinoCLI.listBoards();
    Log.d("ArduinoCLI", "Available boards: " + boards);
}
```

## 🔧 Core Functions

### Sketch Operations
```java
// Compile sketch
String result = arduinoCLI.compileSketch(
    "arduino:avr:uno",           // FQBN
    "/path/to/sketch",           // Sketch directory
    "/path/to/output"            // Output directory
);

// Upload hex file
String result = arduinoCLI.uploadHex(
    "/path/to/sketch.ino.hex",   // Hex file path
    "/dev/ttyUSB0",              // Port
    "arduino:avr:uno"           // FQBN
);

// One-step compile and upload
String result = arduinoCLI.compileAndUpload(
    "arduino:avr:uno",
    "/path/to/sketch",
    "/dev/ttyUSB0"
);
```

### Board Management
```java
// List available boards
String boards = arduinoCLI.listBoards();

// Get board information
String info = arduinoCLI.getBoardInfo("arduino:avr:uno");

// Get common board types
String[] commonBoards = arduinoCLI.getCommonBoards();
```

### Core & Library Management
```java
// List installed cores
String cores = arduinoCLI.listCores();

// Install new core
String result = arduinoCLI.installCore("esp32:esp32:esp32");

// List libraries
String libraries = arduinoCLI.listLibraries();

// Install library
String result = arduinoCLI.installLibrary("WiFi");

// Update package index
String result = arduinoCLI.updateIndex();
```

### Sketch Verification
```java
// Verify sketch syntax
String result = arduinoCLI.verifySketch(
    "arduino:avr:uno",
    "/path/to/sketch"
);
```

## 📱 Android Integration Patterns

### Background Operations
```java
// Always run Arduino CLI operations on background threads
new Thread(() -> {
    try {
        String result = arduinoCLI.compileSketch(fqbn, sketchDir, outDir);
        runOnUiThread(() -> {
            // Update UI with result
            outputText.setText(result);
        });
    } catch (Exception e) {
        Log.e("ArduinoCLI", "Error: " + e.getMessage());
        runOnUiThread(() -> {
            // Show error in UI
            outputText.setText("Error: " + e.getMessage());
        });
    }
}).start();
```

### Error Handling
```java
try {
    String result = arduinoCLI.compileSketch(fqbn, sketchDir, outDir);
    
    if (result.contains("failed") || result.contains("error")) {
        // Handle failure
        Log.e("ArduinoCLI", "Operation failed: " + result);
        showError("Operation failed: " + result);
    } else {
        // Handle success
        Log.d("ArduinoCLI", "Success: " + result);
        showSuccess("Operation completed successfully");
    }
} catch (Exception e) {
    // Handle JNI errors
    Log.e("ArduinoCLI", "JNI error: " + e.getMessage());
    showError("System error: " + e.getMessage());
}
```

### Permission Handling
```java
// Request storage permission
if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
        != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this, 
        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
        PERMISSION_REQUEST_CODE);
}

// Handle permission result
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSION_REQUEST_CODE) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with Arduino CLI operations
            initializeArduinoCLI();
        } else {
            // Permission denied, show message
            Toast.makeText(this, "Storage permission required", Toast.LENGTH_LONG).show();
        }
    }
}
```

## 🎯 Common Use Cases

### 1. Simple Compilation
```java
public void compileSketch(String fqbn, String sketchPath) {
    new Thread(() -> {
        try {
            String result = arduinoCLI.compileSketch(fqbn, sketchPath, sketchPath + "/build");
            runOnUiThread(() -> {
                if (result.contains("failed")) {
                    showError("Compilation failed: " + result);
                } else {
                    showSuccess("Compilation successful!");
                    enableUploadButton();
                }
            });
        } catch (Exception e) {
            showError("Compilation error: " + e.getMessage());
        }
    }).start();
}
```

### 2. Board Detection
```java
public void detectBoards() {
    new Thread(() -> {
        try {
            String boards = arduinoCLI.listBoards();
            runOnUiThread(() -> {
                updateBoardList(boards);
                if (boards.contains("No boards")) {
                    showMessage("No boards detected. Please connect a board.");
                }
            });
        } catch (Exception e) {
            showError("Board detection failed: " + e.getMessage());
        }
    }).start();
}
```

### 3. Library Installation
```java
public void installLibrary(String libraryName) {
    new Thread(() -> {
        try {
            showProgress("Installing " + libraryName + "...");
            String result = arduinoCLI.installLibrary(libraryName);
            runOnUiThread(() -> {
                hideProgress();
                if (result.contains("failed")) {
                    showError("Installation failed: " + result);
                } else {
                    showSuccess(libraryName + " installed successfully!");
                    refreshLibraryList();
                }
            });
        } catch (Exception e) {
            hideProgress();
            showError("Installation error: " + e.getMessage());
        }
    }).start();
}
```

## 🚨 Important Notes

### Threading
- **ALWAYS** run Arduino CLI operations on background threads
- Use `runOnUiThread()` to update UI from background operations
- Never call Arduino CLI functions directly from UI thread

### Error Handling
- Check returned strings for "failed" or "error" keywords
- Wrap all calls in try-catch blocks
- Log errors for debugging

### File Paths
- Use absolute paths for sketch directories
- Ensure output directories are writable
- Handle Android storage permissions properly

### Memory Management
- The JNI bridge handles memory automatically
- No need to manually free resources
- Large output buffers (8KB) are handled efficiently

## 🔍 Debugging Tips

### Enable Logging
```java
// Log all Arduino CLI output
String result = arduinoCLI.compileSketch(fqbn, sketchDir, outDir);
Log.d("ArduinoCLI", "Full output: " + result);

// Check for specific patterns
if (result.contains("error")) {
    Log.e("ArduinoCLI", "Error detected: " + result);
}
```

### Test Individual Functions
```java
// Test initialization
if (arduinoCLI.isInitialized()) {
    Log.d("Test", "Arduino CLI ready");
} else {
    Log.e("Test", "Arduino CLI not ready");
}

// Test board listing
String boards = arduinoCLI.listBoards();
Log.d("Test", "Boards: " + boards);
```

### Common Issues
1. **Library not found**: Check `jniLibs` directory structure
2. **Permission denied**: Verify manifest permissions and runtime requests
3. **Compilation fails**: Check sketch syntax and FQBN compatibility
4. **Upload fails**: Verify USB connection and board compatibility

## 📚 Resources

- **Full Integration Guide**: `ANDROID_INTEGRATION.md`
- **Project Template**: `android_project_template/`
- **Setup Script**: `setup_android_project.sh`
- **Example Code**: `ArduinoCLIExample.java`

---

**🚀 Your Arduino CLI library is now fully integrated into Android!**

Use this quick reference to get started quickly. For detailed information, see the full integration guide.
