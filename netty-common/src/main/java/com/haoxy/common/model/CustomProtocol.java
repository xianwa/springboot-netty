package com.haoxy.common.model;

import java.io.Serializable;


/**
 * Created by haoxy on 2018/10/17. E-mail:hxyHelloWorld@163.com github:https://github.com/haoxiaoyong1014
 */
public class CustomProtocol  implements Serializable {

    private static final long serialVersionUID = 290429819350651974L;
    private long comId;
    private int sendType;
    private String content;

    public long getComId() {
        return comId;
    }

    public void setComId(long comId) {
        this.comId = comId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }

    public CustomProtocol(long comId, int sendType, String content) {
        this.comId = comId;
        this.sendType = sendType;
        this.content = content;
    }

    public CustomProtocol() {

    }

    @Override
    public String toString() {
        return "CustomProtocol{" +
                "id=" + comId +
                ",sendType=" + SendType.ofCode(sendType) + "  content='" + content + '\'' +
                '}';
    }

    public enum SendType {
        HEART(0, "heart"),
        TMS_LOGIN(1, "tms_login"),
        OTHER_LOGIN(2, "other_login");

        public int code;
        public String desc;

        SendType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static SendType ofCode(int code) {
            for (SendType sendType : SendType.values()) {
                if (code == sendType.code) {
                    return sendType;
                }
            }
            return HEART;
        }
    }
}
