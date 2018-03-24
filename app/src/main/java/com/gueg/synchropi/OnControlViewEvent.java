package com.gueg.synchropi;

import java.util.ArrayList;

/**
 * Bridge between ControlViews and the bluetooth device.
 * @see BluetoothHandler
 * @see ControlView
 * @see ControlsFragment
 */
public interface OnControlViewEvent {
    void disconnect();
    void send(String value);
    void send(String id, int value, ArrayList<Integer> macs);
    void send(String id, boolean value, ArrayList<Integer> macs);
    void send(String id, String value, ArrayList<Integer> macs);
}
