package com.casc.stockmanager.utils;

import android.text.TextUtils;
import android.util.Log;

import com.casc.stockmanager.MyParams;
import com.casc.stockmanager.MyVars;
import com.casc.stockmanager.bean.EPCType;
import com.casc.stockmanager.bean.IntStrPair;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    private CommonUtils(){}

    public static IntStrPair getProduct(String epcStr) {
        return getProduct(CommonUtils.hexToBytes(epcStr));
    }

    public static IntStrPair getProduct(byte[] epc) {
        return MyVars.config.getProductByCode(epc[5]);
    }

    public static String getBodyCode(String epcStr) {
        return getBodyCode(hexToBytes(epcStr));
    }

    public static String getBodyCode(byte[] epc) {
        String res = "";
        for (int i = 0; i < 5; i++) {
            res = String.format("%s%s", res, (char) epc[7 + i]);
        }
        return CommonUtils.convertEPCHeader(epc) + res;
    }

    private static String[] hanArr = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
    private static String[] unitArr = { "十", "百", "千", "万", "十", "白", "千", "亿", "十", "百", "千" };
    public static String numToChinese(int number) {
        String numStr = number + "";
        String result = "";
        int numLen = numStr.length();
        for (int i = 0; i < numLen; i++) {
            int num = numStr.charAt(i) - 48;
            if (i != numLen - 1 && num != 0) {

                result += hanArr[num] + unitArr[numLen - 2 - i];
                if (number >= 10 && number < 20) {
                    result = result.substring(1);
                }
            } else {
                if (!(number >= 10 && number % 10 == 0)) {
                    result += hanArr[num];
                }
            }
        }
        return result;
    }

    public static EPCType validEPC(byte[] epc) {
        byte[] validHeader = generateEPCHeader();
        if (epc == null || epc.length < validHeader.length)
            return EPCType.NONE;
        for (int i = 0; i < validHeader.length; i++) {
            if (epc[i] != validHeader[i])
                return EPCType.NONE;
        }
        return EPCType.getType(epc);
    }

    public static byte[] generateEPCHeader() {
        String headerStr = MyVars.config.getVersion() + MyVars.config.getHeader();
        byte[] header = new byte[headerStr.length()];
        for (int i = 0; i < header.length; i++) {
            header[i] = (byte) headerStr.toCharArray()[i];
        }
        return header;
    }

    public static String convertEPCHeader(byte[] epc) {
        String header = "";
        header += (char) epc[1];
        header += (char) epc[2];
        header += (char) epc[3];
        return header;
    }

    public static byte[] generatePC(int epcLength) {
        byte[] pc = new byte[2];
        pc[0] = (byte) (((epcLength / 2) << 3) & 0xFF);
        return pc;
    }

    public static String generateGradientRedColor(int level) {
        StringBuilder color = new StringBuilder("#FF");
        for (int i = 0; i < 2; i++)
            color.append(String.format("%02x", level < 256 ? 255 - level : 0).toUpperCase());
        return color.toString();
    }

    public static Map<String,  String> generateRequestHeader(String role) {
        Map<String, String> header = new HashMap<>();
        header.put("role", role);
        header.put("user_key", "abc");
        header.put("request_time", new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.CHINA).format(new Date()));
        header.put("random_number", String.valueOf(new Random().nextInt()));
        header.put("version_number", "2.0");
        if (!TextUtils.isEmpty(MyVars.api.getCompanySymbol()))
            header.put("company", MyVars.api.getCompanySymbol());
        return header;
    }

    public static RequestBody generateRequestBody(Object body) {
        String jsonStr = toJson(body);
        if (MyParams.PRINT_JSON) {
            Log.i(TAG, jsonStr);
        }
        return RequestBody.create(MediaType.parse("application/json"), jsonStr);
    }

    public static String toJson(Object o) {
        return new GsonBuilder().serializeNulls().create().toJson(o);
    }

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        return bytesToHex(bytes, 0, bytes.length);
    }

    public static String bytesToHex(byte[] bytes, int start, int length) {
        if (bytes == null || bytes.length == 0 || length == 0) return "";
        char[] hexChars = new char[length * 2];
        for ( int j = start; j < length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        int len = hex.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character
                    .digit(hex.charAt(i + 1), 16));
        }
        return b;
    }

    private static final int[] maskArray = {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
    public static long getBitsFromBytes(byte[] b, int start, int length) {
        long result = 0;
        for (int i = 0; i < length; i++) {
            if ((b[(start + i) / 8] & maskArray[(start + i) % 8]) != 0) {
                result += 1L << (length - 1 - i);
            }
        }
        return result;
    }

    public static String generateRandomString(int length) {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        String temp = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
        return temp.substring(0, length);
    }

    public static String convertTime(long time) {
        return new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(time);
    }

    public static String convertDateTime(long time) {
        return new SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA).format(time);
    }

    public static String covertBucketNumToStr(int bucketNum) {
        return bucketNum + " (桶)";
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis() + 12 * 30 * 24 * 60 * 60 * 1000);
        System.out.println(System.currentTimeMillis() + 12L * 30 * 24 * 60 * 60 * 1000);
    }
}
