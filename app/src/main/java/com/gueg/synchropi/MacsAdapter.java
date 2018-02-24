package com.gueg.synchropi;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class MacsAdapter extends RecyclerView.Adapter<MacsAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Mac> list;


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView connected;
        TextView mac;
        TextView ip;
        ViewHolder(View v) {
            super(v);
            connected = v.findViewById(R.id.row_mac_enabled);
            mac = v.findViewById(R.id.row_mac_address);
            ip = v.findViewById(R.id.row_mac_ip);
        }
    }

    MacsAdapter(Context context, ArrayList<Mac> list) {
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
        holder.mac.setText(list.get(position).ad);
        holder.ip.setText(new StringBuilder("150.150.150.").append(list.get(position).ip));
        if(list.get(position).BTco)
            holder.connected.setImageDrawable(context.getResources().getDrawable(R.drawable.btconnected));
        else if(list.get(position).co)
            holder.connected.setImageDrawable(context.getResources().getDrawable(R.drawable.connected));
        else
            holder.connected.setImageDrawable(context.getResources().getDrawable(R.drawable.disconnected));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }



}
