package com.amigos.attendance.models;

public class FaceRegMsgModel {

    private String opCode;
    private String opMessage;

    public FaceRegMsgModel() {
    }

    public FaceRegMsgModel(String opCode, String opMessage) {
        this.opCode = opCode;
        this.opMessage = opMessage;
    }

    public String getOpCode() {
        return opCode;
    }

    public void setOpCode(String opCode) {
        this.opCode = opCode;
    }

    public String getOpMessage() {
        return opMessage;
    }

    public void setOpMessage(String opMessage) {
        this.opMessage = opMessage;
    }

    @Override
    public String toString() {
        return "FaceRegMsgModel{" +
                "opCode='" + opCode + '\'' +
                ", opMessage='" + opMessage + '\'' +
                '}';
    }
}
