package com.gueg.rasp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int BLUETOOTH_PERMISSION = 0;

    BluetoothManager manager;
    TextView textView;
    EditText editText;
    Button interact;

    View.OnClickListener connectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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

        checkPermission();

        interact = findViewById(R.id.btn);
        interact.setOnClickListener(connectListener);
        textView = findViewById(R.id.txt);
        editText = findViewById(R.id.send);

        manager = new BluetoothManager(this);

    }

    public void connected() {
        interact.setOnClickListener(sendListener);
        interact.setText(R.string.btn_send);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH))
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION);
        } else
            Log.d(":-:","Permission OK");
    }


}
