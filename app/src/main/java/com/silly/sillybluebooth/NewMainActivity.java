package com.silly.sillybluebooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class NewMainActivity extends Activity {
    private final static String TAG = NewMainActivity.class.getSimpleName();

    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private ImageView mac_windows;
    private ImageView mac;
    private ImageView windows;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);


        initView();
        initEvent();

        initData();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
    }

    private void initData() {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private void initEvent() {

        LongClickUtils.setLongClick(new Handler(), mac, 1000, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mBluetoothLeService.WriteValue("A");
                SystemClock.sleep(1000);
                mBluetoothLeService.WriteValue("a");
                return true;
            }
        });

        LongClickUtils.setLongClick(new Handler(), windows, 1000, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mBluetoothLeService.WriteValue("B");
                SystemClock.sleep(1000);
                mBluetoothLeService.WriteValue("b");
                return true;
            }
        });

        LongClickUtils.setLongClick(new Handler(), mac_windows, 1000, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mBluetoothLeService.WriteValue("C");
                SystemClock.sleep(1000);
                mBluetoothLeService.WriteValue("c");
                return true;
            }
        });

    }

    private void initView() {
        mac_windows = (ImageView) findViewById(R.id.mac_windows);
        mac = (ImageView) findViewById(R.id.mac);
        windows = (ImageView) findViewById(R.id.windows);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            Log.e(TAG, "mBluetoothLeService is okay");
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
            mBluetoothLeService.connect("00:15:83:30:78:E4");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //连接成功
                Log.e(TAG, "Only gatt, just wait");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
                mConnected = false;
                Toast.makeText(NewMainActivity.this, "蓝牙已断开", Toast.LENGTH_LONG).show();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //可以开始干活了
            {
                mConnected = true;
                Toast.makeText(NewMainActivity.this, "蓝牙已连接", Toast.LENGTH_LONG).show();
                Log.e(TAG, "In what we need");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
                Log.e(TAG, "RECV DATA");
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);

            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {                        //注册接收的事件
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }
}
