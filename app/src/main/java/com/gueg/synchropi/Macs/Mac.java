package com.gueg.synchropi.macs;

/**
 * Wrapper for a device.
 * Allows to manage every Mac separately and assign them delays.
 */
@SuppressWarnings("WeakerAccess")
public class Mac {
    public static final int UNKNOWN_IP = -1; /**< Device not seen yet or lost connection. */
    public String ad; /**< Device mac address. */
    public int ip; /**< Device local ip address. */
    public boolean co; /**< Is currently connected in the ad-hoc network. */
    public boolean BTco = false; /**< Has an alive socket with Bluetoothhandler. */
    public boolean isSelected = true;

    /**
     * Constructor.
     * @param a Mac address.
     */
    public Mac(String a) {
        this(a, false);
    }

    /**
     * Constructor.
     * @param a Mac address.
     * @param c Is currently connected?
     */
    public Mac(String a, boolean c) {
        this(a, UNKNOWN_IP, false, c);
    }

    /**
     * Full constructor.
     * @param a Mac address.
     * @param i Local ip.
     * @param bt Is currently connected with BluetoothHandler.
     * @param c Is currently alive in the ad-hoc network.
     */
    public Mac(String a, int i, boolean bt, boolean c) {
        ad = a;
        ip = i;
        BTco = bt;
        co = c;
        ip = i;
    }

    /**
     * Setter for BTco.
     * @see BTco
     */
    public void BTconnected() {
        BTco = true;
    }

    /**
     * Setter for co.
     * @see co
     * @see disconnected()
     */
    public void connected() {
        co = true;
    }

    /**
     * Setter for co.
     * @see co
     * @see connected()
     */
    public void disconnected() {
        co = false;
    }

    public void toggle() {
        isSelected = !isSelected;
    }

    /**
     * Override to return a human readable String representing the Mac.
     * @return Readable Mac representation.
     */
    @Override
    public String toString() {
        //noinspection StringBufferReplaceableByString
        return new StringBuilder().append(ad).append(" - ").append(ip).toString();
    }
}
