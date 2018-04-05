package com.gueg.synchropi;

import java.util.ArrayList;

/**
 * Bridge between ControlViews and the bluetooth device.
 * @see BluetoothHandler
 * @see ControlView
 * @see ControlsFragment
 */
public interface OnEvent {
    void disconnect();
    void send(String value);
    void send(String id, int value);
    void send(String id, boolean value);
    void send(String id, String value);
    void send(String id, String value, ArrayList<Integer> macs);
}