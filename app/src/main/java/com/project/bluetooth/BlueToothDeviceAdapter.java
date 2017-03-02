package com.project.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 蓝牙列表适配器
 * Created by meijing on 2017/3/2.
 */

public class BlueToothDeviceAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    private List<BluetoothDevice>  list;
    private Context                context;

    public BlueToothDeviceAdapter(List<BluetoothDevice> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_recyclerview_layout,null);
        RecyclerViewHolder  holder=new RecyclerViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
            final BluetoothDevice  result=list.get(position);
            //设备名
            String  name=result.getName();
            holder.getTvName().setText("设备："+name);
            //地址
            String  address=result.getAddress();
            holder.getTvAddress().setText("macd地址："+address);

            holder.getLlBg().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle  bundle=new Bundle();
                    bundle.putString("name",result.getName());
                    bundle.putString("address",result.getAddress());
                    Intent  intent=new Intent(context,BlueToothConnectActivity.class);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
