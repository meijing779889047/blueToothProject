package com.project.bluetooth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * 蓝牙设备连接界面
 */
public class BlueToothConnectActivity extends AppCompatActivity  implements View.OnClickListener {

    private TextView      tvDeviceName;
    private TextView      tvDeviceAddress;
    private TextView      tvDeviceStatus;
    private TextView      tvDevicConnect;
    private RecyclerView  mRecyclerView;

    private String deviceName;
    private String deviceAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_connect);
        //初始化组件
         initView();
        //初始化数据
         initData();
    }
    /**
     * 初始化组件
     */
    private void initView() {
        tvDeviceName= (TextView) findViewById(R.id.tv_device_name);
        tvDeviceAddress= (TextView) findViewById(R.id.tv_device_address);
        tvDeviceStatus =(TextView) findViewById(R.id.tv_device_statu);
        tvDevicConnect= (TextView) findViewById(R.id.tv_device_connect);
        tvDevicConnect.setOnClickListener(this);
        mRecyclerView= (RecyclerView) findViewById(R.id.recyclerview_data);
    }
    private void initData() {
        Bundle  bundle=getIntent().getExtras();
        if(bundle!=null){
            deviceName= bundle.getString("name");
            tvDeviceName.setText("蓝牙设备："+deviceName);
            deviceAddress= bundle.getString("address");
            tvDeviceAddress.setText("蓝牙设备的mac地址："+deviceAddress);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_device_connect:
                break;
        }
    }
}
