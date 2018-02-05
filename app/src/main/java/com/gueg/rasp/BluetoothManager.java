package com.gueg.rasp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private static final String PI_MAC = "B8:27:EB:F0:82:5B";

    BluetoothManager(MainActivity activity) {
        this.activity = activity;
        if(bluetoothAdapter.isEnabled())
            initBluetooth();
        else
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
            UUID uuid = UUID.fromString("00001111-0000-1000-8000-00805f9b34fb");
            Log.d(TAG,""+uuid);
            BluetoothServerSocket server = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Server", uuid);
            BluetoothSocket socket = server.accept();
            String str = "abcd";
            socket.getOutputStream().write(str.getBytes());
        } catch(IOException e) {
            Log.e(TAG,"",e);
        }
        /*
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(PI_MAC);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord()
        ArrayList<BluetoothSocket> sockets = new ArrayList<>();
        String[] usedUuids = new String[2];
        if(device!=null) {
            for(ParcelUuid uuid : device.getUuids()) {
                try {
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid.getUuid());
                    socket.connect();
                    if(socket.isConnected()) {
                        sockets.add(socket);
                        if(sockets.size()<=2)
                            usedUuids[sockets.size()-1]=uuid.getUuid().toString();
                        Log.d(TAG, "Socket creation succeeded with uuid " + uuid.getUuid().toString());
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Socket creation failed with uuid "+uuid.getUuid().toString());
                }
            }
            Log.d(TAG,sockets.size()+" valid uuids found out of "+device.getUuids().length);
            if(sockets.size()<2) {
                Log.e(TAG,"Not enough UUIDs found.");
                return;
            }
            try {
                in = sockets.get(0).getInputStream();
                out = sockets.get(1).getOutputStream();
                bluetoothHandler.post(checkIncomingData);
                activity.connected(usedUuids);
                Toast.makeText(activity, "Connecté", Toast.LENGTH_SHORT).show();
            } catch(IOException e) {
                Log.e(TAG,"Failed to open io streams",e);
            }
        } else
            Toast.makeText(activity, "Raspberry non trouvé", Toast.LENGTH_SHORT).show();
        */
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
