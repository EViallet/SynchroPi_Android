package com.gueg.rasp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
public class BluetoothManager {
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private MainActivity activity;
    private InputStream in;
    private OutputStream out;
    private static final int CLOCK = 200;
    private static final long MAX_DELAY = 5000L;
    private static final String PI_MAC = "B8:27:EB:F0:82:5B";
    private long lastSent = 0;
    private long lastReceived = 0;

    BluetoothManager(MainActivity activity) {
        this.activity = activity;
        initBluetooth();
    }

    private Handler bluetoothHandler = new Handler();
    Runnable checkIncomingData = new Runnable() {
        @Override
        public void run() {
            try {
                if(in!=null) {
                    int byteCount = in.available();
                    if (byteCount > 0) {
                        byte[] rawBytes = new byte[byteCount];
                        in.read(rawBytes);
                        final String string = new String(rawBytes, "ASCII");
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                activity.setText(new SimpleDateFormat("HH':'mm':'ss",Locale.getDefault()).format(System.currentTimeMillis()) + " Reçu : " + string + "\n");
                            }
                        });
                        lastReceived = System.currentTimeMillis();
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            if(lastReceived>MAX_DELAY)

            //send(y);
            bluetoothHandler.postDelayed(this, CLOCK);
        }
    };


    public void initBluetooth() {
        if(!bluetoothAdapter.isEnabled())
            activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(PI_MAC);

        if(device!=null) {
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(device.getUuids()[2].getUuid());
                socket.connect();
                in = socket.getInputStream();
                out = socket.getOutputStream();
                bluetoothHandler.post(checkIncomingData);
                Log.d(":-:", "Connected");
                activity.connected();
            } catch(IOException e) {
                Log.e(":-:", "Socket creation failed");
                e.printStackTrace();
            }
        } else
            Toast.makeText(activity, "Raspberry non trouvé", Toast.LENGTH_SHORT).show();

    }


    @SuppressLint("DefaultLocale")
    public void send(String cmd) {
        try {
            out.write(cmd.getBytes());
            lastSent = System.currentTimeMillis();
        } catch(IOException e) {
            e.printStackTrace();
        }
        if(lastSent>MAX_DELAY)
            Toast.makeText(activity, "-", Toast.LENGTH_SHORT).show();
    }

}
