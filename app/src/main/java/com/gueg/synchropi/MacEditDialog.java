package com.gueg.synchropi;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MacEditDialog extends DialogFragment {

    private MacInterface _listener;
    View rootView;

    EditText mac;

    Mac m;
    boolean modif;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Light_Dialog_Alert);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_mac_edit, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        mac = rootView.findViewById(R.id.dialog_mac_edit_mac);
        mac.setText(m.ad);

        if(modif) {
            Button remove = rootView.findViewById(R.id.dialog_mac_edit_remove);
            remove.setVisibility(View.VISIBLE);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _listener.onMacRemoved();
                    dismiss();
                }
            });
        }

        rootView.findViewById(R.id.dialog_mac_edit_annuler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        rootView.findViewById(R.id.dialog_mac_edit_valider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mac.getText().toString().isEmpty()) {
                    _listener.onMacAdded(new Mac(mac.getText().toString(),m.co));
                    dismiss();
                } else
                    Toast.makeText(getActivity(), "Entrer un titre et une url", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    public void setListener(MacInterface listener) {
        _listener = listener;
    }

    public void setMac(Mac m, boolean modif) {
        this.m = m;
        this.modif = modif;
    }

}
