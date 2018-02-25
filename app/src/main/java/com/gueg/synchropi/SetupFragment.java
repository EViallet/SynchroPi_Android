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

    LoadingView loading;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_setup,container,false);

        loading = root.findViewById(R.id.setup_connect);

        return root;
    }

    public void setLoadingMode(boolean infinite) {
        loading.setLoadingMode(infinite);
    }

    @SuppressLint("SetTextI18n")
    public void setIndex(final int current, final int max) {
        loading.postDelayed(new Runnable() {
            @Override
            public void run() {
                loading.setCurrent(current+1,max);
            }
        },500);
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }
}
