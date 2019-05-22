package com.casc.stockmanager.backend.impl;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.util.Map;

/**
 * TagReader的USB实现
 */
public class USBReaderImpl extends BaseReaderImpl {

    private static final String TAG = USBReaderImpl.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = "INTENT.USB_PERMISSION";

    // 读写器USB相关的字段
    private UsbManager mUsbManager;
    private UsbDevice mUsbDevice;
    private UsbSerialPort mSerialPort;

    public USBReaderImpl(Context context) {
        super(context);
        this.mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                    boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    if (granted) {
                        buildConnection();
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
                    buildConnection();
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                    lostConnection();
                }
            }
        };
        mContext.registerReceiver(receiver, filter);

        // 尝试建立连接
        buildConnection();
    }

    @Override
    void write(byte[] data) throws IOException {
        mSerialPort.write(data, 0);
    }

    @Override
    int read(byte[] data) throws IOException {
        return mSerialPort.read(data, 100);
    }

    @Override
    void lostConnection() {
        super.lostConnection();
        try {
            mSerialPort.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ignored) {
        } finally {
            mUsbDevice = null;
            mSerialPort = null;
        }
    }

    @Override
    public void start() {
        super.start();
        if (mState != STATE_CONNECTED) {
            buildConnection();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (mState == STATE_CONNECTED) {
            lostConnection();
        }
    }

    /**
     * 通过USB建立到读写器的连接
     */
    private void buildConnection() {
        Map<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();
        if (deviceMap != null && !deviceMap.isEmpty()) {
            // 获取到设备列表
            for (UsbDevice device : deviceMap.values()) {
                Log.i(TAG, device.getDeviceName() + " with vid = " + device.getVendorId() + " AND pid = " + device.getProductId());
                mUsbDevice = device;
            }
            // 尝试连接到设备，并打开Serial端口，打开失败则提示用户获取权限
            try {
                mSerialPort = new Cp21xxSerialDriver(mUsbDevice).getPorts().get(0);
                mSerialPort.open(mUsbManager.openDevice(mUsbDevice));
                mSerialPort.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                mState = STATE_CONNECTED;
            } catch (NullPointerException e) {
                Log.i(TAG, "Need permission when build connection with reader by usb");
                mUsbManager.requestPermission(mUsbDevice, PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0));
            } catch (IOException e) {
                Log.i(TAG, "IOException when connect reader with usb");
            }
        }
    }
}
