package com.gueg.synchropi;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.gueg.synchropi.Macs.Mac;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Manages every bluetooth related method.
 * Tries to connect to every mac currently loaded in MainActivity, then opens a socket to read/write from it at anytime.
 */
@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
public class BluetoothHandler extends Thread implements OnControlViewEvent {

    private static final String TAG = "BTHandler"; /**<Debug tag*/
    private static final String PI_UUID = "00001111-0000-1000-8000-00805f9b34fb"; /**< Free uuid channel between the devices. */

    /** Separators to code/decode commands easily */
    public static final String PING_CHAR = "+";
    public static final String SEP_PACKETS = "=";
    public static final String SEP_ID = "&";
    public static final String SEP_DBG = "-";
    public static final String SEP_INT = "#";
    public static final String SEP_BOOL = "$";
    public static final String SEP_STR = "*";
    public static final String SEP_MAC = "^";
    public static final String SEP_DELAY = "%";
    public static final String SEP_TASKID = "~";

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); /**< Default android bluetooth adapter. */
    private BluetoothSocket socket; /**< Will try to connect to any open QBluetoothServer on channel PI_UUID @see PI_UUID @see initBluetooth() */
    private MainActivity activity; /**< Used to interact with views .*/
    private InputStream in; /**< Allows to read from socket */
    private OutputStream out; /**< Allows to write to socket */
    private StringBuilder buffer = new StringBuilder(); /**<  Buffer used to append incomplete data when reading from socket */
    private boolean isConnected = false; 
    private long taskId = 0; /**< Counter that will increment on each sent command. Allows to cancel delayed tasks. */

    /**
    * Main thread method. Loops while the app is open and Bluetooth connection is turned on.
    * @see initBluetooth()
    */
    @Override
    public void run() {
        while(!interrupted()) {
            if (!isConnected) {
                initBluetooth();
            } else {
            	// Checks if the connection is still live by sending it a packet.
                try {
                    out.write(PING_CHAR.getBytes());
                } catch (IOException e) {
                	// If we couldn't write to the socket, we are disconnected.
                    if (e.toString().contains("Broken pipe")) {
                        activity.disconnected();
                        isConnected = false;
                    }
                }
                // Check if there are any awaiting data.
                try {
                    int byteCount = in.available();
                    if (byteCount > 0) {
                        boolean valid = false;
                        byte[] raw = new byte[byteCount];
                        in.read(raw);
                        final String string = new String(raw, "ASCII");
                        String read = "";
                        if (!isComplete(string)) {
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
                            for (int i = 1; i < cmds.length; i++) {
                                final String cmd = cmds[i];
                                if (!isCmdComplete(cmd)) {
                                    buffer.append(cmd);
                                    break;
                                }
                                Log.d(TAG, "Received : " + cmd);
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (cmd.contains("M"))
                                            activity.setBTIp(Integer.decode(cmd.replace(SEP_MAC, "").replace("M", "")));
                                        else if (cmd.contains("C"))
                                            activity.macConnected(cmd.replace(SEP_MAC, "").replace("C", "").substring(0, 17), Integer.decode(cmd.replace(SEP_MAC, "").replace("C", "").substring(17)));
                                        else if (cmd.contains("D"))
                                            activity.macDisconnected(cmd.replace(SEP_MAC, "").replace("C", ""));
                                    }
                                });
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } // if(isConnected())
        } // while(!interrupted())
        // On thread interrupted = on bluetooth turned off or on app close.
        try {
            if (socket != null && socket.isConnected())
                socket.close();
        } catch(IOException e) {
            Log.d(TAG,"Couldn't close socket"+e.getCause());
        }
    }
    
   /**
   * Count the number of s occurences in str.
   * @param str String to check.
   * @param s String to count.
   * @see run()
   */
    private int count(String str, String s) {
        int counter = 0;
        for(int i=0; i<str.length(); i++)
            if(str.charAt(i)==s.toCharArray()[0])
                counter++;
        return counter;
    }

    /**
    * While getting incoming data, checks if the command contains at least one complete command.
    @see count()
    @see run()
    */
    private boolean isComplete(String str) {
        return count(str,SEP_PACKETS)>0&&(count(str,SEP_DBG)>=2||(count(str,SEP_ID)>=2&&(count(str,SEP_BOOL)>=2||count(str,SEP_INT)>=2)||count(str,SEP_STR)>=2)||count(str,SEP_MAC)>=2);
    }

    /**
    * Before executing each command, checks if it's complete.
    * @see count()
    * @see run()
    */
    private boolean isCmdComplete(String str) {
        return count(str,SEP_DBG)==2||(count(str,SEP_ID)==2&&(count(str,SEP_BOOL)==2||count(str,SEP_INT)==2||count(str,SEP_STR)==2))||count(str,SEP_MAC)>=2;
    }


    /**
    * Constructor.
    @param activity Used as a bridge to interact with views. Checks if the Bluetooth is enabled and asks the user to turn it on.
    */
    BluetoothHandler(MainActivity activity) {
        this.activity = activity;
        if(!bluetoothAdapter.isEnabled())
            activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
    }

   /**
   * Tries to open a BluetoothSocket from a device's mac.
   */
    public void initBluetooth() {
        int currentIndex = 0;
        ArrayList<Mac> macs = activity.getMacs();
        activity.currentIndex(currentIndex,macs.size());
        while(!isConnected&&currentIndex<macs.size()) {
            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macs.get(currentIndex).ad);
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(PI_UUID));
                socket.connect();
                in = socket.getInputStream();
                out = socket.getOutputStream();
                isConnected = true;
                final int cI = currentIndex;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.connected(cI);
                    }
                });
            } catch (IOException e) {
                Log.d(TAG,"Couldn't connect to "+macs.get(currentIndex).ad);
                e.printStackTrace();
                currentIndex++;
                if(currentIndex<macs.size())
                    activity.currentIndex(currentIndex,macs.size());
            }
        }
        if(!isConnected)
            activity.currentIndex(-1,macs.size());
    }

    /**
    * Tries to connect to a specific mac requested from the user.
    * @param mac User requested mac.
    */
    public void initBluetooth(final String mac) {
        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString(PI_UUID));
            socket.connect();
            in = socket.getInputStream();
            out = socket.getOutputStream();
            isConnected = true;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.connected(mac);
                }
            });
        } catch (IOException e) {
            Log.d(TAG,"Couldn't connect to "+mac+" - "+e.getCause());
        }
        if(!isConnected)
            activity.currentIndex(-1,-1);
    }

   /**
   * Setter method. Will impact on the next run() iteration.
   * @see run()
   */
    public void disconnect() {
        isConnected = false;
    }


   /**
   * Send a literal command to socket.
   * @see out
   */
    @SuppressLint("DefaultLocale")
    public void send(String cmd) {
        if(!isConnected)
            return;
        try {
            StringBuilder str = new StringBuilder();
            str.append(SEP_PACKETS).append(SEP_TASKID).append(taskId).append(SEP_TASKID).append(SEP_DBG).append(cmd).append(SEP_DBG);
            out.write(str.toString().getBytes());
            Log.d(TAG,"Sending : "+str);
            taskId++;
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }


    // TODO redo check
    private String lastIntCmd = "";
    /**
    * Send an int command to out.
    * @param id Remote command target
    * @param cmd Command
    * @param target Targeted devices
    * @see out
    */
   public void send(String id, int cmd, ArrayList<Integer> target) {
        if(!isConnected)
            return;
        try {
            StringBuilder str = new StringBuilder();
            str.append(SEP_PACKETS).append(SEP_TASKID).append(taskId).append(SEP_TASKID).append(SEP_ID).append(id).append(SEP_ID);
            str.append(getTargets(target));
            str.append(SEP_INT).append(cmd).append(SEP_INT);
            if(!str.toString().equals(lastIntCmd)) {
                out.write(str.toString().getBytes());
                lastIntCmd = str.toString();
                Log.d(TAG, "Sending : " + str);
                taskId++;
            }
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
   }

    private String lastBoolCmd = "";
    /**
    * Send a bool command to out.
    * @param id Remote command target
    * @param cmd Command
    * @param target Targeted devices
    * @see out
    */
    public void send(String id, boolean cmd, ArrayList<Integer> target) {
        if(!isConnected)
            return;
        try {
            StringBuilder str = new StringBuilder();
            str.append(SEP_PACKETS).append(SEP_TASKID).append(taskId).append(SEP_TASKID).append(SEP_ID).append(id).append(SEP_ID);
            str.append(getTargets(target));
            str.append(SEP_BOOL).append(Boolean.toString(cmd).charAt(0)).append(SEP_BOOL);
            if(!str.toString().equals(lastBoolCmd)) {
                out.write(str.toString().getBytes());
                Log.d(TAG, "Sending : " + str);
                lastBoolCmd = str.toString();
                taskId++;
            }
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }

    private String lastStrCmd = "";
    /**
    * Send a String command to out.
    * @param id Remote command target
    * @param cmd Command
    * @param target Targeted devices
    * @see out
    */
    public void send(String id, String cmd, ArrayList<Integer> target) {
        if(!isConnected)
            return;
        try {
            StringBuilder str = new StringBuilder();
            str.append(SEP_PACKETS).append(SEP_TASKID).append(taskId).append(SEP_TASKID).append(SEP_ID).append(id).append(SEP_ID);
            str.append(getTargets(target));
            str.append(SEP_STR).append(cmd).append(SEP_STR);
            if(!str.toString().equals(lastStrCmd)) {
                out.write(str.toString().getBytes());
                Log.d(TAG, "Sending : " + str);
                lastStrCmd = str.toString();
                taskId++;
            }
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                activity.disconnected();
                isConnected = false;
            }
        }
    }

    /**
    * Formats targets to a device readable list.
    * @param arrayList View's targets
    * @return Device readable list.
    */
    private String getTargets(ArrayList<Integer> arrayList) {
        StringBuilder str = new StringBuilder("");
        ArrayList<Mac> macs = activity.getMacs();
        for(int i=0; i<macs.size(); i++) {
            if(arrayList.contains(i)&&macs.get(i).ip!=Mac.UNKNOWN_IP)
                str.append(BluetoothHandler.SEP_MAC).append(macs.get(i).ip).append(BluetoothHandler.SEP_MAC).append(BluetoothHandler.SEP_DELAY).append(macs.get(i).delay).append(BluetoothHandler.SEP_DELAY);
        }
        return str.toString();
    }
}
