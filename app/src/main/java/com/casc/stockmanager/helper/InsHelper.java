package com.casc.stockmanager.helper;

import com.casc.stockmanager.utils.CommonUtils;

import java.util.Arrays;

/**
 * 固件指令
 * 对应 UL6+ 通讯协议使用说明。指令转码生成。
 * <p>
 * 默认byte数组为大端存储！
 * （数据的高字节保存在数组的低地址）
 */
public class InsHelper {
    private InsHelper() {
    }

    public static final byte INS_HEADER = (byte) 0xBB;
    public static final byte INS_END = (byte) 0x7E;

    /**
     * 指令帧类型
     * <p>
     * 0x00 命令帧: 由上位机发送给芯片
     * 0x01 响应帧: 由UL6+芯片发回给上位机
     * 0x02 通知帧: 由UL6+芯片发回给上位机
     */
    private static final byte[] INS_TYPE = new byte[]{(byte) 0x00, (byte) 0x01, (byte) 0x02};

    /**
     * 标签数据存储区类型
     */
    public enum MemBankType {
        RFO(0, (byte) 0x00), EPC(1, (byte) 0x01), TID(2, (byte) 0x02), UserMem(3, (byte) 0x03);

        private int key;
        private byte vaule;

        MemBankType(int key, byte value) {
            this.key = key;
            this.vaule = value;
        }

        public static byte getValue(int key) {
            for (MemBankType m : MemBankType.values()) {
                if (m.getKey() == key) {
                    return m.getVaule();
                }
            }
            return RFO.getVaule();
        }

        public int getKey() {
            return key;
        }

        public byte getVaule() {
            return vaule;
        }
    }

    public enum SelectModel {

    }

    /**
     * Command 类型
     */
    private static final byte[] INS_CMD = {};


    /**
     * 获取读写器模块信息
     */
    public static byte[] getRfInfo() {
        return RF_INFO;
    }

    private static final byte[] RF_INFO = new byte[]{INS_HEADER, INS_TYPE[0], (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x04, INS_END};

    /**
     * 单次轮询指令
     * <p>
     * 完成一次EPC Class1 Gen2 协议中轮询Inventory 操作。该指令中不包含Select 操作。每次轮询指令执行前
     * 后都会自动打开和关闭功放。单次轮询Inventory 指令中，Query 操作参数由另外一条指令来配置，固件中已
     * 经有初始值。
     */
    public static byte[] getSinglePolling() {
        return SINGLE_POLLING;
    }

    private static final byte[] SINGLE_POLLING = new byte[]{INS_HEADER, INS_TYPE[0], (byte) 0x22, (byte) 0x00, (byte) 0x00, (byte) 0x22, INS_END};

    /**
     * 多次轮询指令
     * index的7、8位表示次数
     * <p>
     * 该指令要求芯片MCU 进行多次轮询Inventory 操作，轮询次数限制为0至65535 次。
     *
     * @param cnt pollingAmount
     * @return
     */
    public static byte[] getMultiPolling(final int cnt) {
        if (cnt < 0 || cnt > (1 << 16) - 1) {
            throw new IllegalArgumentException("cnt is out of range! [0, 65535]");
        }
        byte[] multiPoing = new byte[MULTI_POLLING.length];
        System.arraycopy(MULTI_POLLING, 0, multiPoing, 0, MULTI_POLLING.length);

        byte lsb = (byte) (cnt & 0xff);
        byte msb = (byte) (cnt >> 8 & 0xff);
        multiPoing[6] = msb;
        multiPoing[7] = lsb;

        multiPoing[8] = getChecksum(multiPoing, 1, 8);

        return multiPoing;
    }

    private static final byte[] MULTI_POLLING = new byte[]{
            INS_HEADER, INS_TYPE[0], (byte) 0x27, (byte) 0x00, (byte) 0x03, (byte) 0x22, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, INS_END};

    /**
     * 停止多次轮询指令
     * <p>
     * 在芯片内部MCU 进行多次轮询Inventory 操作的过程中，
     * 可以立即停止多次轮询操作，非暂停多次轮询操作。
     */
    public static byte[] getStopMultiPolling() {
        return TERMINATE_MULTI_POLLING;
    }

    private static final byte[] TERMINATE_MULTI_POLLING = new byte[]{INS_HEADER, INS_TYPE[0], (byte) 0x28, (byte) 0x00, (byte) 0x00, (byte) 0x28, INS_END};

    /**
     * 设置Select 参数指令
     * <p>
     * 设置Select 参数，并且同时设置Select 模式为0x02。在对标签除轮询操作之前，先发送Select 指令。在多
     * 标签的情况下，可以根据Select 参数只对特定标签进行轮询和读写等操作。
     */
    public static byte[] getSelectParameter(byte selParam, byte[] ptr, boolean truncate, byte[] mask) {
        if (ptr == null || mask == null) {
            throw new IllegalArgumentException("ptr OR mask is null!");
        }
        byte[] selectParameter = new byte[5 + 1 + ptr.length + 2 + mask.length + 2];
        int p = 5;
        System.arraycopy(SELECT_PARAMETER, 0, selectParameter, 0, p);

        selectParameter[p++] = selParam;
        for (int i = 0; i < ptr.length; i++) {
            selectParameter[p++] = ptr[i]; // Ptr
        }
        selectParameter[p++] = (byte) (mask.length << 3); // MaskLen in bit
        selectParameter[p++] = truncate ? (byte) 0x80 : (byte) 0x00; // Truncate
        for (int i = 0; i < mask.length; i++) {
            selectParameter[p++] = mask[i]; // Mask
        }

        int pl = p - 5;
        selectParameter[3] = (byte) (pl >> 8 & 0xFF); // pl
        selectParameter[4] = (byte) (pl & 0xFF);

        selectParameter[p++] = getChecksum(selectParameter, 1, p);
        selectParameter[p++] = INS_END;
        return selectParameter;
    }

    public static byte[] getSelectParameter(byte target, byte action, MemBankType memBankType, byte[] ptr, boolean truncate, byte[] mask) {
        byte selParm = (byte) ((target << 5) | (action << 2) | memBankType.getVaule());
        return getSelectParameter(selParm, ptr, truncate, mask);
    }

    public static byte[] getEPCSelectParameter(byte[] mask) {
        return getSelectParameter((byte) 0x01, CommonUtils.hexToBytes("00000020"), false, mask);
    }

    private static final byte[] SELECT_PARAMETER = new byte[]{
            INS_HEADER, INS_TYPE[0], (byte) 0x0C, (byte) 0x00, (byte) 0x13, (byte) 0x01, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x20, (byte) 0x60, (byte) 0x00, (byte) 0x30, (byte) 0x75, (byte) 0x1F, (byte) 0xEB,
            (byte) 0x70, (byte) 0x5C, (byte) 0x59, (byte) 0x04, (byte) 0xE3, (byte) 0xD5, (byte) 0x0D, (byte) 0x70,
            (byte) 0xAD, INS_END};

    /**
     * 设置Select 模式
     * 如果已经设置好了Select 参数，执行该条指令，可以设置Select 模式。
     *
     * @param model
     * @return
     */
    public static byte[] getSelectModel(byte model) {
        byte[] setSelectModel = new byte[SELECT_MODEL.length];
        System.arraycopy(SELECT_MODEL, 0, setSelectModel, 0, SELECT_MODEL.length);

        if (model == (byte) 0x00) {
            setSelectModel[5] = (byte) 0x00;
        } else if (model == (byte) 0x01) {
            setSelectModel[5] = (byte) 0x01;
        } else if (model == (byte) 0x02) {
            setSelectModel[5] = (byte) 0x02;
        } else {
            // 其他情况
            throw new IllegalArgumentException("set 'Select' model is Illegal!");
        }
        setSelectModel[6] = getChecksum(setSelectModel, 1, 6);

        return setSelectModel;
    }

    private static final byte[] SELECT_MODEL = new byte[]{INS_HEADER, INS_TYPE[0], (byte) 0x12, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x14, INS_END};

    /**
     * 读标签数据存储区
     * <p>
     * 对单个标签，读取标签数据存储区Memory Bank 中指定地址和长度的数据。
     *
     * @param ap
     * @param memBankType
     * @param sa          单位为Word，即2 个Byte/16 个Bit
     * @param dl          单位为Word，即2 个Byte/16 个Bit
     * @return
     */
    public static byte[] getReadMemBank(byte[] ap, MemBankType memBankType, int sa, int dl) {
        if (ap == null || ap.length == 0) {
            throw new IllegalArgumentException("Parameter is null! OR length is zero!");
        }
        if (ap.length != 4) {
            throw new IllegalArgumentException("If there is an 'Access Password', then it's length must equals FOUR!");
        }

        byte[] readMemBank = new byte[5 + ap.length + 1 + 2 + 2 + 2];
        int p = 5;
        System.arraycopy(READ_MEM_BANK, 0, readMemBank, 0, p);

        readMemBank[p++] = ap[0]; // ap
        readMemBank[p++] = ap[1];
        readMemBank[p++] = ap[2];
        readMemBank[p++] = ap[3];

        readMemBank[p++] = memBankType.getVaule(); // MemBank

        readMemBank[p++] = (byte) (sa >> 8 & 0xFF); // sa
        readMemBank[p++] = (byte) (sa & 0xFF);

        readMemBank[p++] = (byte) (dl >> 8 & 0xFF); // dl, 单位为Word，即2 个Byte/16 个Bit
        readMemBank[p++] = (byte) (dl & 0xFF);

        readMemBank[p++] = getChecksum(readMemBank, 1, p); // checksum
        readMemBank[p++] = INS_END;

        return readMemBank;
    }

    private static final byte[] READ_MEM_BANK = new byte[]{
            INS_HEADER, INS_TYPE[0], (byte) 0x39, (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
            (byte) 0xFF, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x45, INS_END};

    /**
     * 写标签数据存储区
     * <p>
     * 对单个标签，写入标签数据存储区Memory Bank 中指定地址和长度的数据。标签数据区地址偏移SA 和要写
     * 入的标签数据长度DL，他们的单位为Word，即2 个Byte/16 个Bit。这条指令之前应先设置Select 参数，以
     * 便选择指定的标签进行写标签数据区操作。如果Access Password 全为零，则不发送Access 指令。
     *
     * @param ap          Access Password
     * @param memBankType
     * @param sa          标签数据区地址偏移
     * @param dt          写入数据
     * @return
     */
    public static byte[] getWriteMemBank(byte[] ap, MemBankType memBankType, int sa, byte[] dt) {
        if (ap == null || ap.length == 0 || dt == null || dt.length == 0) {
            throw new IllegalArgumentException("Parameter is null! OR length is zero!");
        }
        if (ap.length != 4) {
            throw new IllegalArgumentException("If there is an 'Access Password', then it's length must equals FOUR!");
        }

        byte[] writeMemBank = new byte[5 + ap.length + 1 + 2 + 2 + dt.length + 2 + (dt.length % 2)];
        int p = 5;
        System.arraycopy(WRITE_MEM_BANK, 0, writeMemBank, 0, p);

        writeMemBank[p++] = ap[0]; // ap 顺序
        writeMemBank[p++] = ap[1];
        writeMemBank[p++] = ap[2];
        writeMemBank[p++] = ap[3];

        writeMemBank[p++] = memBankType.getVaule(); // MemBank

        writeMemBank[p++] = (byte) (sa >> 8 & 0xFF); // sa
        writeMemBank[p++] = (byte) (sa & 0xFF);

        int dl = (dt.length + 1) >> 1; // 单位为Word，即2 个Byte/16 个Bit
        writeMemBank[p++] = (byte) (dl >> 8 & 0xFF); // dl
        writeMemBank[p++] = (byte) (dl & 0xFF);
        for (int i = 0; i < dt.length; i++) {
            writeMemBank[p++] = dt[i]; // dt
        }
        if (dt.length % 2 == 1) { // 偶数
            writeMemBank[p++] = (byte) 0x00; // 数组末尾补0
        }

        int pl = p - 5;
        writeMemBank[3] = (byte) (pl >> 8 & 0xFF); // pl
        writeMemBank[4] = (byte) (pl & 0xFF);

        writeMemBank[p++] = getChecksum(writeMemBank, 1, p); // checksum
        writeMemBank[p++] = INS_END;

        return writeMemBank;
    }

    private static final byte[] WRITE_MEM_BANK = new byte[]{
            INS_HEADER, INS_TYPE[0], (byte) 0x49, (byte) 0x00, (byte) 0x0D, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
            (byte) 0xFF, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x12, (byte) 0x34,
            (byte) 0x56, (byte) 0x78, (byte) 0x6D, INS_END};

    /**
     * 获取Query 参数
     *
     * @return
     */
    public static byte[] getQueryParameter() {
        return GET_QUERY_PARAMETER;
    }

    private static final byte[] GET_QUERY_PARAMETER = new byte[]{INS_HEADER, INS_TYPE[0], (byte) 0x0D, (byte) 0x00, (byte) 0x00, (byte) 0x0D, INS_END};

    /**
     * 设置Query 参数，取值范围为0至15
     * <p>
     * 参数为2 字节，具体参数按位拼接而成。
     *
     * @param para
     * @return
     */
    public static byte[] setQueryParameter(byte[] para) {
        if (para == null || para.length != 2) {
            throw new IllegalArgumentException("para is null OR length not equals 2!");
        }
        byte[] setQueryParameter = new byte[SET_QUERY_PARAMETER.length];
        System.arraycopy(SET_QUERY_PARAMETER, 0, setQueryParameter, 0, SET_QUERY_PARAMETER.length);

        setQueryParameter[5] = para[0]; // Para(MSB)
        setQueryParameter[6] = para[1]; // Para(LSB)
        setQueryParameter[7] = getChecksum(setQueryParameter, 1, 7);
        return setQueryParameter;
    }

    /**
     * 设置Query 参数，取值范围为0至15
     * （DR、M、TRext不可设）
     *
     * @param sel
     * @param session
     * @param target
     * @param q
     * @return
     */
    public static byte[] setQueryParameter(byte sel, byte session, byte target, int q) {
        byte[] para = new byte[2];
        para[0] = (byte) ((0x01 << 4) | ((sel & 0x03) << 2) | (session & 0x3)); // Para(MSB)
        para[1] = (byte) (((target & 0x01) << 7) | (q & 0x0F) << 3); // Para(LSB)
        return setQueryParameter(para);
    }

    private static final byte[] SET_QUERY_PARAMETER = new byte[]{INS_HEADER, INS_TYPE[0], (byte) 0x0E, (byte) 0x00, (byte) 0x02, (byte) 0x10, (byte) 0x20, (byte) 0x40, INS_END};

    /**
     * 获取发射功率
     *
     * @return
     */
    public static byte[] getTransmitPower() {
        return GET_TRANSMIT_POWER;
    }

    private static final byte[] GET_TRANSMIT_POWER = new byte[]{INS_HEADER, INS_TYPE[0], (byte) 0xB7, (byte) 0x00, (byte) 0x00, (byte) 0xB7, INS_END};

    /**
     * 设置发射功率
     *
     * @param powINdBm power value in dBm
     * @return
     */
    public static byte[] setTransmitPower(int powINdBm) {
        if (powINdBm <= 0) {
            throw new IllegalArgumentException("pow(in dBm) is out of range!");
        }
        byte[] setTransmitPower = new byte[SET_TRANSMIT_POWER.length];
        System.arraycopy(SET_TRANSMIT_POWER, 0, setTransmitPower, 0, SET_TRANSMIT_POWER.length);

        setTransmitPower[5] = (byte) ((powINdBm * 100) >> 8 & 0xFF); // Pow(MSB)
        setTransmitPower[6] = (byte) ((powINdBm * 100) & 0xFF); // Pow(LSB)

        setTransmitPower[7] = getChecksum(setTransmitPower, 1, 7);

        return setTransmitPower;
    }

    private static final byte[] SET_TRANSMIT_POWER = new byte[]{INS_HEADER, INS_TYPE[0], (byte) 0xB6, (byte) 0x00, (byte) 0x02, (byte) 0x07, (byte) 0xD0, (byte) 0x8F, INS_END};

    public static byte[] getSensorIOPort() {
        return controlIOPort((byte) 0x02, (byte) 0x00, (byte) 0x00);
    }

    public static byte[] setSensorIOPort() {
        return controlIOPort((byte) 0x00, (byte) 0x00, (byte) 0x00);
    }

    public static byte[] controlIOPort(byte p1, byte p2, byte p3) {
        byte[] controlIOPort = CONTROL_IO_PORT.clone();

        controlIOPort[5] = p1;
        controlIOPort[6] = p2;
        controlIOPort[7] = p3;

        controlIOPort[8] = getChecksum(controlIOPort, 1, 8);

        return controlIOPort;
    }

    private static final byte[] CONTROL_IO_PORT = new byte[]{INS_HEADER, INS_TYPE[0], (byte) 0x1A, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x22, INS_END};

    public static boolean checkIns(byte[] data, int start, int end) {
        int pl = ((data[start + 3] & 0xFF) << 8) + (data[start + 4] & 0xFF);
        return (data[start + 1] == 0x01 || data[start + 1] == 0x02) // 检查Type字段
                && pl + 6 == end - start // 检查指令长度
                && data[end - 1] == getChecksum(data, start + 1, end - 1); // 检查Checksum
    }

    public static byte[] getReadContent(byte[] data) {
        return Arrays.copyOfRange(data, 6 + data[5], data.length - 2);
    }

    public static byte[] getWriteContent(byte[] data) {
        return Arrays.copyOfRange(data, 8, data.length - 3);
    }


    /**
     * 计算cmd命令的第 beginIndex 位至第 rightIndex-1 位对应的校验和，instruction[beginIndex, rightIndex-1]。
     * <p>
     * ---校验位Checksum 为从帧类型Type 到最后一个指令参数Parameter 累加和，
     * 并只取累加和最低一个字节(LSB)。
     *
     * @param instruction
     * @param beginIndex
     * @param rightIndex
     * @return
     */
    public static byte getChecksum(byte[] instruction, int beginIndex, int rightIndex) {
        if (instruction == null || instruction.length == 0) {
            throw new IllegalArgumentException("instruction is NULL！");
        }
        if (beginIndex < 0 || rightIndex > instruction.length) {
            throw new IllegalArgumentException("Wrong instruction range！");
        }
        int checksum = 0;
        for (int i = beginIndex; i < rightIndex; i++) {
            checksum = checksum + (instruction[i] >= 0 ? instruction[i] : (instruction[i] + (1 << 8)));
        }
        return (byte) checksum; // 直接溢出，保留低位
    }

    /**
     * 统计指令参数长度PL
     *
     * @param arr
     * @return 大端
     */
    private static byte[] getPL(byte[] arr) {
        if (arr == null) {
            return new byte[]{(byte) 0x00, (byte) 0x00};
        }
        if (arr.length < 7) {
            throw new IllegalArgumentException("Instruction is too short to count its length! (at least bigger then seven)");
        }
        if ((arr.length >> 16) != 0x00) {
            throw new IllegalArgumentException("Instruction is too long!");
        }

        byte lsb = (byte) ((arr.length - 7) & 0xff);
        byte msb = (byte) ((arr.length - 7) >> 8 & 0xff);
        return new byte[]{msb, lsb};
    }

    /**
     * 判断是否数组的所有元素都为0（即任何元素都不包含1）
     *
     * @param arr
     * @return
     */
    private static boolean isOnlyComposedofZero(byte[] arr) {
        if (arr == null || arr.length == 0) return true;
        for (int i = 0, length = arr.length; i < length; i++) {
            if (arr[i] != (byte) 0x00)
                return false;
        }
        return true;
    }

    private static final char[] HEX_CHAR_ARR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) { // 利用位运算进行转换，可以看作方法一的变种
            sb.append(HEX_CHAR_ARR[b >>> 4 & 0xf]);
            sb.append(HEX_CHAR_ARR[b & 0xf]);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(CommonUtils.bytesToHex(getReadMemBank(new byte[4], MemBankType.TID, 0, 6)));
    }
}
