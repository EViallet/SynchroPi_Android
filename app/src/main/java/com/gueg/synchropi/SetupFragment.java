package com.gueg.synchropi;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gueg.synchropi.views.LoadingView;

public class SetupFragment extends Fragment {

    MainActivity activity;

    View root;

    LoadingView loading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_setup,container,false);

        loading = root.findViewById(R.id.setup_connect);

        return root;
    }


    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }
}
