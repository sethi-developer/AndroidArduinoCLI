#!/bin/bash
set -e

echo "Building Arduino CLI Go Library for Android..."

# Set up environment
export ANDROID_NDK=$HOME/Library/Android/sdk/ndk/25.1.8937393
export JAVA_HOME=$(/usr/libexec/java_home)

echo "Using Android NDK: $ANDROID_NDK"
echo "Using Java Home: $JAVA_HOME"

# Clean previous builds
echo "Cleaning previous builds..."
rm -f libarduino_cli_go.so libarduino_cli_go.h

# Build Go library for Android ARM64
echo "Building Go library for Android ARM64..."
GOOS=android GOARCH=arm64 CGO_ENABLED=1 \
CC=$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin/aarch64-linux-android21-clang \
go build -buildmode=c-shared -o libarduino_cli_go_arm64.so main.go

# Build Go library for Android ARM32
echo "Building Go library for Android ARM32..."
GOOS=android GOARCH=arm CGO_ENABLED=1 \
CC=$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin/armv7a-linux-androideabi21-clang \
go build -buildmode=c-shared -o libarduino_cli_go_arm32.so main.go

# Build Go library for Android x86_64
echo "Building Go library for Android x86_64..."
GOOS=android GOARCH=amd64 CGO_ENABLED=1 \
CC=$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin/x86_64-linux-android21-clang \
go build -buildmode=c-shared -o libarduino_cli_go_x86_64.so main.go

echo "Go libraries built successfully!"
echo ""
echo "Generated files:"
echo "  - libarduino_cli_go_arm64.so (ARM64)"
echo "  - libarduino_cli_go_arm32.so (ARM32)"
echo "  - libarduino_cli_go_x86_64.so (x86_64)"
echo ""
echo "Now run: ./build_jni_android.sh"
