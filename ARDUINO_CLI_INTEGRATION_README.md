# Arduino CLI Android Integration

This project demonstrates the integration of Arduino CLI into an Android application using JNI (Java Native Interface).

## 🏗️ What's Been Implemented

### 1. Project Structure
- ✅ Updated `build.gradle` with NDK support
- ✅ Created `jniLibs` directories for multiple architectures
- ✅ Added required permissions in `AndroidManifest.xml`
- ✅ Created USB device filter XML

### 2. Core Components
- ✅ `ArduinoCLIBridge.java` - JNI interface to Arduino CLI
- ✅ `MainActivity.kt` - Jetpack Compose-based UI
- ✅ `MainActivityJava.java` - Traditional Java-based UI (alternative)
- ✅ `activity_main.xml` - Traditional XML layout

### 3. Features
- ✅ Board selection and management
- ✅ Port detection and USB connectivity
- ✅ Sketch compilation
- ✅ Hex file upload
- ✅ Core and library management
- ✅ Permission handling
- ✅ Background thread operations

## 🚧 What Needs to Be Completed

### 1. Native Libraries
The current implementation includes placeholder files for the native libraries. You need to:

1. **Compile Arduino CLI Go library for Android:**
   ```bash
   # Cross-compile for Android architectures
   GOOS=android GOARCH=arm64 go build -o libarduino_cli_go.so
   GOOS=android GOARCH=arm go build -o libarduino_cli_go.so
   GOOS=android GOARCH=amd64 go build -o libarduino_cli_go.so
   ```

2. **Compile JNI bridge library:**
   ```bash
   # Use Android NDK to compile the JNI wrapper
   ndk-build
   ```

3. **Replace placeholder files:**
   - `app/src/main/jniLibs/arm64-v8a/libarduino_cli_go.so`
   - `app/src/main/jniLibs/arm64-v8a/libarduino_cli_jni.so`
   - `app/src/main/jniLibs/armeabi-v7a/libarduino_cli_go.so`
   - `app/src/main/jniLibs/armeabi-v7a/libarduino_cli_jni_arm32.so`
   - `app/src/main/jniLibs/x86_64/libarduino_cli_go.so`
   - `app/src/main/jniLibs/x86_64/libarduino_cli_jni_x86_64.so`

### 2. Arduino CLI Go Library
You need to implement the actual Arduino CLI functionality in Go:

```go
package main

import (
    "C"
    "arduino.cc/cli/v2"
)

//export initArduinoCLI
func initArduinoCLI() C.int {
    // Initialize Arduino CLI
    return 0
}

//export compileSketch
func compileSketch(fqbn, sketchDir, outDir *C.char) *C.char {
    // Implement sketch compilation
    return C.CString("Compilation successful")
}

// Add other exported functions...
```

### 3. JNI Wrapper
Create a C/C++ JNI wrapper:

```cpp
#include <jni.h>
#include "arduino_cli_go.h"

extern "C" {
    JNIEXPORT jint JNICALL
    Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeInitArduinoCLI(JNIEnv *env, jobject obj) {
        return initArduinoCLI();
    }
    
    JNIEXPORT jstring JNICALL
    Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeCompileSketch(
        JNIEnv *env, jobject obj, jstring fqbn, jstring sketchDir, jstring outDir) {
        // Implement JNI wrapper
        return env->NewStringUTF("Compilation result");
    }
    
    // Add other JNI functions...
}
```

## 🔧 Building and Testing

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

### 3. Test Functionality
- Launch the app
- Grant storage permissions
- Connect an Arduino board via USB
- Test board detection, compilation, and upload

## 📱 UI Options

### Option 1: Jetpack Compose (Current)
- Modern, declarative UI
- Better performance
- Easier state management
- File: `MainActivity.kt`

### Option 2: Traditional XML Layout
- Familiar Android development approach
- Better compatibility with older devices
- File: `MainActivityJava.java`

## 🐛 Troubleshooting

### Common Issues
1. **Library not found errors:**
   - Ensure `.so` files are in correct `jniLibs` directories
   - Check library names match exactly

2. **Permission denied errors:**
   - Verify manifest permissions
   - Check runtime permission requests
   - Ensure USB permissions are granted

3. **Compilation failures:**
   - Verify sketch syntax
   - Check FQBN compatibility
   - Ensure output directory is writable

### Debug Mode
Enable verbose logging in the ArduinoCLIBridge:
```java
Log.d("ArduinoCLI", "Full output: " + result);
```

## 🎯 Next Steps

1. **Implement native libraries:**
   - Compile Arduino CLI Go library for Android
   - Create JNI wrapper library
   - Replace placeholder files

2. **Test with real hardware:**
   - Connect Arduino boards
   - Test compilation and upload
   - Verify USB communication

3. **Enhance functionality:**
   - Add real-time serial monitor
   - Implement library manager
   - Add sketch editor
   - Support for more board types

4. **Performance optimization:**
   - Optimize native library calls
   - Implement caching for board lists
   - Add progress indicators

## 📚 Resources

- [Android NDK Documentation](https://developer.android.com/ndk)
- [JNI Specification](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)
- [Arduino CLI Documentation](https://arduino.github.io/arduino-cli/)
- [Android USB Host API](https://developer.android.com/guide/topics/connectivity/usb/host)
- [Go Cross-Compilation](https://golang.org/doc/install/source#environment)

## 🚀 Getting Started

1. **Complete the native library implementation**
2. **Build and test the project**
3. **Connect real Arduino hardware**
4. **Customize the UI and functionality**

---

**Your Arduino CLI Android integration is ready for the final native library implementation! 🎉**

The foundation is complete - you just need to add the actual Arduino CLI Go library and JNI wrapper to make it fully functional.
