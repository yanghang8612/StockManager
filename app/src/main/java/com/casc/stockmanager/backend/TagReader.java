package com.casc.stockmanager.backend;

/**
 * UL6+通信模块接口
 */
public interface TagReader {

    /**
     * Reader状态常量
     */
    int STATE_NONE = 0;
    int STATE_CONNECTING = 1;
    int STATE_CONNECTED = 2;

    /**
     * 向读写器下发指定次数的指令，非阻塞
     *
     * @param cmd 指令byte数组
     */
    void sendCommand(byte[] cmd);

    /**
     * 向读写器下发指定次数的指令，阻塞
     *
     * @param cmd 指令byte数组
     * @return 执行完毕后读写器返回的结果，若无正确结果则返回null
     */
    byte[] sendCommandSync(byte[] cmd);

    /**
     * 设置读写器的MASK
     *
     * @param mask 要设置的MASK值
     */
    void setMask(byte[] mask, int mode);

    /**
     * 检测读写器连接状态
     *
     * @return true：读写器已连接；false：读写器未连接
     */
    boolean isConnected();

    /**
     * 获取读写器的当前状态
     *
     * @return Reader状态常量
     */
    int getState();

    /**
     * 开始读写器工作
     */
    void start();

    /**
     * 暂停读写器工作，但不断开连接
     */
    void pause();

    /**
     * 停止读写器工作，且断开连接
     */
    void stop();

    /**
     * 停止读写器工作，且断开连接，并终止所有读写线程
     */
    void shutdown();
}

