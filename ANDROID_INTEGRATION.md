# Android Integration Guide for Arduino CLI Go Library

This guide provides step-by-step instructions for integrating the Arduino CLI Go library into your Android application using the JNI bridge.

## 📱 Prerequisites

- Android Studio 4.0+
- Android SDK API 21+ (Android 5.0)
- Android NDK (for building native libraries)
- Java 8 or higher
- Arduino CLI binary (included in the library)

## 🏗️ Project Setup

### 1. Create New Android Project

```bash
# Create a new Android project in Android Studio
# Or use command line:
android create project \
  --name ArduinoCLIApp \
  --package com.example.arduinocliapp \
  --target android-21 \
  --path ./ArduinoCLIApp
```

### 2. Project Structure

Your Android project should have this structure:

```
ArduinoCLIApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── example/
│   │   │   │           └── arduinocliapp/
│   │   │   │               ├── MainActivity.java
│   │   │   │               └── ArduinoCLIBridge.java
│   │   │   ├── jniLibs/
│   │   │   │   ├── arm64-v8a/
│   │   │   │   │   ├── libarduino_cli_go.so
│   │   │   │   │   └── libarduino_cli_jni.so
│   │   │   │   ├── armeabi-v7a/
│   │   │   │   │   ├── libarduino_cli_go.so
│   │   │   │   │   └── libarduino_cli_jni_arm32.so
│   │   │   │   └── x86_64/
│   │   │   │       ├── libarduino_cli_go.so
│   │   │   │       └── libarduino_cli_jni_x86_64.so
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   └── activity_main.xml
│   │   │   │   └── values/
│   │   │   │       └── strings.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## 🔧 Step-by-Step Integration

### Step 1: Copy Native Libraries

1. **Create jniLibs directories:**
```bash
cd app/src/main
mkdir -p jniLibs/arm64-v8a
mkdir -p jniLibs/armeabi-v7a
mkdir -p jniLibs/x86_64
```

2. **Copy the compiled libraries:**
```bash
# From your arduino-go-lib directory
cp libarduino_cli_go.so app/src/main/jniLibs/arm64-v8a/
cp libarduino_cli_jni.so app/src/main/jniLibs/arm64-v8a/

cp libarduino_cli_go.so app/src/main/jniLibs/armeabi-v7a/
cp libarduino_cli_jni_arm32.so app/src/main/jniLibs/armeabi-v7a/

cp libarduino_cli_go.so app/src/main/jniLibs/x86_64/
cp libarduino_cli_jni_x86_64.so app/src/main/jniLibs/x86_64/
```

### Step 2: Add Java Interface

1. **Copy ArduinoCLIBridge.java to your project:**
```bash
cp ArduinoCLIBridge.java app/src/main/java/com/example/arduinocliapp/
```

2. **Update the package declaration in ArduinoCLIBridge.java:**
```java
package com.example.arduinocliapp;
```

### Step 3: Update AndroidManifest.xml

Add required permissions and features:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.arduinocliapp">

    <!-- Required permissions -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.USB_PERMISSION" />
    <uses-permission android:name="android.permission.INTERNET" />
    
    <!-- USB features -->
    <uses-feature android:name="android.hardware.usb.host" />
    <uses-feature android:name="android.hardware.usb.accessory" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        
        <activity android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            
            <!-- USB device filter -->
            <intent-filter>
                <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
            </intent-filter>
            <meta-data
                android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
                android:resource="@xml/device_filter" />
        </activity>
    </application>
</manifest>
```

### Step 4: Create Device Filter

Create `app/src/main/res/xml/device_filter.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Arduino Uno -->
    <usb-device vendor-id="9025" product-id="67" />
    <!-- Arduino Nano -->
    <usb-device vendor-id="9025" product-id="67" />
    <!-- Arduino Mega -->
    <usb-device vendor-id="9025" product-id="66" />
    <!-- ESP32 -->
    <usb-device vendor-id="4292" product-id="60000" />
    <!-- ESP8266 -->
    <usb-device vendor-id="4292" product-id="60000" />
</resources>
```

### Step 5: Update build.gradle

Add NDK support to `app/build.gradle`:

```gradle
android {
    compileSdkVersion 33
    buildToolsVersion "33.0.0"
    
    defaultConfig {
        applicationId "com.example.arduinocliapp"
        minSdkVersion 21
        targetSdkVersion 33
        versionCode 1
        versionName "1.0"
        
        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a', 'x86_64'
        }
    }
    
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    // Enable NDK
    ndkVersion "25.1.8937393"
    
    // External native build
    externalNativeBuild {
        cmake {
            path "CMakeLists.txt"
        }
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'com.google.android.material:material:1.9.0'
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

### Step 6: Create MainActivity

Create `app/src/main/java/com/example/arduinocliapp/MainActivity.java`:

```java
package com.example.arduinocliapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String ACTION_USB_PERMISSION = "com.example.arduinocliapp.USB_PERMISSION";
    
    private ArduinoCLIBridge arduinoCLI;
    private TextView outputText;
    private Spinner boardSpinner, portSpinner;
    private Button compileButton, uploadButton, listBoardsButton, listCoresButton;
    private Button connectButton, disconnectButton;
    
    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private UsbDeviceConnection usbConnection;
    private List<String> availablePorts = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        initializeViews();
        
        // Initialize Arduino CLI
        initializeArduinoCLI();
        
        // Initialize USB manager
        initializeUSBManager();
        
        // Check permissions
        checkPermissions();
        
        // Set up button listeners
        setupButtonListeners();
        
        // Populate board spinner
        populateBoardSpinner();
    }
    
    private void initializeViews() {
        outputText = findViewById(R.id.output_text);
        boardSpinner = findViewById(R.id.board_spinner);
        portSpinner = findViewById(R.id.port_spinner);
        compileButton = findViewById(R.id.compile_button);
        uploadButton = findViewById(R.id.upload_button);
        listBoardsButton = findViewById(R.id.list_boards_button);
        listCoresButton = findViewById(R.id.list_cores_button);
        connectButton = findViewById(R.id.connect_button);
        disconnectButton = findViewById(R.id.disconnect_button);
    }
    
    private void initializeArduinoCLI() {
        arduinoCLI = new ArduinoCLIBridge();
        new Thread(() -> {
            try {
                int result = arduinoCLI.initArduinoCLI();
                runOnUiThread(() -> {
                    if (result == 0) {
                        outputText.setText("Arduino CLI initialized successfully!");
                        enableButtons(true);
                    } else {
                        outputText.setText("Failed to initialize Arduino CLI");
                        enableButtons(false);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Arduino CLI", e);
                runOnUiThread(() -> {
                    outputText.setText("Error initializing Arduino CLI: " + e.getMessage());
                    enableButtons(false);
                });
            }
        }).start();
    }
    
    private void initializeUSBManager() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        
        // Register USB permission receiver
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                PERMISSION_REQUEST_CODE);
        }
    }
    
    private void setupButtonListeners() {
        // List Boards Button
        listBoardsButton.setOnClickListener(v -> {
            outputText.setText("Listing boards...");
            new Thread(() -> {
                try {
                    String result = arduinoCLI.listBoards();
                    runOnUiThread(() -> {
                        outputText.setText("Available Boards:\n" + result);
                        updatePortSpinner(result);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error listing boards", e);
                    runOnUiThread(() -> outputText.setText("Error listing boards: " + e.getMessage()));
                }
            }).start();
        });
        
        // List Cores Button
        listCoresButton.setOnClickListener(v -> {
            outputText.setText("Listing cores...");
            new Thread(() -> {
                try {
                    String result = arduinoCLI.listCores();
                    runOnUiThread(() -> outputText.setText("Installed Cores:\n" + result));
                } catch (Exception e) {
                    Log.e(TAG, "Error listing cores", e);
                    runOnUiThread(() -> outputText.setText("Error listing cores: " + e.getMessage()));
                }
            }).start();
        });
        
        // Compile Button
        compileButton.setOnClickListener(v -> {
            String selectedBoard = boardSpinner.getSelectedItem().toString();
            outputText.setText("Compiling sketch for " + selectedBoard + "...");
            
            new Thread(() -> {
                try {
                    String sketchDir = createTestSketch();
                    String outDir = sketchDir + "/build";
                    
                    String result = arduinoCLI.compileSketch(selectedBoard, sketchDir, outDir);
                    runOnUiThread(() -> outputText.setText("Compilation Result:\n" + result));
                } catch (Exception e) {
                    Log.e(TAG, "Error compiling sketch", e);
                    runOnUiThread(() -> outputText.setText("Error compiling sketch: " + e.getMessage()));
                }
            }).start();
        });
        
        // Upload Button
        uploadButton.setOnClickListener(v -> {
            String selectedBoard = boardSpinner.getSelectedItem().toString();
            String selectedPort = portSpinner.getSelectedItem().toString();
            
            if (selectedPort.equals("No ports available")) {
                Toast.makeText(this, "Please connect a board first", Toast.LENGTH_SHORT).show();
                return;
            }
            
            outputText.setText("Uploading to " + selectedBoard + " via " + selectedPort + "...");
            
            new Thread(() -> {
                try {
                    String sketchDir = getExternalFilesDir(null) + "/test_sketch";
                    String hexFile = sketchDir + "/build/test_sketch.ino.hex";
                    
                    String result = arduinoCLI.uploadHex(hexFile, selectedPort, selectedBoard);
                    runOnUiThread(() -> outputText.setText("Upload Result:\n" + result));
                } catch (Exception e) {
                    Log.e(TAG, "Error uploading sketch", e);
                    runOnUiThread(() -> outputText.setText("Error uploading sketch: " + e.getMessage()));
                }
            }).start();
        });
        
        // Connect Button
        connectButton.setOnClickListener(v -> {
            if (usbDevice != null) {
                requestUsbPermission(usbDevice);
            } else {
                Toast.makeText(this, "No USB device detected", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Disconnect Button
        disconnectButton.setOnClickListener(v -> {
            if (usbConnection != null) {
                usbConnection.close();
                usbConnection = null;
                usbDevice = null;
                availablePorts.clear();
                updatePortSpinner("");
                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void populateBoardSpinner() {
        String[] boards = arduinoCLI.getCommonBoards();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, boards);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boardSpinner.setAdapter(adapter);
    }
    
    private void updatePortSpinner(String boardListOutput) {
        availablePorts.clear();
        
        // Parse board list output to extract ports
        String[] lines = boardListOutput.split("\n");
        for (String line : lines) {
            if (line.contains("/dev/") || line.contains("COM")) {
                String[] parts = line.split("\\s+");
                if (parts.length > 0) {
                    availablePorts.add(parts[0]);
                }
            }
        }
        
        if (availablePorts.isEmpty()) {
            availablePorts.add("No ports available");
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, availablePorts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        portSpinner.setAdapter(adapter);
    }
    
    private String createTestSketch() throws IOException {
        String sketchDir = getExternalFilesDir(null) + "/test_sketch";
        File dir = new File(sketchDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        File sketchFile = new File(dir, "test_sketch.ino");
        try (FileWriter writer = new FileWriter(sketchFile)) {
            writer.write("void setup() {\n");
            writer.write("  Serial.begin(9600);\n");
            writer.write("  pinMode(13, OUTPUT);\n");
            writer.write("}\n\n");
            writer.write("void loop() {\n");
            writer.write("  digitalWrite(13, HIGH);\n");
            writer.write("  delay(1000);\n");
            writer.write("  digitalWrite(13, LOW);\n");
            writer.write("  delay(1000);\n");
            writer.write("  Serial.println(\"Hello from Arduino!\");\n");
            writer.write("}\n");
        }
        
        return sketchDir;
    }
    
    private void requestUsbPermission(UsbDevice device) {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, 
            new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        usbManager.requestPermission(device, permissionIntent);
    }
    
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            usbDevice = device;
                            usbConnection = usbManager.openDevice(device);
                            if (usbConnection != null) {
                                Toast.makeText(MainActivity.this, "USB device connected", Toast.LENGTH_SHORT).show();
                                // Update port spinner with connected device
                                availablePorts.clear();
                                availablePorts.add("/dev/ttyUSB0"); // Simulated port
                                updatePortSpinner("");
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "USB permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };
    
    private void enableButtons(boolean enabled) {
        compileButton.setEnabled(enabled);
        uploadButton.setEnabled(enabled);
        listBoardsButton.setEnabled(enabled);
        listCoresButton.setEnabled(enabled);
        connectButton.setEnabled(enabled);
        disconnectButton.setEnabled(enabled);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
        if (usbConnection != null) {
            usbConnection.close();
        }
    }
}
```

### Step 7: Create Layout File

Create `app/src/main/res/layout/activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    tools:context=".MainActivity">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Arduino CLI Integration"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_gravity="center"
        android:layout_marginBottom="16dp" />

    <!-- Board Selection -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginBottom="8dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Board: "
            android:layout_gravity="center_vertical" />

        <Spinner
            android:id="@+id/board_spinner"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1" />
    </LinearLayout>

    <!-- Port Selection -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginBottom="16dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Port: "
            android:layout_gravity="center_vertical" />

        <Spinner
            android:id="@+id/port_spinner"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1" />
    </LinearLayout>

    <!-- Control Buttons -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginBottom="8dp">

        <Button
            android:id="@+id/connect_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Connect"
            android:layout_marginEnd="4dp" />

        <Button
            android:id="@+id/disconnect_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Disconnect"
            android:layout_marginStart="4dp" />
    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginBottom="8dp">

        <Button
            android:id="@+id/list_boards_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="List Boards"
            android:layout_marginEnd="4dp" />

        <Button
            android:id="@+id/list_cores_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="List Cores"
            android:layout_marginStart="4dp" />
    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginBottom="16dp">

        <Button
            android:id="@+id/compile_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Compile"
            android:layout_marginEnd="4dp" />

        <Button
            android:id="@+id/upload_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Upload"
            android:layout_marginStart="4dp" />
    </LinearLayout>

    <!-- Output Text -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:background="#f5f5f5"
        android:padding="8dp">

        <TextView
            android:id="@+id/output_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Arduino CLI ready. Click 'List Boards' to start."
            android:textSize="12sp"
            android:fontFamily="monospace" />
    </ScrollView>

</LinearLayout>
```

### Step 8: Update strings.xml

Update `app/src/main/res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">Arduino CLI App</string>
</resources>
```

## 🚀 Building and Running

### 1. Build the Project

```bash
# In Android Studio: Build > Make Project
# Or command line:
./gradlew assembleDebug
```

### 2. Install on Device

```bash
# Connect Android device with USB debugging enabled
./gradlew installDebug
```

### 3. Run the App

- Launch the app on your device
- Grant storage permissions when prompted
- Connect an Arduino board via USB
- Use the app to compile and upload sketches

## 🔍 Testing the Integration

### 1. Basic Functionality Test

```java
// Test Arduino CLI initialization
if (arduinoCLI.isInitialized()) {
    Log.d("Test", "Arduino CLI initialized successfully");
} else {
    Log.e("Test", "Arduino CLI initialization failed");
}
```

### 2. Compilation Test

```java
// Test sketch compilation
String result = arduinoCLI.compileSketch(
    "arduino:avr:uno",
    "/path/to/test/sketch",
    "/path/to/output"
);
Log.d("Test", "Compilation result: " + result);
```

### 3. Board Detection Test

```java
// Test board listing
String boards = arduinoCLI.listBoards();
Log.d("Test", "Available boards: " + boards);
```

## 🐛 Troubleshooting

### Common Issues and Solutions

1. **Library not found error:**
   - Ensure `.so` files are in correct `jniLibs` directories
   - Check that library names match exactly

2. **Permission denied errors:**
   - Verify Android manifest permissions
   - Check runtime permission requests
   - Ensure USB permissions are granted

3. **Compilation failures:**
   - Verify sketch syntax
   - Check FQBN compatibility
   - Ensure output directory is writable

4. **Upload failures:**
   - Check USB connection
   - Verify board compatibility
   - Ensure proper port selection

### Debug Mode

Enable verbose logging:

```java
// Check detailed output from Arduino CLI
String result = arduinoCLI.compileSketch(fqbn, sketchDir, outDir);
Log.d("ArduinoCLI", "Full output: " + result);

// Check for specific error patterns
if (result.contains("error") || result.contains("failed")) {
    Log.e("ArduinoCLI", "Operation failed: " + result);
}
```

## 📱 Advanced Features

### 1. Real-time Serial Monitor

```java
// Implement serial communication for real-time monitoring
// This would require additional USB serial library integration
```

### 2. Multiple Board Support

```java
// Handle different board types dynamically
String[] boards = arduinoCLI.getCommonBoards();
for (String board : boards) {
    // Test compatibility and show available options
}
```

### 3. Library Management

```java
// Install and manage Arduino libraries
String result = arduinoCLI.installLibrary("WiFi");
Log.d("ArduinoCLI", "Library installation: " + result);
```

## 🎯 Next Steps

1. **Test with real Arduino boards**
2. **Implement error handling UI**
3. **Add progress indicators for long operations**
4. **Implement sketch editor**
5. **Add board configuration options**
6. **Implement library manager UI**

## 📚 Additional Resources

- [Android NDK Documentation](https://developer.android.com/ndk)
- [JNI Specification](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)
- [Arduino CLI Documentation](https://arduino.github.io/arduino-cli/)
- [Android USB Host API](https://developer.android.com/guide/topics/connectivity/usb/host)

---

**Your Arduino CLI Go library is now fully integrated into Android! 🎉**

The integration provides a complete Arduino development environment within your Android app, allowing users to compile, upload, and manage Arduino projects directly from their mobile devices.
