package com.demo.myarduinodroid;

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
import java.util.List;

public class MainActivityJava extends AppCompatActivity {
    
    private static final String TAG = "MainActivityJava";
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String ACTION_USB_PERMISSION = "com.demo.myarduinodroid.USB_PERMISSION";
    
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
                    // The compileSketch method now automatically creates the build directory
                    String result = arduinoCLI.compileSketch(selectedBoard, sketchDir, "");
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
        
        // Also create the build directory
        File buildDir = new File(sketchDir, "build");
        if (!buildDir.exists()) {
            buildDir.mkdirs();
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
                                Toast.makeText(MainActivityJava.this, "USB device connected", Toast.LENGTH_SHORT).show();
                                // Update port spinner with connected device
                                availablePorts.clear();
                                availablePorts.add("/dev/ttyUSB0"); // Simulated port
                                updatePortSpinner("");
                            }
                        }
                    } else {
                        Toast.makeText(MainActivityJava.this, "USB permission denied", Toast.LENGTH_SHORT).show();
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
