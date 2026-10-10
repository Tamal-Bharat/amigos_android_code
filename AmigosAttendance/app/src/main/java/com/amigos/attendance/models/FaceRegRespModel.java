package com.amigos.attendance.models;

public class FaceRegRespModel {

    private int code;
    private FaceRegMsgModel message;

    public FaceRegRespModel() {
    }

    public FaceRegRespModel(int code, FaceRegMsgModel message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public FaceRegMsgModel getMessage() {
        return message;
    }

    public void setMessage(FaceRegMsgModel message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "FaceRegRespModel{" +
                "code=" + code +
                ", message=" + message +
                '}';
    }
}
