package com.project.bluetooth.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.project.bluetooth.R;

/**
 * Created by meijing on 2017/3/2.
 */

public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    private TextView  tvAddress;
    private TextView  tvName;
    private LinearLayout  llBg;

    public RecyclerViewHolder(View itemView) {
        super(itemView);
        tvName= (TextView) itemView.findViewById(R.id.tv_name);
        tvAddress= (TextView) itemView.findViewById(R.id.tv_address);
        llBg= (LinearLayout) itemView.findViewById(R.id.item_bg);
    }

    public TextView getTvName() {
        return tvName;
    }

    public void setTvName(TextView tvName) {
        this.tvName = tvName;
    }

    public LinearLayout getLlBg() {
        return llBg;
    }

    public void setLlBg(LinearLayout llBg) {
        this.llBg = llBg;
    }

    public TextView getTvAddress() {
        return tvAddress;
    }

    public void setTvAddress(TextView tvAddress) {
        this.tvAddress = tvAddress;
    }
}
