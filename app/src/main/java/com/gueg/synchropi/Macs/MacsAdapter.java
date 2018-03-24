package com.gueg.synchropi.Macs;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gueg.synchropi.R;

import java.util.ArrayList;


public class MacsAdapter extends RecyclerView.Adapter<MacsAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Mac> list;


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView connected;
        TextView mac;
        TextView ip;
        TextView delay;
        ViewHolder(View v) {
            super(v);
            connected = v.findViewById(R.id.row_mac_enabled);
            mac = v.findViewById(R.id.row_mac_address);
            ip = v.findViewById(R.id.row_mac_ip);
            delay = v.findViewById(R.id.row_mac_delay);
        }
    }

    public MacsAdapter(Context context, ArrayList<Mac> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MacsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_mac, parent, false);
        return new MacsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MacsAdapter.ViewHolder holder, final int position) {
        Mac m = list.get(position);
        holder.mac.setText(m.ad);
        if(m.ip!=Mac.UNKNOWN_IP)
            holder.ip.setText(new StringBuilder("150.150.150.").append(m.ip));
        else
            holder.ip.setText("IP inconnue");

        holder.delay.setText(Integer.toString(m.delay)+" ms");

        if(m.BTco)
            holder.connected.setImageDrawable(context.getResources().getDrawable(R.drawable.btconnected));
        else if(m.co)
            holder.connected.setImageDrawable(context.getResources().getDrawable(R.drawable.connected));
        else
            holder.connected.setImageDrawable(context.getResources().getDrawable(R.drawable.disconnected));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
