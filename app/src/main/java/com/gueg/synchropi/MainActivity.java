package com.gueg.synchropi;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gueg.synchropi.Macs.Mac;
import com.gueg.synchropi.Macs.MacEditDialog;
import com.gueg.synchropi.Macs.MacInterface;
import com.gueg.synchropi.Macs.MacsAdapter;
import com.gueg.synchropi.Macs.RecyclerItemClickListener;
import com.gueg.synchropi.Macs.VerticalSpaceItemDecoration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages every class and the UI.
 */
public class MainActivity extends AppCompatActivity {

    private static final String MACS_KEY = "com.gueg.synchropi.macs"; /**< Key recquired to load data from SharedPreferences. @see loadMacs() */
    private BluetoothHandler bluetoothHandler; /**< Bluetooth handling thread. */
    private FragmentManager fragmentManager; /**< Allow to make Fragment transactions to update the UI smoothly. */
    private FrameLayout container; /**< View to contains Fragments. */
    private ControlsFragment controls; /**< Fragment containing and disposing ControlViews. @see ControlView */
    private SetupFragment setup; /**< Fragment containing a single LoadingView. @see LoadingView */
    private ArrayList<Mac> macs = new ArrayList<>(); /**< Contains every stored and currently connected device's mac. */
    private RecyclerView macsRecyclerView; /**< Displays macs as a list in the drawer. @see macs */
    private MacsAdapter macsAdapter; /**< macsRecyclerView's adapter, needed to draw its rows. @see macsRecyclerView */

    private static final String PI_MAC_1 = "B8:27:EB:F0:82:5B"; /**< Default mac, in case there are no saved instance in SharedPreferences. @see loadMacs() */
    private static final String PI_MAC_2 = "B8:27:EB:40:00:FF"; /**< Another default mac. */


    /**
     * Receiver to trigger on bluetooth toggle.
     * Bluetooth turned on : start the @see BluetoothHandler
     * Bluetooth turned off : interrupt the @see BluetoothHandler
     */
    BroadcastReceiver bluetoothStateChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)) {
                case BluetoothAdapter.STATE_CONNECTED:
                    if(bluetoothHandler!=null&&bluetoothHandler.isInterrupted())
                        bluetoothHandler.start();
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                        bluetoothHandler.disconnect();
                        bluetoothHandler.interrupt();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Parsing layout views
        container = findViewById(R.id.fragment_container);

        // BluetoothHandler instantiation
        bluetoothHandler = new BluetoothHandler(this);

        // Initializing fragments and fragment manager
        setup = new SetupFragment();
        setup.setActivity(this);
        controls = new ControlsFragment();
        controls.setActivity(this);
        controls.setOnControlViewEventListener(bluetoothHandler);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(container.getId(), setup).commit();

        // Drawer - Bottom "Add" button
        findViewById(R.id.drawer_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Will show an empty MacEditDialog

                MacEditDialog dialog = new MacEditDialog();
                dialog.setListener(new MacInterface() {
                    @Override
                    public void onMacAdded(Mac m) {
                        macs.add(m);
                        saveMacs();
                        macsAdapter.notifyItemInserted(macs.size());
                        macsAdapter.notifyItemRangeChanged(0,macs.size());
                    }
                    @Override public void onCancel() {}
                    @Override public void onMacRemoved() {}
                });
                dialog.show(fragmentManager,"DIALOG");
            }
        });

        // Drawer - RecyclerView
        macsRecyclerView = findViewById(R.id.drawer_macs);
        macsRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        macsRecyclerView.setLayoutManager(mLayoutManager);

        macsRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(15));

        // Loading macs from SharedPreferences
        if(loadMacs()==0) {
            macs.add(new Mac(PI_MAC_1));
            macs.add(new Mac(PI_MAC_2));
        }

        // RecyclerView adapter
        macsAdapter = new MacsAdapter(this,macs);

        macsRecyclerView.setAdapter(macsAdapter);


        // RecyclerView callback to handle item swipe
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


        // RecyclerView touchHelper to handler item click/long click
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

        // BluetoothHandler launch
        registerReceiver(bluetoothStateChanged,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        if(BluetoothAdapter.getDefaultAdapter().isEnabled())
            bluetoothHandler.start();
    }

    /**
     * Bridge between BluetoothHandler loading and LoadingView animation.
     * @param c Current index
     * @param m Max index
     * @see BluetoothHandler
     * @see LoadingView
     */
    public void currentIndex(int c, int m) {
        setup.setIndex(c,m);
    }

    /**
     * BluetoothHandler successfully connected to a device.
     * @param mac Currently connected mac address
     * @see BluetoothHandler
     */
    public void connected(String mac) {
        for(int i=0; i<macs.size(); i++)
            if(macs.get(i).ad.equals(mac)) {
                connected(i);
                return;
            }
    }

    /**
     * BluetoothHandler successfully connected to a device.
     * @param pos Index of the Mac in the Macs list.
     * @see BluetoothHandler
     */
    public void connected(int pos) {
        Toast.makeText(this, R.string.text_connected, Toast.LENGTH_SHORT).show();
        fragmentManager.beginTransaction().replace(container.getId(), controls).setCustomAnimations(R.anim.enter,R.anim.exit).commitAllowingStateLoss();
        for(Mac m : macs)
            m.BTco = false;
        macs.get(pos).BTconnected();
        macsAdapter.notifyDataSetChanged();
    }

    /**
     * BluetoothHandler disconnected from a device. Starts it again.
     * @see BluetoothHandler
     */
    public void disconnected() {
        Toast.makeText(this, R.string.text_disconnected, Toast.LENGTH_SHORT).show();
        fragmentManager.beginTransaction().replace(container.getId(), setup).setCustomAnimations(R.anim.enter,R.anim.exit).commitAllowingStateLoss();
    }

    /**
     * Getter for macs.
     * @return macs
     */
    public ArrayList<Mac> getMacs() {
        return macs;
    }

    /**
     * Another lan device was discovered by the currently connected device.
     * @param m Its bluetooth mac address.
     * @param ip Its lan ip.
     */
    public void macConnected(String m,int ip) {
        Log.d("BTManager","Mac connected with mac "+m+" and ip "+ip);
        for(Mac mac : macs)
            if(mac.ad.equals(m)) {
                mac.ip = ip;
                mac.connected();
                macsAdapter.notifyDataSetChanged();
                Toast.makeText(this, Integer.toString(macs.size())+" appareils connectés", Toast.LENGTH_SHORT).show();
                return;
            }
        macs.add(new Mac(m,true));
        macsAdapter.notifyDataSetChanged();
    }

    /**
     * Another lan device was disconnected from the currently connected device.
     * @param m Its bluetooth mac address.
     */
    public void macDisconnected(String m) {
        for(Mac mac : macs)
            if(mac.ad.equals(m)) {
                mac.disconnected();
                macsAdapter.notifyDataSetChanged();
                Toast.makeText(this, Integer.toString(macs.size())+" appareils connectés", Toast.LENGTH_SHORT).show();
                return;
            }
        macs.add(new Mac(m,false));
        macsAdapter.notifyDataSetChanged();
    }

    /**
     * Setter method to change currently connected device's lan ip.
     * @param ip Its static ip.
     */
    public void setBTIp(int ip) {
        for(Mac mac : macs)
            if(mac.BTco) {
                mac.ip = ip;
                macsAdapter.notifyDataSetChanged();
                return;
            }
    }

    /**
     * Shows a MacEditDialog to edit a mac.
     * @param pos Position of the mac to edit.
     * @see MacEditDialog
     */
    public void editMac(final int pos) {
        MacEditDialog dialog = new MacEditDialog();
        dialog.setMac(macs.get(pos));
        dialog.setListener(new MacInterface() {
            @Override
            public void onMacAdded(Mac m) {
                deleteMac(pos,false);

                macs.add(pos,m);
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
        });
        dialog.show(fragmentManager,"DIALOG");
    }

    /**
     * Deletes a mac from macs.
     * @param position Position to remove.
     * @param showConfirmation Shows the user a confirmation dialog before removing.
     */
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
                            controls.notifyMacDeleted(position);
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

    /**
     * Saves macs to SharedPreferences to be restored on another app launch.
     * @see loadMacs()
     * @see macs
     */
    private void saveMacs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().remove(MACS_KEY).apply();
        HashSet<String> str = new HashSet<>();
        for(Mac m : macs)
            str.add(m.ad);
        prefs.edit().putStringSet(MACS_KEY,str).apply();
    }

    /**
     * Loads macs from SharedPreferences if there was a previous save.
     * @return Number of loaded macs.
     * @see saveMacs()
     * @see macs
     */
    private int loadMacs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> str = prefs.getStringSet(MACS_KEY, new HashSet<String>());
        macs = new ArrayList<>();
        for(String s : str)
            macs.add(new Mac(s,false));
        return macs.size();
    }


    /**
     * Kill the app correctly and close any open socket.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothStateChanged);
        bluetoothHandler.interrupt();
    }


}
