package com.gueg.synchropi;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
public class BluetoothHandler {

    private static final String PING_CHAR = "+";
    private static final String SEP_PACKETS = "=";
    private static final String SEP_ID = "&";
    private static final String SEP_DBG = "-";
    private static final String SEP_INT = "#";
    private static final String SEP_BOOL = "$";
    private static final String SEP_STR = "*";
    private static final String SEP_MAC = "^";

    
    private static final String TAG = "BTManager";
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private MainActivity activity;
    private InputStream in;
    private OutputStream out;
    private StringBuilder buffer = new StringBuilder();
    private boolean isConnected = false;
    private static final long CLOCK = 200L;
    private static final long TIME_BETWEEN_RETRIES = 400L;
    private static final String PI_UUID = "00001111-0000-1000-8000-00805f9b34fb";


    private Handler bluetoothHandler = new Handler();

    Runnable checkIncomingData = new Runnable() {
        @Override
        public void run() {
            if(!isConnected)
                return;
            try {
                out.write(PING_CHAR.getBytes());
            } catch(IOException e) {
                if(e.toString().contains("Broken pipe")) {
                    activity.disconnected();
                    isConnected = false;
                }
            }
            try {
                int byteCount = in.available();
                if (byteCount > 0) {
                    boolean valid = false;
                    byte[] raw = new byte[byteCount];
                    in.read(raw);
                    final String string = new String(raw, "ASCII");
                    String read = "";
                    if(!isComplete(string)) {
                        buffer.append(string);
                        if (isComplete(buffer.toString())) {
                            read = buffer.toString();
                            valid = true;
                            buffer.setLength(0);
                        }
                    } else {
                        read = string;
                        valid = true;
                    }
                    if (valid) {
                        String cmds[] = read.split(SEP_PACKETS);
                        for(int i=1; i<cmds.length; i++) {
                            String cmd = cmds[i];
                            if(!isCmdComplete(cmd)) {
                                buffer.append(cmd);
                                break;
                            }
                            Log.d(TAG,"Received : "+cmd);
                            if(cmd.contains("M"))
                                activity.getBTIp(Integer.decode(cmd.replace(SEP_MAC, "").replace("M", "")));
                            else if (cmd.contains("C"))
                                activity.macConnected(cmd.replace(SEP_MAC, "").replace("C", "").substring(0,17),Integer.decode(cmd.replace(SEP_MAC, "").replace("C", "").substring(17)));
                            else if (cmd.contains("D"))
                                activity.macDisconnected(cmd.replace(SEP_MAC, "").replace("C", ""));
                        }
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            bluetoothHandler.postDelayed(this, CLOCK);
        }
    };

    private boolean isComplete(String str) {
        return count(str,SEP_PACKETS)>0&&(count(str,SEP_DBG)>=2||(count(str,SEP_ID)>=2&&(count(str,SEP_BOOL)>=2||count(str,SEP_INT)>=2)||count(str,SEP_STR)>=2)||count(str,SEP_MAC)>=2);
    }

    private boolean isCmdComplete(String str) {
        return count(str,SEP_DBG)==2||(count(str,SEP_ID)==2&&(count(str,SEP_BOOL)==2||count(str,SEP_INT)==2||count(str,SEP_STR)==2))||count(str,SEP_MAC)>=2;
    }


    BluetoothHandler(MainActivity activity) {
        this.activity = activity;
        if(!bluetoothAdapter.isEnabled())
            activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
    }


    private int count(String str, String s) {
        int counter = 0;
        for(int i=0; i<str.length(); i++)
            if(str.charAt(i)==s.toCharArray()[0])
                counter++;
        return counter;
    }


    public void initBluetooth() {
        int currentIndex = 0;
        activity.currentIndex(currentIndex);
        while(!isConnected&&currentIndex<activity.getMacs().size()) {
            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(activity.getMacs().get(currentIndex).ad);
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(PI_UUID));
                socket.connect();
                in = socket.getInputStream();
                out = socket.getOutputStream();
                isConnected = true;
                activity.connected(currentIndex);
                bluetoothHandler.post(checkIncomingData);
            } catch (IOException e) {
                Log.d(TAG,"Couldn't connect to "+activity.getMacs().get(currentIndex).ad);
                currentIndex++;
                if(currentIndex<activity.getMacs().size())
                    activity.currentIndex(currentIndex);
            }
        }
        if(!isConnected) {
            activity.currentIndex(-1);
            bluetoothHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initBluetooth();
                }
            }, TIME_BETWEEN_RETRIES);
        }
    }

    public void initBluetooth(String mac) {
        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(PI_UUID));
            socket.connect();
            in = socket.getInputStream();
            out = socket.getOutputStream();
            isConnected = true;
            bluetoothHandler.post(checkIncomingData);
            activity.connected(mac);
        } catch (IOException e) {
            Log.d(TAG,"Couldn't connect to "+mac);
        }
        if(!isConnected) {
            activity.currentIndex(-1);
            bluetoothHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initBluetooth();
                }
            }, TIME_BETWEEN_RETRIES);
        }
    }

    public void disconnect() {
        initBluetooth();
    }


    @SuppressLint("DefaultLocale")
    public void send(String cmd) {
        if(!isConnected)
            return;
        try {
            out.write((SEP_PACKETS+SEP_DBG+cmd+SEP_DBG).getBytes());
            Log.d(TAG,"Sending : "+cmd);
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }


    private String lastIntId = "";
    private int lastIntCmd = -1;
    public void send(String id, int cmd) {
        if(!isConnected)
            return;
        if(id.equals(lastIntId)&&cmd==lastIntCmd)
            return;
        lastIntId = id;
        lastIntCmd = cmd;
        try {
            out.write((SEP_PACKETS+SEP_ID+id+SEP_ID).getBytes());
            out.write((SEP_INT+Integer.toString(cmd)+SEP_INT).getBytes());
            Log.d(TAG,"Sending : "+cmd+" from "+id);
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }

    private String lastBoolId = "";
    private boolean lastBoolCmd = false;
    public void send(String id, boolean cmd) {
        if(!isConnected)
            return;
        if(id.equals(lastBoolId)&&cmd==lastBoolCmd)
            return;
        lastBoolId = id;
        lastBoolCmd = cmd;
        try {
            out.write((SEP_PACKETS+SEP_ID+id+SEP_ID).getBytes());
            out.write((SEP_BOOL+Boolean.toString(cmd).charAt(0)+SEP_BOOL).getBytes());
            Log.d(TAG,"Sending : "+cmd+" from "+id);
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }

    private String lastStrId = "";
    private String lastStrCmd = "";
    public void send(String id, String cmd) {
        if(!isConnected)
            return;
        if(id.equals(lastStrId)&&cmd.equals(lastStrCmd))
            return;
        lastStrId = id;
        lastStrCmd = cmd;
        try {
            out.write((SEP_PACKETS+SEP_ID+id+SEP_ID).getBytes());
            out.write((SEP_STR+cmd+SEP_STR).getBytes());
            Log.d(TAG,"Sending : "+cmd+" from "+id);
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }

    public boolean isConnected() {
        return isConnected;
    }


}
