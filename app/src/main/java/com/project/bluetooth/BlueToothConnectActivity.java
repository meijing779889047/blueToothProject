package com.project.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 蓝牙设备连接界面
 */
public class BlueToothConnectActivity extends AppCompatActivity  implements View.OnClickListener {

    private TextView      tvDeviceName;
    private TextView      tvDeviceAddress;
    private TextView      tvDeviceStatus;
    private TextView      tvDevicConnect;
    private RecyclerView  mRecyclerView;

    private String TAG="BlueToothConnect";
    private String deviceName;//设备名
    private String deviceAddress;//设备地址
    private BluetoothLeService mBluetoothLeService;
    private boolean isConnected=false;//是否连接上
    private List<Map<String,String>>  mGattCharacteristicData=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_connect);
        //初始化组件
         initView();
        //初始化数据
         initData();
         //绑定服务
         bindBlueToothService();
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

    /**
     * 绑定服务
     */
    private void bindBlueToothService() {
        Intent  intent=new Intent(this,BluetoothLeService.class);
        boolean bindSuccess=bindService(intent,mConnect,BIND_AUTO_CREATE);
        if(bindSuccess){
            Log.i(TAG,"成功绑定服务");
        }else{
            Log.i(TAG,"绑定服务失败");
        }
    }


    ServiceConnection  mConnect=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          mBluetoothLeService=((BluetoothLeService.LocalBinder) service).getService();
          if(!mBluetoothLeService.initialize()){
              Log.e(TAG,"初始化蓝牙失败，即将退出");
              finish();
          }
           mBluetoothLeService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
           mBluetoothLeService=null;
        }
    };


    /**
     * 蓝牙广播接收器
     */
    class   BlueToothReceiver  extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String  action=intent.getAction();
            Log.i(TAG,"接收广播:"+action);
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){//蓝牙连接成功
                tvDeviceStatus.setText("连接状态：已连接");
                isConnected=true;
            }else  if(BlueToothService.ACTION_GATT_DISCONNECTED.equals(action)){//蓝牙连接断开
                tvDeviceStatus.setText("连接状态：未连接");
                isConnected=false;
            }else if(BlueToothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){//发现蓝牙gatt服务
                  //显示所有用户界面上的支持服务和特色
                 displayGattService(mBluetoothLeService.getSupportedGattServices());

            }else if(BlueToothService.ACTION_DATA_AVAILABLE.equals(action)){//获取到设备的数据

            }
        }
    }

    /**
     * 演示了如何遍历的支持关贸总协定服务/特征。在此示例中,我们填充绑定到的数据结构ExpandableListView UI
     * @param supportedGattServices
     */
    private void displayGattService(List<BluetoothGattService> supportedGattServices) {
        if(supportedGattServices==null){
            return;
        }
        String unKnownServiceStr="unKnownService";
        String unKnownCharacteristic="unKnownCharacteristic";
        String uuid="";
        //存储服务的集合
        List<Map<String,String>>  gattServiceData=new ArrayList<>();
        //存储Characteristic的集合
        List<Map<String,String>>  gattCharacteristicData=new ArrayList<>();
        mGattCharacteristicData.clear();
        //循环遍历所有的服务
        for (int i = 0; i <supportedGattServices.size() ; i++) {
            Map<String,String> map=new HashMap<>();
             uuid=supportedGattServices.get(i).getUuid().toString();
             map.put(Constanct.LIST_NAME,GattAttributes.lookup(uuid,unKnownCharacteristic));
             map.put(Constanct.LIST_UUID,uuid);
             gattServiceData.add(map);


            List<BluetoothGattCharacteristic> characteristics = supportedGattServices.get(i).getCharacteristics();


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
