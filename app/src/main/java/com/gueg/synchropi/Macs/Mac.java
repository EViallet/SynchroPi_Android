package com.gueg.synchropi.Macs;

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
    public int delay; /**< Delay before executing tasks. */

    /**
     * Constructor.
     * @param a Mac address.
     */
    public Mac(String a) {
        this(a,0);
    }

    /**
     * Constructor.
     * @param a Mac address.
     * @param c Is currently connected?
     */
    public Mac(String a, boolean c) {
        this(a, UNKNOWN_IP, false, c, 0);
    }

    /**
     * Constructor.
     * @param a Mac address.
     * @param d Assigned delay.
     */
    public Mac(String a, int d) {
        this(a, UNKNOWN_IP, false, false, d);
    }

    /**
     * Full constructor.
     * @param a Mac address.
     * @param i Local ip.
     * @param bt Is currently connected with BluetoothHandler.
     * @param c Is currently alive in the ad-hoc network.
     * @param d Assigned delay.
     */
    public Mac(String a, int i, boolean bt, boolean c, int d) {
        ad = a;
        ip = i;
        BTco = bt;
        co = c;
        ip = i;
        delay = d;
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

    /**
     * Override to return a human readable String representing the Mac.
     * @return Readable Mac representation.
     */
    @Override
    public String toString() {
        //noinspection StringBufferReplaceableByString
        return new StringBuilder().append(ad).append(" - ").append(ip).append(" - ").append(delay).toString();
    }
}
