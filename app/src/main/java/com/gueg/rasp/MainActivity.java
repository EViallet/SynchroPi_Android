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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

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
    TextView textView;
    EditText editText;
    Button interact;

    View.OnClickListener connectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(manager!=null)
                manager.initBluetooth();
        }
    };

    View.OnClickListener sendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            manager.send(editText.getText().toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        interact = findViewById(R.id.btn);
        interact.setOnClickListener(connectListener);
        textView = findViewById(R.id.txt);
        editText = findViewById(R.id.send);

        registerReceiver(bluetoothStateChanged,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        checkPermission();

    }

    private void initBluetoothManager() {
        manager = new BluetoothManager(this);
    }

    public void connected(String uuids[]) {
        interact.setOnClickListener(sendListener);
        interact.setText(R.string.btn_send);
        textView.setText("in : "+uuids[0]+"\nout : "+uuids[1]);
    }

    public void setText(String text) {
        textView.append("\n"+text);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH))
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION);
        } else
            initBluetoothManager();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initBluetoothManager();
                } else {
                    finish();
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothStateChanged);
    }

}
