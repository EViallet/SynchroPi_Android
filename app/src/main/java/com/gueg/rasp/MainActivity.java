package com.gueg.rasp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements MotorView.OnValueChanged, ButtonView.OnSwitch {

    private final static boolean DEBUG_MODE = true;

    private static final int BLUETOOTH_PERMISSION = 0;

    BroadcastReceiver bluetoothStateChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)) {
                case BluetoothAdapter.STATE_CONNECTED:
                    if(manager!=null&&!manager.isConnected())
                        manager.initBluetooth();
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    break;
                default:
                    break;
            }
        }
    };

    BluetoothManager manager;
    TextView debug_view;
    EditText debug_text;
    Button btn_connect;
    MotorView motorController;
    ButtonView motorSwitch;

    View.OnClickListener connectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btn_connect.setText(R.string.text_awaiting_connexion);
            manager.initBluetooth();
        }
    };

    View.OnClickListener sendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            manager.send(debug_text.getText().toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_connect = findViewById(R.id.btn);
        btn_connect.setOnClickListener(connectListener);
        debug_view = findViewById(R.id.txt);
        debug_text = findViewById(R.id.send);
        motorController = findViewById(R.id.controller_motor);
        motorController.setTitle("m_pwm");
        motorController.setOnValueChangedListener(this);
        motorSwitch = findViewById(R.id.switch_motor);
        motorSwitch.setTitle("m_sw");
        motorSwitch.setOnSwitchListener(this);
        manager = new BluetoothManager(this);

        registerReceiver(bluetoothStateChanged,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        checkPermission();

    }

    public void connected() {
        manager.send("Connecting");
        motorController.setVisibility(View.VISIBLE);
        motorSwitch.setVisibility(View.VISIBLE);
        if(DEBUG_MODE) {
            debug_view.setText(R.string.text_connected);
            debug_text.setVisibility(View.VISIBLE);
            debug_view.setVisibility(View.VISIBLE);
            btn_connect.setOnClickListener(sendListener);
            btn_connect.setText(R.string.text_send);
        } else {
            btn_connect.animate().translationX(400f).setDuration(500).start();
        }
    }

    public void connectionFailed() {
        btn_connect.setText(R.string.text_connect);
    }

    public void setText(String text) {
        if(DEBUG_MODE)
            debug_view.append("\n"+text);
    }

    public void onValueChanged(MotorView v, int value) {
        if(!manager.isConnected())
            return;
        manager.send(v.getTitle(), value);
    }

    public void onSwitch(ButtonView v, boolean isSwitched) {
        if(!manager.isConnected())
            return;
        manager.send(v.getTitle(), isSwitched);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH))
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_PERMISSION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    finish();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        manager.send("Disconnecting");
        unregisterReceiver(bluetoothStateChanged);
    }

}
