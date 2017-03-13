package com.project.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙扫描界面
 */
public class BlueToothScanActivity extends AppCompatActivity   implements View.OnClickListener{

    private LinearLayout     llBg;
    private ProgressBar      progressBar;
    private Button           btnScan;
    private RecyclerView     mRecyclerView;

    private String TAG="BlueToothScanActivity";
    private BluetoothManager blueToothManager;
    private BluetoothAdapter bluethoothAdapter;
    private Handler          mHandler=new Handler();
    private boolean          isScan=false;
    private List<BluetoothDevice> list=new ArrayList<>();
    private BlueToothDeviceAdapter  mAdapter;
    private LinearLayoutManager manager;


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_scan://扫描
              boolean  flag=isEnableBlueTooth();
              if(flag){
                   scanBlueToothDevice(true);
               }
                break;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化组件
        initView();
        //初始化数据
        initData();
    }
    /**
     * 初始化组件
     */
    private void initView() {
        llBg= (LinearLayout) findViewById(R.id.boothblue_bg);
        progressBar= (ProgressBar) findViewById(R.id.progressbar);
        btnScan=(Button)findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(this);
        mRecyclerView= (RecyclerView) findViewById(R.id.recyclerview);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //1.判断当前设备是否支持ble设备
        if(!getPackageManager().hasSystemFeature(getPackageManager().FEATURE_BLUETOOTH_LE)){
            Toast.makeText(BlueToothScanActivity.this,"当前设备不支持ble，即将推出",Toast.LENGTH_SHORT).show();
            finish();
        }
        //2.若支持蓝牙设备，判断设备是否打开，若没有打开，请求打开蓝牙设备
        blueToothManager= (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluethoothAdapter=blueToothManager.getAdapter();
        isEnableBlueTooth();

        setAdapter();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==10001){
                Toast.makeText(BlueToothScanActivity.this,"蓝牙已启用",Toast.LENGTH_SHORT).show();
            }
        }else if(resultCode==RESULT_CANCELED){
            if(requestCode==10001) {
                Toast.makeText(BlueToothScanActivity.this, "蓝牙未启用", Toast.LENGTH_SHORT).show();
            }
        }
        Log.i(TAG,"结果码:"+resultCode+"--->请求码："+requestCode);
    }



    /**
     * 判断蓝牙是否打开
     */
    private boolean isEnableBlueTooth() {
        if(bluethoothAdapter==null||!bluethoothAdapter.isEnabled()){
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,10001);
            return  false;
        }
        return   true;
    }

    /**
     * 扫描蓝牙设备
     */
    private void scanBlueToothDevice(boolean isEnable) {
        if(bluethoothAdapter!=null) {
            if (isEnable) {
                progressBar.setVisibility(View.VISIBLE);
                llBg.setVisibility(View.GONE);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isScan = false;
                        bluethoothAdapter.stopLeScan(mLeScanCallback);//停止扫描
                        progressBar.setVisibility(View.GONE);
                        llBg.setVisibility(View.VISIBLE);
                        Log.i(TAG,"扫描完成");
                        setAdapter();
                    }
                }, 10000);
                Log.i(TAG,"开始扫描");
                isScan = true;
                list.clear();
                bluethoothAdapter.startLeScan(mLeScanCallback);//开始扫描
            } else {
                Log.i(TAG,"结束扫描");
                isScan = false;
                bluethoothAdapter.stopLeScan(mLeScanCallback);//停止扫描
            }
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(TAG,"设备名："+device.getName()+"设备地址："+device.getAddress());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!list.contains(device)) {
                        list.add(device);
                     }
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setAdapter();
        boolean flag=isEnableBlueTooth();
        if(flag){
            scanBlueToothDevice(true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanBlueToothDevice(false);
    }


    public   void  setAdapter(){
        if(mAdapter==null){
            manager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
            mRecyclerView.setLayoutManager(manager);
            mAdapter=new BlueToothDeviceAdapter(list,this);
            mRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.notifyDataSetChanged();
        }
    }
}
