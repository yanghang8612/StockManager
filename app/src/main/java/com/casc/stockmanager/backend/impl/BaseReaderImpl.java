package com.casc.stockmanager.backend.impl;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.casc.stockmanager.MyParams;
import com.casc.stockmanager.MyVars;
import com.casc.stockmanager.backend.TagReader;
import com.casc.stockmanager.helper.InsHelper;
import com.casc.stockmanager.helper.SpHelper;
import com.casc.stockmanager.message.PollingResultMessage;
import com.casc.stockmanager.message.ReadResultMessage;
import com.casc.stockmanager.message.WriteResultMessage;
import com.casc.stockmanager.utils.CommonUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

abstract class BaseReaderImpl implements TagReader {

    private static final String TAG = BaseReaderImpl.class.getSimpleName();

    private static final int SIGNAL_TIMEOUT = 150 * 1000 * 1000;
    private static final int DISCOVERY_INTERVAL = 1500; // ms
    private static final int CMD_MAX_TRY_COUNT = 10;

    // 上下文
    Context mContext;

    // 读写器的连接状态
    int mState = TagReader.STATE_NONE;

    // 读写器的工作状态
    boolean mIsRunning;

    // 读写器的运行标志
    private boolean mIsShutdown;

    // 读写器轮询标志
    private boolean mIsPolling;

    // 是否正在执行读写任务
    private boolean mIsExecuteTask;

    // 读写器的MASK，要执行的指令，以及指令执行的结果
    private byte[] mMask, mCmd, mResult;

    // 读写器Mask的Mode
    private byte mMode;

    // 指令已经尝试执行的次数
    private int mTryCount;

    // 读写器的功率和Q值
    private volatile String mPower, mQValue;

    // 读写器相关设置情况
    private volatile boolean mIsMaskSet = true, mIsModeSet = true;

    private Lock mLock = new ReentrantLock();

    private Condition mPowerSet = mLock.newCondition();

    private Condition mQValueSet = mLock.newCondition();

    private Condition mMaskSetup = mLock.newCondition();

    private Condition mModeSetup = mLock.newCondition();

    private Condition mPollingStopped = mLock.newCondition();

    private Condition mSensorSetup = mLock.newCondition();

    private Condition mSensorSignal = mLock.newCondition();

    private Condition mCmdSignal  = mLock.newCondition();

    private Condition mTaskComplete  = mLock.newCondition();

    private long mLastReceivedDataTime;

    BaseReaderImpl(Context context) {
        mContext = context;
        MyVars.executor.execute(new WriteTask());
        MyVars.executor.execute(new ReadTask());
    }

    @Override
    public synchronized void sendCommand(byte[] cmd) {
        if (!mIsExecuteTask) {
            mIsExecuteTask = true;
            mCmd = cmd;
            mTryCount = 0;
        }
    }

    @Override
    public synchronized byte[] sendCommandSync(byte[] cmd) {
        // TODO: 2018.9.17 当前后两个执行的指令为读读或写写时，无法通过指令类型来判定后一条指令执行情况
        mResult = null;
        if (!mIsExecuteTask) {
            mIsExecuteTask = true;
            mCmd = cmd;
            mTryCount = 0;
            mLock.lock();
            try {
                mTaskComplete.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally { // 这里若成功执行完毕后被唤醒，后续尚未执行的指令并没有继续执行的意义，因此需要清空写队列
                mLock.unlock();
            }
        }
        return mResult;
    }

    @Override
    public void setMask(byte[] mask, int mode) {
        mIsMaskSet = false;
        mMask = mask;
        if (mode != 2) {
            mIsModeSet = false;
            mMode = (byte) mode;
        } else {
            mIsModeSet = true;
        }
    }

    @Override
    public boolean isConnected() {
        return mState == STATE_CONNECTED;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void start() {
        mIsRunning = true;
    }

    @Override
    public void pause() {
        mIsRunning = false;
    }

    @Override
    public void stop() {
        mIsRunning = false;
    }

    @Override
    public void shutdown() {
        mIsRunning = false;
        mIsShutdown = true;
        lostConnection();
    }

    abstract void write(byte[] data) throws IOException;

    abstract int read(byte[] data) throws IOException;

    @CallSuper
    void lostConnection() {
        if (mState == STATE_CONNECTED) {
            if (mIsExecuteTask) {
                mLock.lock();
                mTaskComplete.signal();
                mLock.unlock();
            }
            mState = STATE_NONE;
            mPower = mQValue = "";
        }
    }

    private void clearMask() {
        mMode = 2;
        mIsModeSet = false;
    }

    /**
     * 写数据线程，该线程一直工作在后台
     */
    private class WriteTask implements Runnable {

        @Override
        public void run() { // 循环从写队列中取指令帧尝试下发给读写器，若队列为空或读写器尚未连接则休眠指定时间后重试
            Thread.currentThread().setName(
                    BaseReaderImpl.this.getClass().getSimpleName().substring(0, 3)
                            + getClass().getSimpleName());

            while (!mIsShutdown) {
                try {
                    if (mState != STATE_CONNECTED) { // 读写器未连接状态下则不发送任何指令
                        Thread.sleep(1);
                    } else if (!SpHelper.getString(MyParams.S_POWER).equals(mPower)) { // 配置发射功率，写任务阻塞
                        write(InsHelper.setTransmitPower(SpHelper.getInt(MyParams.S_POWER)));
                        waitForCondition("Set Power", mPowerSet);
                    } else if (!SpHelper.getString(MyParams.S_Q_VALUE).equals(mQValue)) { // 配置Q值，写任务阻塞
                        write(InsHelper.setQueryParameter(
                                (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                SpHelper.getInt(MyParams.S_Q_VALUE)));
                        waitForCondition("Set Q", mQValueSet);
                    } else if (!mIsMaskSet) { // 设置MASK，写任务阻塞
                        write(InsHelper.getSelectParameter(
                                (byte) 0x00,
                                mIsModeSet ? (byte) 0x00 : (byte) 0x04,
                                InsHelper.MemBankType.EPC,
                                CommonUtils.hexToBytes("00000020"),
                                false,
                                mMask));
                        waitForCondition("Set Mask", mMaskSetup);
                    } else if (!mIsModeSet) {
                        write(InsHelper.getSelectModel(mMode));
                        waitForCondition("Set Mode", mModeSetup);
                    } else if (mIsRunning) { // 除却读写器设置外的指令均在读写器工作标志位为true时才下发
                        if (mIsExecuteTask && mIsPolling) {
                            write(InsHelper.getStopMultiPolling());
                            waitForCondition("Stop Polling", mPollingStopped);
                        } else if ((!mIsExecuteTask && !mIsPolling)
                                || System.currentTimeMillis() - mLastReceivedDataTime > 100) {
                            mIsPolling = true;
                            write(InsHelper.getMultiPolling(65535));
                        } else if (mIsExecuteTask && mTryCount < CMD_MAX_TRY_COUNT) { // 队列不空则取队首指令发送，写任务阻塞
                            write(mCmd);
                            if (MyParams.PRINT_COMMAND) {
                                Log.i(TAG, "Send: " + CommonUtils.bytesToHex(mCmd));
                            }
                            waitForCondition("Execute cmd", mCmdSignal);
                        }
                    } else if (mIsPolling) {
                        write(InsHelper.getStopMultiPolling());
                        waitForCondition("Stop Polling", mPollingStopped);
                    } else {
                        Thread.sleep(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    lostConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void waitForCondition(String comment, Condition condition) throws InterruptedException {
            mLock.lock();
            condition.awaitNanos(SIGNAL_TIMEOUT);
//            Log.i(TAG, comment + " cost: " + ((SIGNAL_TIMEOUT - condition.awaitNanos(SIGNAL_TIMEOUT)) / 1000 / 1000));
            mLock.unlock();
        }
    }

    /**
     * 读数据线程，该线程一直工作在后台
     */
    private class ReadTask implements Runnable {

        @Override
        public void run() {
            Thread.currentThread().setName(
                    BaseReaderImpl.this.getClass().getSimpleName().substring(0, 3)
                            + getClass().getSimpleName());

            byte[] temp = new byte[1024];
            byte[] data = new byte[2 * temp.length];
            int leftCount = 0, endIndex;
            while (!mIsShutdown) {
                try {
                    Thread.sleep(1);
                    if (mState != STATE_CONNECTED) continue;
                    int readCount = read(temp);
                    if (readCount > 0) {
                        mLastReceivedDataTime = System.currentTimeMillis();
                    }
                    System.arraycopy(temp, 0, data, leftCount, readCount);
                    leftCount += readCount;endIndex = -1;
                    for (int i = 0; i < leftCount; i++) {
                        if (data[i] == InsHelper.INS_HEADER) {
                            for (int j = i + 1; j < leftCount; j++) {
                                if (data[j] == InsHelper.INS_END && InsHelper.checkIns(data, i, j)) {
//                                    Log.i(TAG, CommonUtils.bytesToHex(Arrays.copyOfRange(data, i, j + 1)));
                                    switch (data[i + 2] & 0xFF) {
                                        case 0xB6:
                                            mPower = SpHelper.getString(MyParams.S_POWER);
                                            sendSignal(mPowerSet);
                                            break;
                                        case 0x0E:
                                            mQValue = SpHelper.getString(MyParams.S_Q_VALUE);
                                            sendSignal(mQValueSet);
                                            break;
                                        case 0x0C:
                                            mIsMaskSet = true;
                                            sendSignal(mMaskSetup);
                                            break;
                                        case 0x12:
                                            mIsModeSet = true;
                                            sendSignal(mModeSetup);
                                            break;
                                        case 0x28:
                                            mIsPolling = false;
                                            sendSignal(mPollingStopped);
                                            break;
                                        case 0x22: // 轮询通知帧
                                            int pl = ((data[i + 3] & 0xFF) << 8) + (data[i + 4] & 0xFF);
                                            byte[] epc = Arrays.copyOfRange(data, i + 8, i + pl + 3);
                                            if (!mIsExecuteTask) {
                                                EventBus.getDefault().post(new PollingResultMessage(data[5], epc));
                                            }
                                            break;
                                        case 0xFF:
                                            if (data[i + 5] == 0x15) {
                                                if (!mIsExecuteTask) {
                                                    EventBus.getDefault().post(new PollingResultMessage());
                                                }
                                            } else {
                                                if (MyParams.PRINT_COMMAND) {
                                                    Log.i(TAG, "Recv: " + CommonUtils.bytesToHex(Arrays.copyOfRange(data, i, j + 1)));
                                                }
                                                if (++mTryCount >= CMD_MAX_TRY_COUNT) {
                                                    mIsExecuteTask = false;
                                                    sendSignal(mTaskComplete);
                                                }
                                                sendSignal(mCmdSignal);
                                            }
                                            break;
                                        default:
                                            if (MyParams.PRINT_COMMAND) {
                                                Log.i(TAG, "Recv: " + CommonUtils.bytesToHex(Arrays.copyOfRange(data, i, j + 1)));
                                            }
                                            mResult = Arrays.copyOfRange(data, i, j + 1);
                                            if (mResult[2] == mCmd[2]) {
                                                if (mIsExecuteTask) {
                                                    if ((mResult[2] & 0xFF) == 0x39)
                                                        EventBus.getDefault().post(new ReadResultMessage(mResult));
                                                    if ((mResult[2] & 0xFF) == 0x49)
                                                        EventBus.getDefault().post(new WriteResultMessage(mResult));
                                                }
                                                mIsExecuteTask = false;
                                                sendSignal(mTaskComplete);
                                                sendSignal(mCmdSignal);
                                            }
                                    }
                                    endIndex = i = j;
                                    break;
                                }
                            }
                        }
                    }
                    System.arraycopy(data, endIndex + 1, data, 0, leftCount -= endIndex + 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    lostConnection();
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.i(TAG, "Expand read buffer");
                    e.printStackTrace();
                    data = new byte[data.length * 10];
                    temp = new byte[temp.length * 10];
                } catch (IllegalArgumentException e) {
                    Log.i(TAG, "Bucket Tag error code : " + CommonUtils.bytesToHex(Arrays.copyOf(data, leftCount)));
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendSignal(Condition signal) {
            mLock.lock();
            signal.signal();
            mLock.unlock();
        }
    }
}
