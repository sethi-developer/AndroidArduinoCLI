#ifndef JNI_BRIDGE_H
#define JNI_BRIDGE_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

// Arduino CLI functions
JNIEXPORT jint JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeInitArduinoCLI(JNIEnv *env, jobject obj);
JNIEXPORT jint JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeSetArduinoDataDir(JNIEnv *env, jobject obj, jstring dataDir);
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeCompileSketch(JNIEnv *env, jobject obj, jstring fqbn, jstring sketchDir, jstring outDir);
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeUploadHex(JNIEnv *env, jobject obj, jstring hexPath, jstring port, jstring fqbn);

// Board management functions
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeListBoards(JNIEnv *env, jobject obj);
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeGetBoardInfo(JNIEnv *env, jobject obj, jstring fqbn);

// Core management functions
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeListCores(JNIEnv *env, jobject obj);
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeInstallCore(JNIEnv *env, jobject obj, jstring coreName);

// Package management functions
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeUpdateIndex(JNIEnv *env, jobject obj);

// Library management functions
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeListLibraries(JNIEnv *env, jobject obj);
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeInstallLibrary(JNIEnv *env, jobject obj, jstring libName);
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeInstallLibraryFromZip(JNIEnv *env, jobject obj, jstring zipPath);
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeUninstallLibrary(JNIEnv *env, jobject obj, jstring libName);
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeSearchLibrary(JNIEnv *env, jobject obj, jstring searchTerm);
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeGetLibraryInfo(JNIEnv *env, jobject obj, jstring libName);

// Sketch verification function
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeVerifySketch(JNIEnv *env, jobject obj, jstring fqbn, jstring sketchDir);

#ifdef __cplusplus
}
#endif

#endif // JNI_BRIDGE_H
