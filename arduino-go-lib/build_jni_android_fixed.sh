#!/bin/bash
set -e

echo "Building Arduino CLI JNI Bridge for Android..."

# Set up environment
export ANDROID_NDK=$HOME/Library/Android/sdk/ndk/25.1.8937393
export JAVA_HOME=$(/usr/libexec/java_home)

echo "Using Android NDK: $ANDROID_NDK"
echo "Using Java Home: $JAVA_HOME"

# Clean previous builds
echo "Cleaning previous builds..."
rm -f libarduino_cli_jni*.so

# Build JNI bridge for Android ARM64
echo "Building JNI bridge for Android ARM64..."
cp libarduino_cli_go_arm64.h libarduino_cli_go.h
$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin/clang++ \
    -shared -fPIC \
    -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/darwin" \
    -I"$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include" \
    -I"$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/aarch64-linux-android" \
    -L. \
    -larduino_cli_go_arm64 \
    -o libarduino_cli_jni.so \
    jni_bridge.c \
    -target aarch64-linux-android21 \
    -static-libstdc++ \
    -static-libgcc

# Build JNI bridge for Android ARM32
echo "Building JNI bridge for Android ARM32..."
cp libarduino_cli_go_arm32.h libarduino_cli_go.h
$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin/clang++ \
    -shared -fPIC \
    -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/darwin" \
    -I"$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include" \
    -I"$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/armv7a-linux-androideabi" \
    -L. \
    -larduino_cli_go_arm32 \
    -o libarduino_cli_jni_arm32.so \
    jni_bridge.c \
    -target armv7a-linux-androideabi21 \
    -static-libstdc++ \
    -static-libgcc

# Build JNI bridge for Android x86_64
echo "Building JNI bridge for Android x86_64..."
cp libarduino_cli_go_x86_64.h libarduino_cli_go.h
$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin/clang++ \
    -shared -fPIC \
    -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/darwin" \
    -I"$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include" \
    -I"$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/x86_64-linux-android" \
    -L. \
    -larduino_cli_go_x86_64 \
    -o libarduino_cli_jni_x86_64.so \
    jni_bridge.c \
    -target x86_64-linux-android21 \
    -static-libstdc++ \
    -static-libgcc

# Clean up temporary header
rm -f libarduino_cli_go.h

echo "Build completed successfully!"
echo ""
echo "Generated files:"
echo "  - libarduino_cli_jni.so (JNI bridge for ARM64)"
echo "  - libarduino_cli_jni_arm32.so (JNI bridge for ARM32)"
echo "  - libarduino_cli_jni_x86_64.so (JNI bridge for x86_64)"
echo ""
echo "For Android integration:"
echo "  1. Copy libarduino_cli_go_arm64.so to app/src/main/jniLibs/arm64-v8a/"
echo "  2. Copy libarduino_cli_go_arm32.so to app/src/main/jniLibs/armeabi-v7a/"
echo "  3. Copy libarduino_cli_go_x86_64.so to app/src/main/jniLibs/x86_64/"
echo "  4. Copy the corresponding JNI bridge libraries"
echo "  5. Use ArduinoCLIBridge.java in your Android project"
