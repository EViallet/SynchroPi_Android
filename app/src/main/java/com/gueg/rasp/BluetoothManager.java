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
import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
public class BluetoothManager {

    private static final String SEP_ID = "&";
    private static final String SEP_DBG = "-";
    private static final String SEP_INT = "#";
    private static final String SEP_BOOL = "$";
    private static final String SEP_STR = "*";

    
    private static final String TAG = "BTManager";
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private MainActivity activity;
    private InputStream in;
    private OutputStream out;
    private StringBuilder buffer = new StringBuilder();
    private boolean isConnected = false;
    private static final long CLOCK = 200L;
    //private static final String PI_MAC = "B8:27:EB:F0:82:5B";
    private static final String PI_UUID = "00001111-0000-1000-8000-00805f9b34fb";

    private Handler bluetoothHandler = new Handler();

    Runnable checkIncomingData = new Runnable() {
        @Override
        public void run() {
            if(!isConnected)
                return;
            try {
                int byteCount = in.available();
                if (byteCount > 0) {
                    Log.d(TAG,"Receiving "+byteCount+" bytes...");
                    byte[] raw = new byte[byteCount];
                    in.read(raw);
                    final String string = new String(raw, "ASCII");
                    if(!isComplete(string))
                        buffer.append(string);
                    if(isComplete(string)||isComplete(buffer.toString())) {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d(TAG,"Reçu : "+string);
                            }
                        });
                        buffer.setLength(0);
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            bluetoothHandler.postDelayed(this, CLOCK);
        }
    };


    BluetoothManager(MainActivity activity) {
        this.activity = activity;
        if(!bluetoothAdapter.isEnabled())
            activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
    }


    private boolean isComplete(String str) {
        return count(str,SEP_DBG)==2||(count(str,SEP_ID)==2&&(count(str,SEP_BOOL)==2||count(str,SEP_INT)==2)||count(str,SEP_STR)==2);
    }

    private int count(String str, String s) {
        int counter = 0;
        for(int i=0; i<str.length(); i++)
            if(str.charAt(i)==s.toCharArray()[0])
                counter++;
        return counter;
    }




    public void initBluetooth() {
        try {
            BluetoothServerSocket server = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BTServer", UUID.fromString(PI_UUID));
            BluetoothSocket socket = server.accept(20000);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            isConnected = true;
            bluetoothHandler.post(checkIncomingData);
            activity.connected();
        } catch(IOException e) {
            activity.connectionFailed();
            Log.w(TAG,"Couldn't create BTServer :",e);
        }
    }


    @SuppressLint("DefaultLocale")
    public void send(String cmd) {
        if(!isConnected)
            return;
        try {
            out.write((SEP_DBG+cmd+SEP_DBG).getBytes());
            Log.d(TAG,"Sending : "+cmd);
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken Pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }


    public void send(String id, int cmd) {
        if(!isConnected)
            return;
        try {
            out.write((SEP_ID+id+SEP_ID).getBytes());
            out.write((SEP_INT+Integer.toString(cmd)+SEP_INT).getBytes());
            Log.d(TAG,"Sending : "+cmd+" from "+id);
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken Pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }

    public void send(String id, boolean cmd) {
        if(!isConnected)
            return;
        try {
            out.write((SEP_ID+id+SEP_ID).getBytes());
            out.write((SEP_BOOL+Boolean.toString(cmd).charAt(0)+SEP_BOOL).getBytes());
            Log.d(TAG,"Sending : "+cmd+" from "+id);
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken Pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }

    public void send(String id, String cmd) {
        if(!isConnected)
            return;
        try {
            out.write((SEP_ID+id+SEP_ID).getBytes());
            out.write((SEP_STR+cmd+SEP_STR).getBytes());
            Log.d(TAG,"Sending : "+cmd+" from "+id);
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken Pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }

    public boolean isConnected() {
        return isConnected;
    }


}
