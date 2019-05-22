package com.casc.stockmanager.bean;

public enum EPCType {

    BUCKET((byte) 0x00, "(已注册桶)"),
    CARD_DELIVERY((byte) 0x01, "(出库专用卡)"),
    CARD_ADMIN((byte) 0x02, "(运维专用卡)"),
    CARD_REFLUX((byte) 0x03, "(回流专用卡)"),
    BUCKET_SCRAPED((byte) 0x04, "(报废桶)"),
    NONE((byte) 0xFF, "(UNKOWN)");

    private byte code;
    private String comment;

    EPCType(byte code, String comment) {
        this.code = code;
        this.comment = comment;
    }

    public static EPCType getType(byte[] epc) {
        if (epc.length == 12) {
            for (EPCType type : EPCType.values()) {
                if (epc[4] == type.code)
                    return type;
            }
        }
        return NONE;
    }

    public byte getCode() {
        return code;
    }

    public String getComment() {
        return comment;
    }
}
