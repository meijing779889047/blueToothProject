package com.project.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
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
   public final static String ACTION_GATT_CONNECTED           = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED        = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE           = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA                      = "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID
            .fromString(GattAttributes.HEART_RATE_MEASUREMENT);

    private  final Binder   mBinder=new LocalBinder();
    class   LocalBinder  extends   Binder{
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
        mBluetoothGatt=device.connectGatt(this,false,mGattCallback);//进行蓝牙连接，并监听回掉
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
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
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
     * 想ble写入characteristic
     * @param characteristic
     */
    public void wirteCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }


        mBluetoothGatt.writeCharacteristic(characteristic);

    }
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic
     *            The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * 启用或禁用通知给特征
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
                .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor != null) {
            System.out.println("write descriptor");
            descriptor
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
		/*
		 * // This is specific to Heart Rate Measurement. if
		 * (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
		 * System
		 * .out.println("characteristic.getUuid() == "+characteristic.getUuid
		 * ()+", "); BluetoothGattDescriptor descriptor =
		 * characteristic.getDescriptor
		 * (UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
		 * descriptor
		 * .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		 * mBluetoothGatt.writeDescriptor(descriptor); }
		 */
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        //当连接上设备或者失去连接时会回调该函数
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            String intentAction;
            System.out.println("=======status:" + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                sendBroadCast(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:"
                        + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                sendBroadCast(intentAction);
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

         //当设备是否找到服务时，会回调该函数
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadCast(ACTION_GATT_SERVICES_DISCOVERED);
            }
                Log.w(TAG, "onServicesDiscovered received: " + status);

        }
        //当向设备读取数据时会回调该函数
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadCast(ACTION_DATA_AVAILABLE, characteristic);
            }
            if (status == BluetoothGatt.GATT_SUCCESS)
                Log.e(TAG,"onCharRead "+gatt.getDevice().getName()
                        +" read "
                        +characteristic.getUuid().toString()
                        +" -> "
                        +Utils.bytesToHexString(characteristic.getValue()));
        }
        //当向设备写入数据，返回数据时会回调该函数
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            byte[]  data=characteristic.getValue();
            if(data!=null&&data.length>0) {
                Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
                        + " write "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + new String(characteristic.getValue()));
            }
        };

        //当向设备Descriptor中写数据时，会回调该函数
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {

            System.out.println("onDescriptorWriteonDescriptorWrite = " + status
                    + ", descriptor =" + descriptor.getUuid().toString());

        }

        //当读取设备时会回调该函数
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }
//当向设备D

        //设备发出通知时会调用到该接口  对中心设备与外围设备的传输数据的处理发生在onCharacteristicChanged()里
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            sendBroadCast(ACTION_DATA_AVAILABLE, characteristic);
            if (characteristic.getValue() != null) {

                System.out.println(characteristic.getStringValue(0));
            }
            System.out.println("--------onCharacteristicChanged-----");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            System.out.println("rssi = " + rssi);
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

                Log.i(TAG,"从ble设备获取到的数据："+ new String(data) + "\n" + stringBuilder.toString());
                intent.putExtra(EXTRA_DATA, new String(data) + "\n"+ stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }



}
