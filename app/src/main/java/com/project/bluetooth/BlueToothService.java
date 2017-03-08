package com.project.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

/**
 * Created by meijing on 2017/3/5.
 */

public class BlueToothService extends Service {

    private  String TAG="BlueToothService";
    private  BluetoothManager   mBluetoothManager;//蓝牙管理器
    private  BluetoothAdapter   mBluetoothAdapter;//蓝牙适配器
    private  String             mBlueToothAddress;//蓝牙连接地址
    private  BluetoothGatt      mBluetoothGatt;//用于进行蓝牙连接/断开
    //连接状态
    private int                 mConnectionState=STATE_DISCONNECTED;
    private static final int    STATE_DISCONNECTED = 0;
    private static final int    STATE_CONNECTING = 1;
    private static final int    STATE_CONNECTED = 2;

   //蓝牙连接状态广播
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID
            .fromString(GattAttributes.HEART_RATE_MEASUREMENT);

    private  final Binder   mBinder=new LocalBinder();
    private  class   LocalBinder  extends   Binder{
          BlueToothService getService(){
             return  BlueToothService.this;
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"蓝牙服务开启");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"绑定蓝牙服务");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"蓝牙服务销毁");
    }

    /**
     * 1.初始化蓝牙适配器  判断设备是否支持蓝牙设备
     * @return
     */
    public   boolean  initBlueToothAdapter(){
        if(mBluetoothAdapter==null){
            mBluetoothManager= (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if(mBluetoothManager==null){
            Log.e(TAG,"初始化蓝牙管理器失败");
            return   false;
        }
        mBluetoothAdapter=mBluetoothManager.getAdapter();
        if(mBluetoothAdapter==null){
            Log.e(TAG,"初始化蓝牙适配器失败");
            return   false;
        }
        return  true;
    }

    /**
     * 2.通过地址进行蓝牙连接服务
     * @param address
     * @return
     */
    public   boolean  connectBlueTooth(String address){
           if(mBluetoothAdapter==null|| TextUtils.isEmpty(address)){
               Log.e(TAG,"连接失败,蓝牙设备器为空 或者 无效的蓝牙连接地址");
               return   false;
           }
          if(!TextUtils.isEmpty(address)&&!TextUtils.isEmpty(mBlueToothAddress)&&address.equals(mBlueToothAddress)){
              Log.i(TAG,"连接一个已经存在的蓝牙连接地址");
              if(mBluetoothGatt.connect()){
                  mConnectionState=STATE_CONNECTING;
                  return   true;
              }else{
                  return  false;
              }
          }

          //通过地址获取对应的蓝牙设备
          BluetoothDevice  device=mBluetoothAdapter.getRemoteDevice(address);
          if(device==null){
              Log.e(TAG,"没有获取到对应的蓝牙设备，无法进行连接");
              return   false;
          }
        mBluetoothGatt=device.connectGatt(this,false,mBluetoothGattCallback);//进行蓝牙连接，并监听回掉
        Log.i(TAG,"进行蓝牙连接");
        mBlueToothAddress=address;
        mConnectionState=STATE_CONNECTING;
        return   true;
    }

    /**
     * 断开蓝牙连接
     */
    public   void  disConnectBlueTooth(){
        if(mBluetoothAdapter==null||mBluetoothGatt==null){
            Log.e(TAG,"无蓝牙设备连接,无需断开蓝牙设备");
            return;
        }
        mBluetoothGatt.disconnect();
        Log.i(TAG,"断开蓝牙设备");
    }

    /**
     * 关闭蓝牙设备
     */
    public   void closeBlueTooth(){
        if(mBluetoothGatt==null){
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt=null;

    }

    /**
     * 连接蓝牙设备的回调监听
     */
    public   final BluetoothGattCallback mBluetoothGattCallback=new BluetoothGattCallback() {


        /**
         * 连接gattService状态回掉
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status==BluetoothGatt.GATT_SUCCESS){
                sendBroadCast(ACTION_GATT_SERVICES_DISCOVERED);
            }
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }

        /**
         * 连接状态改变回掉
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG,"status:"+newState);
            if(newState==BluetoothProfile.STATE_CONNECTED){//连接成功
                mConnectionState=STATE_CONNECTED;
                sendBroadCast(ACTION_GATT_CONNECTED);
            }else if(newState==BluetoothProfile.STATE_DISCONNECTED){//连接断开
                mConnectionState=STATE_DISCONNECTED;
                sendBroadCast(ACTION_GATT_DISCONNECTED);

            }
        }

        /**
         * 读取到设备发送过来的数据回掉
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG,"onCharacteristicRead（） status:"+status);
            if(status==BluetoothGatt.GATT_SUCCESS){
                sendBroadCast(ACTION_DATA_AVAILABLE,characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            System.out.println("--------write success----- status:" + status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            sendBroadCast(ACTION_DATA_AVAILABLE, characteristic);
            if (characteristic.getValue() != null) {

                System.out.println(characteristic.getStringValue(0));
            }
           Log.i(TAG,"--------onCharacteristicChanged-----");
        }



        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            System.out.println("onDescriptorWriteonDescriptorWrite = " + status
                    + ", descriptor =" + descriptor.getUuid().toString());
        }



        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.i(TAG,"rssi = " + rssi);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };






    /****发送广播*****************************************************************/

    private void sendBroadCast(String action) {
         Intent  intent=new Intent(action);
         sendBroadcast(intent);
    }

    private void sendBroadCast(String action, BluetoothGattCharacteristic characteristic) {
         Intent  intent=new Intent(action);
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            System.out.println("Received heart rate: %d" + heartRate);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        }else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(
                        data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));

                System.out.println("ppp" + new String(data) + "\n"
                        + stringBuilder.toString());
                intent.putExtra(EXTRA_DATA, new String(data) + "\n"
                        + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }



}
