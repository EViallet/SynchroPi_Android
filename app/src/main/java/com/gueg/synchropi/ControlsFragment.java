package com.gueg.synchropi;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.gueg.synchropi.Views.ButtonView;
import com.gueg.synchropi.Views.ControlView;
import com.gueg.synchropi.Views.MotorView;
import com.gueg.synchropi.Views.SwitchView;

import java.util.ArrayList;


public class ControlsFragment extends Fragment implements MotorView.OnValueChanged, SwitchView.OnSwitch, ButtonView.OnAction {

    MainActivity activity;

    View root;

    OnControlViewEvent onControlViewEventListener;

    ArrayList<ControlView> views = new ArrayList<>();
    ButtonView btn_shutdown;
    ButtonView btn_disconnect;
    ButtonView ledsCircle;
    ButtonView ledsFireworks;
    EditText ledsText;
    ButtonView ledsTextSend;
    MotorView motorController2;
    SwitchView shiftSwitch; /**< Allows to enable or disable command shifting when a device becomes offline. */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_controls,container,false);
        /* ControlViews */
        ledsCircle = root.findViewById(R.id.button_circles);
        ledsCircle.setTitle("Cercles").setCmdId("leds");
        ledsCircle.setActionlistener(this).setCommand("circle.py");
        //ledsCircle.attachMac(0).attachMac(1);
        ledsFireworks = root.findViewById(R.id.button_fireworks);
        ledsFireworks.setTitle("Feux d'artifice").setCmdId("leds");
        ledsFireworks.setActionlistener(this).setCommand("feux_artifice.py");
        //ledsFireworks.attachMac(0).attachMac(1);
        motorController2 = root.findViewById(R.id.controller_motor2);
        motorController2.setTitle("Couleur").setCmdId("leds_color");
        motorController2.setOnValueChangedListener(this);
        //motorController2.attachMac(0);
        ledsText = root.findViewById(R.id.edittext_text);
        ledsTextSend = root.findViewById(R.id.button_text);
        ledsTextSend.setTitle("Envoyer").setCmdId("leds");
        ledsTextSend.setActionlistener(new ButtonView.OnAction() {
            @Override
            public void clicked(ButtonView v, String cmd, ArrayList<Integer> macs) {
                if(!ledsText.getText().toString().isEmpty())
                    onControlViewEventListener.send(v.getCmdId(),"message.py "+ledsText.getText().toString()+" 139 89 25",macs);
            }
        });
        shiftSwitch = root.findViewById(R.id.switch_shift_command);
        shiftSwitch.setTitle("Décalage").setCmdId("cmd_shft");
        shiftSwitch.setOnSwitchListener(this);
        shiftSwitch.setCheckedColor(Color.CYAN).setUncheckedColor(Color.GRAY);
        /* btn_shutdown */
        btn_shutdown = root.findViewById(R.id.btn_shutdown);
        btn_shutdown.setTitle("OFF");
        btn_shutdown.setActionlistener(this).setCommand("sudo shutdown");
        /* btn_disconnect */
        btn_disconnect = root.findViewById(R.id.btn_disconnect);
        btn_disconnect.setTitle("DECO");
        btn_disconnect.setActionlistener(this);

        views.add(ledsCircle);
        views.add(motorController2);
        views.add(shiftSwitch);
        
        return root;
    }

    public void notifyMacDeleted(int pos) {
        for(ControlView v : views)
            v.notifyMacDeleted(pos);
    }

    public void onValueChanged(MotorView v, int value, ArrayList<Integer> macs) {
        onControlViewEventListener.send(v.getCmdId(), value, macs);
    }

    public void onSwitch(SwitchView v, boolean isSwitched, ArrayList<Integer> macs) {
        onControlViewEventListener.send(v.getCmdId(), isSwitched, macs);
    }

    public void clicked(ButtonView v, String cmd, ArrayList<Integer> macs) {
        onControlViewEventListener.send(v.getCmdId(), cmd, macs);
    }

    public void setOnControlViewEventListener(OnControlViewEvent onControlViewEventListener) {
        this.onControlViewEventListener = onControlViewEventListener;
    }


    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

}
