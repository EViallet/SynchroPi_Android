package com.gueg.synchropi;

public class Mac {
    public String ad;
    public int ip;
    public boolean co;
    public boolean BTco = false;

    public Mac(String a, boolean c) {
        ad = a;
        co = c;
        ip = -1;
    }

    public void BTconnected() {
        BTco = true;
    }

    public void connected() {
        co = true;
    }

    public void disconnected() {
        co = false;
    }
}
