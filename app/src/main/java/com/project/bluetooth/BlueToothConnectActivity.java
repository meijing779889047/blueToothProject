package com.project.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.project.bluetooth.Constanct.LIST_NAME;
import static com.project.bluetooth.Constanct.LIST_UUID;


/**
 * 蓝牙设备连接界面
 */
public class BlueToothConnectActivity extends AppCompatActivity  implements View.OnClickListener {

    private TextView      tvDeviceName;
    private TextView      tvDeviceAddress;
    private TextView      tvDeviceStatus;
    private TextView      tvDevicConnect;
    private TextView      tvData;
    private ExpandableListView  mGattServicesList;

    private String TAG="BlueToothConnect";
    private String deviceName;//设备名
    private String deviceAddress;//设备地址
    private BlueToothService mBlueToothService;
    private boolean isConnected=false;//是否连接上
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics=new ArrayList<>();
    private BlueToothReceiver receiver;//广播接收器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_connect);
        //初始化组件
         initView();
        //初始化数据
         initData();
        //注册广播
         registerBlueToothReceiver();
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
        tvData =(TextView) findViewById(R.id.tv_data);
        tvDevicConnect= (TextView) findViewById(R.id.tv_device_connect);
        tvDevicConnect.setOnClickListener(this);
        mGattServicesList= (ExpandableListView) findViewById(R.id.recyclerview_data);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
    }

    /**
     * 初始化数据
     */
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
     * 注册广播
     */
    private void registerBlueToothReceiver() {
        receiver=new BlueToothReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BlueToothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BlueToothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BlueToothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BlueToothService.ACTION_DATA_AVAILABLE);
        registerReceiver(receiver,intentFilter);
    }


    /**
     * 绑定服务
     */
    private void bindBlueToothService() {
        Intent  intent=new Intent(this,BlueToothService.class);
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
          mBlueToothService=((BlueToothService.LocalBinder) service).getService();
          if(!mBlueToothService.initBlueToothAdapter()){
              Log.e(TAG,"初始化蓝牙失败，即将退出");
              finish();
          }
           mBlueToothService.connectBlueTooth(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
           mBlueToothService=null;
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
            if(BlueToothService.ACTION_GATT_CONNECTED.equals(action)){//蓝牙连接成功
                tvDeviceStatus.setText("连接状态：已连接");
                isConnected=true;
            }else  if(BlueToothService.ACTION_GATT_DISCONNECTED.equals(action)){//蓝牙连接断开
                tvDeviceStatus.setText("连接状态：未连接");
                isConnected=false;
                mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
                tvData.setText("数据： ");
            }else if(BlueToothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){//发现蓝牙gatt服务
                  //显示所有用户界面上的支持服务和特色
                 displayGattService(mBlueToothService.getSupportedGattServices());

            }else if(BlueToothService.ACTION_DATA_AVAILABLE.equals(action)){//获取到设备的数据
                  displayData(intent);
            }
        }
    }


    /**
     * 演示了如何遍历的支持关贸总协定服务/特征。在此示例中,我们填充绑定到的数据结构ExpandableListView UI
     * @param services
     */
    private void displayGattService(List<BluetoothGattService> services)   {

        try {
        if(services==null){
            return;
        }

        String unKnownServiceStr="unKnownService";
        String unKnownCharacteristic="unKnownCharacteristic";
        String uuid="";
        //存储服务的集合  存储uuid
        List<Map<String,String>>  gattServiceData=new ArrayList<>();
        ArrayList<List<Map<String, String>>> gattCharacteristicData = new ArrayList<List<Map<String, String>>>();

        //循环遍历所有的服务
        for (int i = 0; i <services.size() ; i++) {

            //-----Service的字段信息-----//
            int type = services.get(i).getType();
            Log.e(TAG,"-->service type:"+Utils.getServiceType(type));
            Log.e(TAG,"-->includedServices size:"+services.get(i).getIncludedServices().size());
            Log.e(TAG,"-->service uuid:"+services.get(i).getUuid());

            Map<String,String> map=new HashMap<>();
             uuid=services.get(i).getUuid().toString();
             map.put(LIST_NAME,GattAttributes.lookup(uuid,unKnownCharacteristic));
             map.put(LIST_UUID,uuid);
             gattServiceData.add(map);


            //存储BluetoothGattCharacteristic
            List<BluetoothGattCharacteristic> chars=new ArrayList<>();
            //存储uuid_name uuid
            List<Map<String,String>>  gattCharacteristicGroupData=new ArrayList<>();

            final List<BluetoothGattCharacteristic> gattCharacteristics = services.get(i).getCharacteristics();
            for (  int j = 0; j <gattCharacteristics.size() ; j++) {

                Log.e(TAG,"---->char uuid:"+gattCharacteristics.get(j).getUuid());

                int permission = gattCharacteristics.get(j).getPermissions();
                Log.e(TAG,"---->char permission:"+Utils.getCharPermission(permission));

                int property = gattCharacteristics.get(j).getProperties();
                Log.e(TAG,"---->char property:"+Utils.getCharPropertie(property));

                byte[] data = gattCharacteristics.get(j).getValue();
                if (data != null && data.length > 0) {
                    Log.e(TAG,"---->char value:"+new String(data));
                }
                final int k=j;
                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                if(gattCharacteristics.get(j).getUuid().toString().equals(BlueToothService.UUID_HEART_RATE_MEASUREMENT)){
                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
                    final int finalJ = j;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBlueToothService.readCharacteristic(gattCharacteristics.get(k));
                        }
                    }, 500);

                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBlueToothService.setCharacteristicNotification( gattCharacteristics.get(j), true);
                    //设置数据内容
                    gattCharacteristics.get(j).setValue("send data->");
                    //往蓝牙模块写入数据
                    mBlueToothService.wirteCharacteristic( gattCharacteristics.get(j));
                }

                //-----Descriptors的字段信息-----//
                List<BluetoothGattDescriptor> gattDescriptors =  gattCharacteristics.get(j).getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    Log.e(TAG,"-------->desc permission:"+ Utils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        Log.e(TAG, "-------->desc value:"+ new String(desData));
                    }
                }

                chars.add(gattCharacteristics.get(j));
                Map<String,String>  maps=new HashMap<>();
                uuid=gattCharacteristics.get(j).getUuid().toString();
                maps.put(LIST_NAME,GattAttributes.lookup(uuid,unKnownServiceStr));
                maps.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(maps);
            }
            mGattCharacteristics.add(chars);
            gattCharacteristicData.add(gattCharacteristicGroupData);

        }
        for (int i = 0; i <gattServiceData.size() ; i++) {
            Log.i(TAG,"service   name:"+gattServiceData.get(i).get(LIST_NAME)+"--->uuid:"+gattServiceData.get(i).get(LIST_UUID));
        }

        for (int i = 0; i < gattCharacteristicData.size(); i++) {
            List<Map<String, String>> result = gattCharacteristicData.get(i);
            for (int j = 0; j <result.size() ; j++) {
                Log.i(TAG,"characteristic  name:"+result.get(j).get(LIST_NAME)+"--->uuid:"+result.get(j).get(LIST_UUID));
            }
        }
            SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                    this, gattServiceData,
                    android.R.layout.simple_expandable_list_item_2, new String[] {
                    LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
                    android.R.id.text2 }, gattCharacteristicData,
                    android.R.layout.simple_expandable_list_item_2, new String[] {
                    LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
                    android.R.id.text2 });
               mGattServicesList.setAdapter(gattServiceAdapter);
        }catch (Exception e){
            Log.e(TAG,"出现异常："+e.toString());
        }
    }

    /**
     * 获取设备发送过来的数据
     * @param intent
     */
    private void displayData(Intent intent) {
       String data= intent.getExtras().getString(BlueToothService.EXTRA_DATA);
        Log.i(TAG,"蓝牙设备数据："+data);
        tvData.setText("数据： "+data);
    }


    /**
     * 如果给定的关贸总协定特征选择,检查支持功能。这个示例演示了“读”和“通知”功能。看到http://d.android.com/reference/android/bluetooth/BluetoothGatt.html支持特征的完整列表
     */
    private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                                    int groupPosition, int childPosition, long id) {

            if (mGattCharacteristics != null) {
                final BluetoothGattCharacteristic characteristic = mGattCharacteristics
                        .get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();
                Log.i(TAG,"charaProp = " + charaProp + ",UUID = " + characteristic.getUuid().toString());
                if (characteristic.getUuid().toString().equals("00001543-0000-3512-2118-0009af100700")) {
                    characteristic.setValue("m".getBytes());
                    mBlueToothService.wirteCharacteristic(characteristic);
                    Log.i(TAG,"write  data  m");
                } else {
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        //如果有一个活跃的通知上的特点,首先明确所以没有更新的数据字段使用接口
                        // If there is an active notification on a
                        // characteristic, clear
                        // it first so it doesn't update the data field on the
                        // user interface.
                        if (mNotifyCharacteristic != null) {
                            mBlueToothService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        mBlueToothService.readCharacteristic(characteristic);

                    }
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

                    if (characteristic.getUuid().toString().equals("0000fff6-0000-1000-8000-00805f9b34fb")||characteristic.getUuid().toString().equals("0000fff4-0000-1000-8000-00805f9b34fb")) {
                        System.out.println("enable notification");
                        mNotifyCharacteristic = characteristic;
                        mBlueToothService.setCharacteristicNotification(
                                characteristic, true);

                    }
                }

                return true;
            }
            return false;
        }
    };



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_device_connect:
                mBlueToothService.connectBlueTooth(deviceAddress);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(mConnect);
    }
}
