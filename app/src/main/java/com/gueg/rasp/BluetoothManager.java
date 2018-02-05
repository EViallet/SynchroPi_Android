package com.gueg.rasp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
public class BluetoothManager {
    
    private static final String TAG = "BTManager";
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private MainActivity activity;
    private InputStream in;
    private OutputStream out;
    private static final long CLOCK = 200L;
    //private static final String PI_MAC = "B8:27:EB:F0:82:5B";
    private static final String PI_UUID = "00001111-0000-1000-8000-00805f9b34fb";

    BluetoothManager(MainActivity activity) {
        this.activity = activity;
        if(!bluetoothAdapter.isEnabled())
            activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
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
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            bluetoothHandler.postDelayed(this, CLOCK);
        }
    };


    public void initBluetooth() {
        try {
            UUID uuid = UUID.fromString(PI_UUID);
            BluetoothServerSocket server = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BTServer", uuid);
            BluetoothSocket socket = server.accept();
            in = socket.getInputStream();
            out = socket.getOutputStream();
            activity.connected();
            bluetoothHandler.post(checkIncomingData);
        } catch(IOException e) {
            Log.e(TAG,"Couldn't create BTServer :",e);
        }
    }


    @SuppressLint("DefaultLocale")
    public void send(String cmd) {
        try {
            out.write(cmd.getBytes());
            Log.d(TAG,"Sending : "+cmd);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return bluetoothAdapter.isEnabled();
    }


}
