package com.demo.myarduinodroid;

import java.io.File;

/**
 * Arduino CLI Bridge - Java interface for the Arduino CLI Go library
 *
 * This class provides a bridge between Java/Android and the Arduino CLI
 * functionality implemented in Go. It handles all the JNI calls and provides
 * a clean Java API for Arduino development.
 */
public class ArduinoCLIBridge {

    // Load the native library
    static {
        System.loadLibrary("arduino_cli_jni");
    }

    /**
     * Initialize the Arduino CLI
     * @return 0 on success, -1 on failure
     */
    public native int nativeInitArduinoCLI();

    /**
     * Set the Arduino data directory
     * @param dataDir Path to the Arduino data directory
     * @return 0 on success, -1 on failure
     */
    public native int nativeSetArduinoDataDir(String dataDir);

    /**
     * Compile an Arduino sketch
     * @param fqbn Fully Qualified Board Name (e.g., "arduino:avr:uno")
     * @param sketchDir Directory containing the sketch
     * @param outDir Output directory for compiled files
     * @return Compilation output and status
     */
    public native String nativeCompileSketch(String fqbn, String sketchDir, String outDir);

    /**
     * Upload a hex file to an Arduino board
     * @param hexPath Path to the hex file
     * @param port Serial port (e.g., "/dev/ttyUSB0" or "COM3")
     * @param fqbn Fully Qualified Board Name
     * @return Upload output and status
     */
    public native String nativeUploadHex(String hexPath, String port, String fqbn);

    /**
     * List all available board ports
     * @return List of available boards and ports
     */
    public native String nativeListBoards();

    /**
     * Get detailed information about a specific board
     * @param fqbn Fully Qualified Board Name
     * @return Board information
     */
    public native String nativeGetBoardInfo(String fqbn);

    /**
     * List all installed Arduino cores
     * @return List of installed cores
     */
    public native String nativeListCores();

    /**
     * Install a new Arduino core
     * @param coreName Name of the core to install
     * @return Installation output and status
     */
    public native String nativeInstallCore(String coreName);

    /**
     * Update the package index
     * @return Update output and status
     */
    public native String nativeUpdateIndex();

    /**
     * List all installed libraries
     * @return List of installed libraries
     */
    public native String nativeListLibraries();

    /**
     * Install a new library
     * @param libName Name of the library to install
     * @return Installation output and status
     */
    public native String nativeInstallLibrary(String libName);
    public native String nativeInstallLibraryFromZip(String zipPath);
    public native String nativeUninstallLibrary(String libName);
    public native String nativeSearchLibrary(String searchTerm);
    public native String nativeGetLibraryInfo(String libName);
    public native String nativeVerifySketch(String fqbn, String sketchDir);

    /**
     * Ensure the build directory exists
     * @param sketchDir The sketch directory
     * @return The build directory path
     */
    private String ensureBuildDirectory(String sketchDir) {
        String buildDir = sketchDir + "/build";
        File buildDirectory = new File(buildDir);
        if (!buildDirectory.exists()) {
            buildDirectory.mkdirs();
        }
        return buildDir;
    }

    /**
     * Convenience method to compile and upload in one step
     * @param fqbn Fully Qualified Board Name
     * @param sketchDir Directory containing the sketch
     * @param port Serial port for upload
     * @return Combined output from compile and upload
     */
    public String compileAndUpload(String fqbn, String sketchDir, String port) {
        // Ensure build directory exists
        String outDir = ensureBuildDirectory(sketchDir);

        // Compile the sketch
        String compileResult = nativeCompileSketch(fqbn, sketchDir, outDir);
        if (compileResult.contains("failed") || compileResult.contains("error")) {
            return "Compilation failed: " + compileResult;
        }

        // Find the hex file
        String hexFile = outDir + "/" + new File(sketchDir).getName() + ".ino.hex";

        // Upload the hex file
        String uploadResult = nativeUploadHex(hexFile, port, fqbn);
        if (uploadResult.contains("failed") || uploadResult.contains("error")) {
            return "Upload failed: " + uploadResult;
        }

        return "Success!\nCompilation: " + compileResult + "\nUpload: " + uploadResult;
    }

    /**
     * Get a list of common Arduino board FQBNs
     * @return Array of common board FQBNs
     */
    public String[] getCommonBoards() {
        return new String[] {
                "arduino:avr:uno",
                "arduino:avr:nano",
                "arduino:avr:mega",
                "arduino:avr:leonardo",
                "arduino:avr:micro",
                "arduino:avr:pro",
                "arduino:avr:mini",
                "esp32:esp32:esp32",
                "esp8266:esp8266:nodemcuv2",
                "stm32:stm32:genericSTM32F103C"
        };
    }

    /**
     * Check if the library is properly initialized
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        try {
            return nativeInitArduinoCLI() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the native Arduino CLI library is available
     * @return true if available, false otherwise
     */
    public boolean isNativeLibraryAvailable() {
        try {
            // Try to call a simple native method to check availability
            nativeInitArduinoCLI();
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        } catch (Exception e) {
            // If we get any other exception, the library is available but may have other issues
            return true;
        }
    }

    /**
     * Get the status of the Arduino CLI library
     * @return Status message about the library
     */
    public String getLibraryStatus() {
        if (!isNativeLibraryAvailable()) {
            return "❌ Arduino CLI native library NOT AVAILABLE\n" +
                   "The native library (.so files) has not been implemented yet.\n" +
                   "You need to:\n" +
                   "1. Implement the Arduino CLI Go library for Android\n" +
                   "2. Create the JNI wrapper library\n" +
                   "3. Place the .so files in the jniLibs directories";
        } else if (!isInitialized()) {
            return "⚠️  Arduino CLI native library AVAILABLE but NOT INITIALIZED\n" +
                   "The library is present but failed to initialize properly.";
        } else {
            return "✅ Arduino CLI native library AVAILABLE and INITIALIZED\n" +
                   "Ready to compile and upload Arduino sketches!";
        }
    }

    // Convenience methods that call the native methods
    public int initArduinoCLI() { 
        try {
            return nativeInitArduinoCLI(); 
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native method not available: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Compile sketch with automatic build directory creation
     */
    public String compileSketch(String fqbn, String sketchDir, String outDir) { 
        try {
            // Ensure the output directory exists
            String actualOutDir = ensureBuildDirectory(sketchDir);
            return nativeCompileSketch(fqbn, sketchDir, actualOutDir); 
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Error during compilation: " + e.getMessage();
        }
    }
    
    public String uploadHex(String hexPath, String port, String fqbn) { 
        try {
            return nativeUploadHex(hexPath, port, fqbn); 
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }
    
    public String listBoards() { 
        try {
            return nativeListBoards(); 
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }
    
    public String getBoardInfo(String fqbn) { 
        try {
            return nativeGetBoardInfo(fqbn); 
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }
    
    public String listCores() { 
        try {
            return nativeListCores(); 
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }
    
    public String installCore(String coreName) { 
        try {
            return nativeInstallCore(coreName); 
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }
    
    public String updateIndex() { 
        try {
            return nativeUpdateIndex(); 
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }
    
    public String listLibraries() { 
        try {
            return nativeListLibraries(); 
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }
    
    public String installLibrary(String libName) { 
        try {
            return nativeInstallLibrary(libName); 
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }

    public String installLibraryFromZip(String zipPath) {
        try {
            return nativeInstallLibraryFromZip(zipPath);
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }

    public String uninstallLibrary(String libName) {
        try {
            return nativeUninstallLibrary(libName);
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }

    public String searchLibrary(String searchTerm) {
        try {
            return nativeSearchLibrary(searchTerm);
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }

    public String getLibraryInfo(String libName) {
        try {
            return nativeGetLibraryInfo(libName);
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }
    
    public String verifySketch(String fqbn, String sketchDir) { 
        try {
            return nativeVerifySketch(fqbn, sketchDir); 
        } catch (UnsatisfiedLinkError e) {
            return "Error: Arduino CLI native library not available.\n" +
                   "Please implement the native Arduino CLI library first.\n" +
                   "Error: " + e.getMessage();
        }
    }

    /**
     * Set the Arduino data directory to use Android emulated storage
     * @param context Android context to get external files directory
     * @return 0 on success, -1 on failure
     */
    public int setArduinoDataDir(android.content.Context context) {
        try {
            String arduinoDataDir = context.getExternalFilesDir(null).getAbsolutePath() + "/arduino_data";
            return nativeSetArduinoDataDir(arduinoDataDir);
        } catch (UnsatisfiedLinkError e) {
            return -1;
        }
    }
}
