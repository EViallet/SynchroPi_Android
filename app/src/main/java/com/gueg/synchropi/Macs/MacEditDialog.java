package com.gueg.synchropi.macs;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gueg.synchropi.R;


public class MacEditDialog extends BottomSheetDialogFragment {

    private MacInterface _listener;
    View rootView;

    EditText mac;

    Mac m;
    boolean modif;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_mac_edit, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        mac = rootView.findViewById(R.id.dialog_mac_edit_mac);
        if(m!=null)
            mac.setText(m.ad);

        if(modif) {
            ImageView remove = rootView.findViewById(R.id.dialog_mac_edit_remove);
            remove.setVisibility(View.VISIBLE);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _listener.onMacRemoved();
                    dismiss();
                }
            });
        }

        rootView.findViewById(R.id.dialog_mac_edit_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _listener.onCancel();
                dismiss();
            }
        });
        rootView.findViewById(R.id.dialog_mac_edit_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mac.getText().toString().isEmpty()) {
                    if(m==null)
                        _listener.onMacAdded(new Mac(mac.getText().toString()));
                    else
                        _listener.onMacAdded(new Mac(mac.getText().toString(),m.ip,m.BTco,m.co));
                    dismiss();
                } else
                    Toast.makeText(getActivity(), "Entrer un mac.", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    public void setListener(MacInterface listener) {
        _listener = listener;
    }

    public void setMac(Mac m) {
        this.m = m;
        modif = true;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        _listener.onCancel();
    }

}
