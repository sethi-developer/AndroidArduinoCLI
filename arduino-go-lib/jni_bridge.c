#include "jni_bridge.h"
#include "libarduino_cli_go.h"
#include <string.h>
#include <stdlib.h>

// Helper function to convert Java string to C string
char* jstring_to_cstring(JNIEnv *env, jstring jstr) {
    if (jstr == NULL) return NULL;
    
    const char *cstr = env->GetStringUTFChars(jstr, NULL);
    if (cstr == NULL) return NULL;
    
    char *result = strdup(cstr);
    env->ReleaseStringUTFChars(jstr, cstr);
    
    return result;
}

// Helper function to create Java string from C string
jstring cstring_to_jstring(JNIEnv *env, const char *cstr) {
    if (cstr == NULL) return env->NewStringUTF("");
    return env->NewStringUTF(cstr);
}

// Arduino CLI initialization
JNIEXPORT jint JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeInitArduinoCLI(JNIEnv *env, jobject obj) {
    return GoInitArduinoCLI();
}

// Set Arduino data directory
JNIEXPORT jint JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeSetArduinoDataDir(JNIEnv *env, jobject obj, jstring dataDir) {
    char *dataDir_c = jstring_to_cstring(env, dataDir);
    
    if (!dataDir_c) {
        return -1;
    }
    
    int result = GoSetArduinoDataDir(dataDir_c);
    free(dataDir_c);
    
    return result;
}

// Sketch compilation
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeCompileSketch(
    JNIEnv *env, jobject obj, 
    jstring fqbn, jstring sketchDir, jstring outDir
) {
    char *fqbn_c = jstring_to_cstring(env, fqbn);
    char *sketchDir_c = jstring_to_cstring(env, sketchDir);
    char *outDir_c = jstring_to_cstring(env, outDir);
    
    if (!fqbn_c || !sketchDir_c || !outDir_c) {
        if (fqbn_c) free(fqbn_c);
        if (sketchDir_c) free(sketchDir_c);
        if (outDir_c) free(outDir_c);
        return cstring_to_jstring(env, "Error: Invalid parameters");
    }
    
    char output[8192];
    int result = GoCompileSketch(fqbn_c, sketchDir_c, outDir_c, output, sizeof(output));
    
    free(fqbn_c);
    free(sketchDir_c);
    free(outDir_c);
    
    if (result != 0) {
        return cstring_to_jstring(env, "Compilation failed");
    }
    
    return cstring_to_jstring(env, output);
}

// Hex file upload
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeUploadHex(
    JNIEnv *env, jobject obj, 
    jstring hexPath, jstring port, jstring fqbn
) {
    char *hexPath_c = jstring_to_cstring(env, hexPath);
    char *port_c = jstring_to_cstring(env, port);
    char *fqbn_c = jstring_to_cstring(env, fqbn);
    
    if (!hexPath_c || !port_c || !fqbn_c) {
        if (hexPath_c) free(hexPath_c);
        if (port_c) free(port_c);
        if (fqbn_c) free(fqbn_c);
        return cstring_to_jstring(env, "Upload failed");
    }
    
    char output[8192];
    int result = GoUploadHex(hexPath_c, port_c, fqbn_c, output, sizeof(output));
    
    free(hexPath_c);
    free(port_c);
    free(fqbn_c);
    
    if (result != 0) {
        return cstring_to_jstring(env, "Upload failed");
    }
    
    return cstring_to_jstring(env, output);
}

// List boards
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeListBoards(JNIEnv *env, jobject obj) {
    char output[8192];
    int result = GoListBoards(output, sizeof(output));
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to list boards");
    }
    
    return cstring_to_jstring(env, output);
}

// Get board info
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeGetBoardInfo(
    JNIEnv *env, jobject obj, jstring fqbn
) {
    char *fqbn_c = jstring_to_cstring(env, fqbn);
    
    if (!fqbn_c) {
        return cstring_to_jstring(env, "Error: Invalid FQBN");
    }
    
    char output[8192];
    int result = GoGetBoardInfo(fqbn_c, output, sizeof(output));
    
    free(fqbn_c);
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to get board info");
    }
    
    return cstring_to_jstring(env, output);
}

// List cores
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeListCores(JNIEnv *env, jobject obj) {
    char output[8192];
    int result = GoListCores(output, sizeof(output));
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to list cores");
    }
    
    return cstring_to_jstring(env, output);
}

// Install core
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeInstallCore(
    JNIEnv *env, jobject obj, jstring coreName
) {
    char *coreName_c = jstring_to_cstring(env, coreName);
    
    if (!coreName_c) {
        return cstring_to_jstring(env, "Error: Invalid core name");
    }
    
    char output[8192];
    int result = GoInstallCore(coreName_c, output, sizeof(output));
    
    free(coreName_c);
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to install core");
    }
    
    return cstring_to_jstring(env, output);
}

// Update index
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeUpdateIndex(JNIEnv *env, jobject obj) {
    char output[8192];
    int result = GoUpdateIndex(output, sizeof(output));
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to update index");
    }
    
    return cstring_to_jstring(env, output);
}

// List libraries
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeListLibraries(JNIEnv *env, jobject obj) {
    char output[8192];
    int result = GoListLibraries(output, sizeof(output));
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to list libraries");
    }
    
    return cstring_to_jstring(env, output);
}

// Install library
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeInstallLibrary(
    JNIEnv *env, jobject obj, jstring libName
) {
    char *libName_c = jstring_to_cstring(env, libName);
    
    if (!libName_c) {
        return cstring_to_jstring(env, "Error: Invalid library name");
    }
    
    char output[8192];
    int result = GoInstallLibrary(libName_c, output, sizeof(output));
    
    free(libName_c);
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to install library");
    }
    
    return cstring_to_jstring(env, output);
}

// Install library from zip file
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeInstallLibraryFromZip(
    JNIEnv *env, jobject obj, jstring zipPath
) {
    char *zipPath_c = jstring_to_cstring(env, zipPath);
    
    if (!zipPath_c) {
        return cstring_to_jstring(env, "Error: Invalid zip file path");
    }
    
    char output[8192];
    int result = GoInstallLibraryFromZip(zipPath_c, output, sizeof(output));
    
    free(zipPath_c);
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to install library from zip");
    }
    
    return cstring_to_jstring(env, output);
}

// Uninstall library
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeUninstallLibrary(
    JNIEnv *env, jobject obj, jstring libName
) {
    char *libName_c = jstring_to_cstring(env, libName);
    
    if (!libName_c) {
        return cstring_to_jstring(env, "Error: Invalid library name");
    }
    
    char output[8192];
    int result = GoUninstallLibrary(libName_c, output, sizeof(output));
    
    free(libName_c);
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to uninstall library");
    }
    
    return cstring_to_jstring(env, output);
}

// Reload libraries
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeReloadLibraries(
    JNIEnv *env, jobject obj
) {
    char output[8192];
    int result = GoReloadLibraries(output, sizeof(output));
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to reload libraries");
    }
    
    return cstring_to_jstring(env, output);
}

// Search library
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeSearchLibrary(
    JNIEnv *env, jobject obj, jstring searchTerm
) {
    char *searchTerm_c = jstring_to_cstring(env, searchTerm);
    
    if (!searchTerm_c) {
        return cstring_to_jstring(env, "Error: Invalid search term");
    }
    
    char output[8192];
    int result = GoSearchLibrary(searchTerm_c, output, sizeof(output));
    
    free(searchTerm_c);
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to search library");
    }
    
    return cstring_to_jstring(env, output);
}

// Get library info
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeGetLibraryInfo(
    JNIEnv *env, jobject obj, jstring libName
) {
    char *libName_c = jstring_to_cstring(env, libName);
    
    if (!libName_c) {
        return cstring_to_jstring(env, "Error: Invalid library name");
    }
    
    char output[8192];
    int result = GoGetLibraryInfo(libName_c, output, sizeof(output));
    
    free(libName_c);
    
    if (result != 0) {
        return cstring_to_jstring(env, "Failed to get library info");
    }
    
    return cstring_to_jstring(env, output);
}

// Verify sketch
JNIEXPORT jstring JNICALL Java_com_demo_myarduinodroid_ArduinoCLIBridge_nativeVerifySketch(
    JNIEnv *env, jobject obj, jstring fqbn, jstring sketchDir
) {
    char *fqbn_c = jstring_to_cstring(env, fqbn);
    char *sketchDir_c = jstring_to_cstring(env, sketchDir);
    
    if (!fqbn_c || !sketchDir_c) {
        if (fqbn_c) free(fqbn_c);
        if (sketchDir_c) free(sketchDir_c);
        return cstring_to_jstring(env, "Error: Invalid parameters");
    }
    
    char output[8192];
    int result = GoVerifySketch(fqbn_c, sketchDir_c, output, sizeof(output));
    
    free(fqbn_c);
    free(sketchDir_c);
    
    if (result != 0) {
        return cstring_to_jstring(env, "Verification failed");
    }
    
    return cstring_to_jstring(env, output);
}
