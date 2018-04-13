package com.gueg.synchropi;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;


public class ServoFragment extends Fragment implements View.OnClickListener {

    View root;
    MainActivity activity;

    OnEvent listener;

    Spinner servo1;
    Spinner servo2;
    Spinner servo3;
    Spinner servo4;

    boolean servosRotating1 = false;
    boolean servosRotating2 = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_servo, container, false);

        root.findViewById(R.id.servo_btn_1).setOnClickListener(this);
        root.findViewById(R.id.servo_btn_2).setOnClickListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_servo_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        servo1 = root.findViewById(R.id.servo_spinner_1);
        servo1.setAdapter(adapter);
        servo2 = root.findViewById(R.id.servo_spinner_2);
        servo2.setAdapter(adapter);
        servo3 = root.findViewById(R.id.servo_spinner_3);
        servo3.setAdapter(adapter);
        servo4 = root.findViewById(R.id.servo_spinner_4);
        servo4.setAdapter(adapter);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.servo_btn_1:
                if(!servo1.getSelectedItem().equals(servo2.getSelectedItem())&&!servo1.getSelectedItem().equals("Aucun")&&!servo2.getSelectedItem().equals("Aucun")) {
                    Log.d(":-:","Selected : " + servo1.getSelectedItem() + " - " + servo2.getSelectedItem());
                    ArrayList<Integer> mac1 = new ArrayList<>();
                    mac1.add(Integer.decode((String)servo1.getSelectedItem()));
                    ArrayList<Integer> mac2 = new ArrayList<>();
                    mac2.add(Integer.decode((String)servo2.getSelectedItem()));
                    listener.send("servo_sync","cw_M_"+servo2.getSelectedItem(),mac1);
                    listener.send("servo_sync","ccw_S_"+servo1.getSelectedItem(),mac2);
                }
                break;
            case R.id.servo_btn_2:
                if(!servo3.getSelectedItem().equals(servo2.getSelectedItem())&&!servo3.getSelectedItem().equals("Aucun")&&!servo4.getSelectedItem().equals("Aucun")) {
                    Log.d(":-:", "Selected : " + servo3.getSelectedItem() + " - " + servo4.getSelectedItem());
                    ArrayList<Integer> mac3 = new ArrayList<>();
                    mac3.add(Integer.decode((String)servo1.getSelectedItem()));
                    ArrayList<Integer> mac4 = new ArrayList<>();
                    mac4.add(Integer.decode((String)servo2.getSelectedItem()));
                    listener.send("servo_sync","cw_M_"+servo4.getSelectedItem(),mac3);
                    listener.send("servo_sync","ccw_S_"+servo3.getSelectedItem(),mac4);
                }
                break;
        }
    }

    public void setServoRotating(int pi1, int pi2, boolean rotating) {
        if(pi1==Integer.decode(servo1.getSelectedItem().toString())&&pi2==Integer.decode(servo2.getSelectedItem().toString()))
            servosRotating1 = rotating;
        else
            servosRotating2 = rotating;
        updateButtons();
    }

    private void updateButtons() {
        if(!servosRotating1) {
            ((Button) root.findViewById(R.id.servo_btn_1)).setText("Démarrer");
            servo1.setEnabled(true);
            servo2.setEnabled(true);
        } else {
            ((Button) root.findViewById(R.id.servo_btn_1)).setText("Arrêter");
            servo1.setEnabled(false);
            servo2.setEnabled(false);
        }
        if(!servosRotating2) {
            ((Button) root.findViewById(R.id.servo_btn_2)).setText("Démarrer");
            servo3.setEnabled(true);
            servo4.setEnabled(true);
        } else {
            ((Button) root.findViewById(R.id.servo_btn_2)).setText("Arrêter");
            servo3.setEnabled(false);
            servo4.setEnabled(false);
        }
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public void setOnEventListener(OnEvent listener) {
        this.listener = listener;
    }
}
