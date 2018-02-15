package com.gueg.rasp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MotorView.OnValueChanged, SwitchView.OnSwitch {

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
    Button btn_connect;
    ButtonView btn_shutdown;
    ButtonView btn_disconnect;
    MotorView motorController;
    SwitchView motorSwitch;
    MotorView motorController2;
    SwitchView motorSwitch2;
    SwitchView ledSwitch;

    View.OnClickListener connectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, R.string.text_awaiting_connexion, Toast.LENGTH_SHORT).show();
            manager.initBluetooth();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_connect = findViewById(R.id.btn);
        btn_connect.setOnClickListener(connectListener);
        motorController = findViewById(R.id.controller_motor);
        motorController.setTitle("Moteur G").setCmdId("m_pwm");
        motorController.setOnValueChangedListener(this);
        motorSwitch = findViewById(R.id.switch_motor);
        motorSwitch.setTitle("Moteur G").setCmdId("m_sw");
        motorSwitch.setOnSwitchListener(this);
        motorController2 = findViewById(R.id.controller_motor2);
        motorController2.setTitle("Moteur D").setCmdId("m_pwm2");
        motorController2.setOnValueChangedListener(this);
        motorSwitch2 = findViewById(R.id.switch_motor2);
        motorSwitch2.setTitle("Moteur D").setCmdId("m_sw2");
        motorSwitch2.setOnSwitchListener(this);
        ledSwitch = findViewById(R.id.switch_led);
        ledSwitch.setTitle("Led").setCmdId("l_sw");
        ledSwitch.setOnSwitchListener(this);
        ledSwitch.setCheckedColor(Color.CYAN).setUncheckedColor(Color.GRAY);
        btn_shutdown = findViewById(R.id.btn_shutdown);
        btn_shutdown.setTitle("OFF");
        btn_shutdown.setActionlistener(new ButtonView.OnAction() {
            @Override
            public void clicked() {
                if(!manager.isConnected())
                    return;
                manager.send("Shutdown");
                disconnected();
            }
        });
        btn_shutdown = findViewById(R.id.btn_disconnect);
        btn_shutdown.setTitle("DECO");
        btn_shutdown.setActionlistener(new ButtonView.OnAction() {
            @Override
            public void clicked() {
                if(!manager.isConnected())
                    return;
                manager.send("Disconnecting");
                disconnected();
            }
        });
        manager = new BluetoothManager(this);

        registerReceiver(bluetoothStateChanged,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        checkPermission();

    }

    public void connected() {
        Toast.makeText(this, R.string.text_connected, Toast.LENGTH_SHORT).show();
        manager.send(".");
        motorController.setVisibility(View.VISIBLE);
        motorSwitch.setVisibility(View.VISIBLE);
        motorController2.setVisibility(View.VISIBLE);
        motorSwitch2.setVisibility(View.VISIBLE);
        ledSwitch.setVisibility(View.VISIBLE);
        btn_shutdown.setVisibility(View.VISIBLE);
        btn_connect.setVisibility(View.GONE);
    }

    public void connectionFailed() {
        Toast.makeText(this, R.string.text_failed, Toast.LENGTH_SHORT).show();
    }

    public void disconnected() {
        Toast.makeText(this, R.string.text_disconnected, Toast.LENGTH_SHORT).show();
        motorController.setVisibility(View.GONE);
        motorSwitch.setVisibility(View.GONE);
        motorController2.setVisibility(View.GONE);
        motorSwitch2.setVisibility(View.GONE);
        ledSwitch.setVisibility(View.GONE);
        btn_shutdown.setVisibility(View.GONE);
        btn_connect.setVisibility(View.VISIBLE);
    }


    public void onValueChanged(MotorView v, int value) {
        if(!manager.isConnected())
            return;
        manager.send(v.getCmdId(), value);
    }

    public void onSwitch(SwitchView v, boolean isSwitched) {
        if(!manager.isConnected())
            return;
        manager.send(v.getCmdId(), isSwitched);
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
