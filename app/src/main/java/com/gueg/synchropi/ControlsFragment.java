package com.gueg.synchropi;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class ControlsFragment extends Fragment implements MotorView.OnValueChanged, SwitchView.OnSwitch {

    MainActivity activity;

    View root;

    ButtonView btn_shutdown;
    ButtonView btn_disconnect;
    MotorView motorController;
    SwitchView motorSwitch;
    MotorView motorController2;
    SwitchView motorSwitch2;
    SwitchView ledSwitch;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_controls,container,false);
        /* ControlViews */
        motorController = root.findViewById(R.id.controller_motor);
        motorController.setTitle("Moteur G").setCmdId("m_pwm");
        motorController.setOnValueChangedListener(this);
        motorSwitch = root.findViewById(R.id.switch_motor);
        motorSwitch.setTitle("Moteur G").setCmdId("m_sw");
        motorSwitch.setOnSwitchListener(this);
        motorController2 = root.findViewById(R.id.controller_motor2);
        motorController2.setTitle("Moteur D").setCmdId("m_pwm2");
        motorController2.setOnValueChangedListener(this);
        motorSwitch2 = root.findViewById(R.id.switch_motor2);
        motorSwitch2.setTitle("Moteur D").setCmdId("m_sw2");
        motorSwitch2.setOnSwitchListener(this);
        ledSwitch = root.findViewById(R.id.switch_led);
        ledSwitch.setTitle("Led").setCmdId("l_sw");
        ledSwitch.setOnSwitchListener(this);
        ledSwitch.setCheckedColor(Color.CYAN).setUncheckedColor(Color.GRAY);
        /* btn_shutdown */
        btn_shutdown = root.findViewById(R.id.btn_shutdown);
        btn_shutdown.setTitle("OFF");
        btn_shutdown.setActionlistener(new ButtonView.OnAction() {
            @Override
            public void clicked() {
                activity.sendCommand("Shutdown");
            }
        });
        /* btn_disconnect */
        btn_disconnect = root.findViewById(R.id.btn_disconnect);
        btn_disconnect.setTitle("DECO");
        btn_disconnect.setActionlistener(new ButtonView.OnAction() {
            @Override
            public void clicked() {
                activity.sendCommand("Disconnecting");
            }
        });
        
        return root;
    }



    public void onValueChanged(MotorView v, int value) {
        activity.sendCommand(v.getCmdId(), value);
    }

    public void onSwitch(SwitchView v, boolean isSwitched) {
        activity.sendCommand(v.getCmdId(), isSwitched);
    }


    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

}
