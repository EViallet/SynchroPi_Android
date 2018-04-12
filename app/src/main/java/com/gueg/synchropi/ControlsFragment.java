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
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;

import com.gueg.synchropi.views.MotorView;


public class ControlsFragment extends Fragment implements MotorView.OnValueChanged, View.OnClickListener, SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {

    MainActivity activity;

    View root;

    OnEvent onEventListener;

    int _R = 88, _G = 226, _B = 120;
    boolean debugMode = false;

    EditText ledsText;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_controls,container,false);
        /* ControlViews */
        root.findViewById(R.id.button_circles).setOnClickListener(this);
        root.findViewById(R.id.button_fireworks).setOnClickListener(this);
        root.findViewById(R.id.button_rainbow).setOnClickListener(this);
        root.findViewById(R.id.button_square).setOnClickListener(this);
        
        ledsText = root.findViewById(R.id.edittext_text);
        root.findViewById(R.id.button_text).setOnClickListener(this);
        root.findViewById(R.id.switch_shift_command).setOnClickListener(this);
        /* btn_disconnect */
        root.findViewById(R.id.btn_disconnect).setOnClickListener(this);
        /* btn_debug*/
        root.findViewById(R.id.btn_debug).setOnClickListener(this);
        /* RGB bars */
        ((SeekBar)root.findViewById(R.id.seek_R)).setOnSeekBarChangeListener(this);
        ((SeekBar)root.findViewById(R.id.seek_G)).setOnSeekBarChangeListener(this);
        ((SeekBar)root.findViewById(R.id.seek_B)).setOnSeekBarChangeListener(this);
        root.findViewById(R.id.seek_helper).setBackgroundColor(Color.rgb(_R,_G,_B));
        /* MotorViews */
        ((MotorView)root.findViewById(R.id.servo_angle)).setOnValueChangedListener(this);
        ((MotorView)root.findViewById(R.id.servo_angle)).setFullCircle(true);
        ((MotorView)root.findViewById(R.id.servo_speed)).setOnValueChangedListener(this);
        ((RadioGroup)root.findViewById(R.id.radio_group)).setOnCheckedChangeListener(this);


        return root;
    }

    // OnSeekBarChangeListener
    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch(seekBar.getId()) {
            case R.id.seek_R:
                _R = progress;
                break;
            case R.id.seek_G:
                _G = progress;
                break;
            case R.id.seek_B:
                _B = progress;
                break;
        }
        root.findViewById(R.id.seek_helper).setBackgroundColor(Color.rgb(_R,_G,_B));
    }
    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        String cmd = "";
        switch(checkedId) {
            case R.id.radio_stop:
                cmd = "stop";
                break;
            case R.id.radio_cw:
                cmd = "cw";
                break;
            case R.id.radio_ccw:
                cmd = "ccw";
                break;
        }
        onEventListener.send("servo", cmd);
    }

    @Override
    public void onValueChanged(MotorView v, int value) {
        String cmdId = "";
        switch(v.getId()) {
            case R.id.servo_angle:
                cmdId = "servo_a";
                break;
            case R.id.servo_speed:
                cmdId = "servo_s";
                break;
        }
        onEventListener.send(cmdId, value);
    }

    public void setOnEventListener(OnEvent onEventListener) {
        this.onEventListener = onEventListener;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.button_circles:
                onEventListener.send("leds", "circle.py");
                break;
            case R.id.button_rainbow:
                onEventListener.send("leds", "rainbow.py");
                break;
            case R.id.button_square:
                onEventListener.send("leds", "square.py");
                break;
            case R.id.button_fireworks:
                onEventListener.send("leds", "feux_artifice.py");
                break;
            case R.id.button_text:
                onEventListener.send("leds", "message.py "+ledsText.getText().toString()+" "+Integer.toString(_R)+" "+Integer.toString(_G)+" "+Integer.toString(_B));
                break;
            case R.id.btn_disconnect:
                break;
            case R.id.btn_debug:
                debugMode = !debugMode;
                onEventListener.send("dbg", debugMode);
                break;
            case R.id.switch_shift_command:
                onEventListener.send("cmd_shft", ((Switch)root.findViewById(id)).isChecked());
                break;
        }
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

}
