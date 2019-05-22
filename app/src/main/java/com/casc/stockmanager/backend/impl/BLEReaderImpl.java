package com.casc.stockmanager.backend.impl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.casc.stockmanager.MyVars;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * TagReader的Bluetooth实现
 */
public class BLEReaderImpl extends BaseReaderImpl {

    private static final String TAG = BLEReaderImpl.class.getSimpleName();

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 串口

    // 读写器蓝牙相关字段
    private BluetoothAdapter mBLEAdapter;
    private BluetoothSocket mBLESocket;
    private InputStream mInStream; // 蓝牙输入流
    private OutputStream mOutStream; // 蓝牙输出流

    public BLEReaderImpl(Context context) {
        super(context);
        this.mBLEAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBLEAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(enableBtIntent);
        }

        MyVars.executor.execute(new BLEConnectTask());
    }

    @Override
    void write(byte[] data) throws IOException {
        mOutStream.write(data);
    }

    @Override
    int read(byte[] data) throws IOException {
        return mInStream.read(data);
    }

    @Override
    synchronized void lostConnection() {
        super.lostConnection();
        if (mState == STATE_CONNECTED) {
            try {
                mBLESocket.close();
            } catch (Exception e) {
                Log.i(TAG, "Close bluetooth socket error");
            } finally {
                mBLESocket = null;
            }
        }
    }

    /**
     * Bluetooth连接线程
     */
    private class BLEConnectTask implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    if (mState == STATE_CONNECTED || MyVars.usbReader.isConnected()) {
                        Thread.sleep(1000);
                    } else {
                        Set<BluetoothDevice> pairedDevices = mBLEAdapter.getBondedDevices();// 获取本机已配对设备
                        if (pairedDevices != null && pairedDevices.size() > 0) {
                            for (BluetoothDevice device : pairedDevices) {
                                if (device.getName().equals("UL05")) {
                                    mBLESocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                                    mBLESocket.connect();
                                    mInStream = mBLESocket.getInputStream();
                                    mOutStream = mBLESocket.getOutputStream();
                                    mState = STATE_CONNECTED;
                                    break;
                                }
                            }
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    Log.i(TAG, "Error when connect to bluetooth reader");
                }
            }
        }
    }
}
