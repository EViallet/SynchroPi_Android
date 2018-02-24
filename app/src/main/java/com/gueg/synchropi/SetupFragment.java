package com.gueg.synchropi;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class SetupFragment extends Fragment {

    MainActivity activity;

    View root;

    TextView txt_connecting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_setup,container,false);

        txt_connecting = root.findViewById(R.id.setup_connect);

        return root;
    }

    @SuppressLint("SetTextI18n")
    public void setIndex(final int current, final int max) {
        txt_connecting.post(new Runnable() {
            @Override
            public void run() {
                int c = current+1;
                txt_connecting.setText("Trying "+c+"/"+max);
            }
        });
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }
}
