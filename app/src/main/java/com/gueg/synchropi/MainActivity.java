package com.gueg.synchropi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int BLUETOOTH_PERMISSION = 0;
    private static final String MACS_KEY = "com.gueg.synchropi.macs";
    private BluetoothHandler bluetoothHandler;
    private FragmentManager fragmentManager;
    private FrameLayout container;
    private ControlsFragment controls;
    private SetupFragment setup;
    private ArrayList<Mac> macs = new ArrayList<>();
    private RecyclerView macsRecyclerView;
    private MacsAdapter macsAdapter;

    private static final String PI_MAC_1 = "B8:27:EB:F0:82:5B";
    private static final String PI_MAC_2 = "B8:27:EB:40:00:FF";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = findViewById(R.id.fragment_container);


        setup = new SetupFragment();
        setup.setActivity(this);
        controls = new ControlsFragment();
        controls.setActivity(this);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(container.getId(), setup).commit();


        bluetoothHandler = new BluetoothHandler(this);

        checkPermission();

        /* DRAWER - MACS LIST */

        macsRecyclerView = findViewById(R.id.drawer_macs);
        macsRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        macsRecyclerView.setLayoutManager(mLayoutManager);

        macsRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(15));

        if(loadMacs()==0) {
            macs.add(new Mac(PI_MAC_1, false));
            macs.add(new Mac(PI_MAC_2, false));
        }

        macsAdapter = new MacsAdapter(this,new ArrayList<>(macs));

        macsRecyclerView.setAdapter(macsAdapter);


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.RIGHT) {
                    editMac(position);
                }
            }

        };


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(macsRecyclerView);

        macsRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, macsRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        bluetoothHandler.initBluetooth(macs.get(position).ad);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })

        );

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothHandler.initBluetooth();
            }
        },200);
    }

    /** BLUETOOTH */
    public void currentIndex(int c) {
        setup.setIndex(c,macs.size());
    }

    public void connected(int pos) {
        Toast.makeText(this, R.string.text_connected, Toast.LENGTH_SHORT).show();
        bluetoothHandler.send(".");
        fragmentManager.beginTransaction().replace(container.getId(), controls).setCustomAnimations(R.anim.enter,R.anim.exit).commit();
        macs.get(pos).BTconnected();
        macsAdapter.notifyDataSetChanged();
    }

    public void connected(String mac) {
        for(int i=0; i<macs.size(); i++)
            if(macs.get(i).ad.equals(mac)) {
                connected(i);
                return;
            }
    }

    public void disconnected() {
        Toast.makeText(this, R.string.text_disconnected, Toast.LENGTH_SHORT).show();
        bluetoothHandler = new BluetoothHandler(this);
        fragmentManager.beginTransaction().replace(container.getId(), setup).setCustomAnimations(R.anim.enter,R.anim.exit).commit();
    }

    public ArrayList<Mac> getMacs() {
        return macs;
    }

    public void macConnected(int ip, String m) {
        for(Mac mac : macs)
            if(mac.ad.equals(m)) {
                mac.ip = ip;
                mac.connected();
                macsAdapter.notifyDataSetChanged();
                return;
            }
        macs.add(new Mac(m,true));
        macsAdapter.notifyDataSetChanged();
    }

    public void getBTIp(int ip) {
        for(Mac mac : macs)
            if(mac.BTco) {
                mac.ip = ip;
                macsAdapter.notifyDataSetChanged();
                return;
            }
    }

    public void macDisconnected(String m) {
        for(Mac mac : macs)
            if(mac.ad.equals(m)) {
                mac.disconnected();
                macsAdapter.notifyDataSetChanged();
                return;
            }
        macs.add(new Mac(m,false));
        macsAdapter.notifyDataSetChanged();
    }

    /** PERMISSIONS */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH))
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_PERMISSION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    finish();
            }
        }
    }

    /** COMMANDS */
    public void sendCommand(String cmd) {
        if(!bluetoothHandler.isConnected())
            return;

        if(cmd.equals("Disconnected")) {
            bluetoothHandler.send("Disconnecting");
            disconnected();
        } else if(cmd.equals("Shutdown")) {
            bluetoothHandler.send("Shutdown");
            disconnected();
        }
    }

    public void sendCommand(String id, int cmd) {
        if(!bluetoothHandler.isConnected())
            return;
        bluetoothHandler.send(id,cmd);
    }

    public void sendCommand(String id, boolean cmd) {
        if(!bluetoothHandler.isConnected())
            return;
        bluetoothHandler.send(id,cmd);
    }

    /** MAC MANAGEMENT */
    public void editMac(final int pos) {
        MacInterface listener = new MacInterface() {
            @Override
            public void onMacAdded(Mac m) {
                deleteMac(pos,false);

                macs.add(m);
                macsAdapter.notifyDataSetChanged();
                saveMacs();
            }
            @Override
            public void onMacRemoved() {
                deleteMac(pos,true);
            }
            @Override
            public void onCancel() {
                macsAdapter.notifyDataSetChanged();
            }
        };
        MacEditDialog dialog = new MacEditDialog();
        dialog.setMac(macs.get(pos),true);
        dialog.setListener(listener);
        dialog.show(fragmentManager,"DIALOG");
    }

    private void deleteMac(final int position, boolean showConfirmation) {
        if(showConfirmation) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            dialog.dismiss();
                            macs.remove(position);
                            macsRecyclerView.removeViewAt(position);
                            macsAdapter.notifyItemRemoved(position);
                            macsAdapter.notifyItemRangeChanged(position, macs.size());
                            saveMacs();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            macsAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Supprimer l'adresse MAC?").setPositiveButton("Oui", dialogClickListener)
                    .setNegativeButton("Non", dialogClickListener).show();
        }
        else {
            macs.remove(position);
            macsRecyclerView.removeViewAt(position);
            macsAdapter.notifyItemRemoved(position);
            macsAdapter.notifyItemRangeChanged(position, macs.size());
            saveMacs();
        }
    }

    private void saveMacs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().remove(MACS_KEY).apply();
        HashSet<String> str = new HashSet<>();
        for(Mac m : macs)
            str.add(m.ad);
        prefs.edit().putStringSet(MACS_KEY,str).apply();
    }

    private int loadMacs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> str = prefs.getStringSet(MACS_KEY, new HashSet<String>());
        macs = new ArrayList<>();
        for(String s : str)
            macs.add(new Mac(s,false));
        return macs.size();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothHandler.send("Disconnecting");
    }

}
