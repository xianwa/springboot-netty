package com.haoxy.common.model;

import java.io.Serializable;


/**
 * Created by haoxy on 2018/10/17.
 * E-mail:hxyHelloWorld@163.com
 * github:https://github.com/haoxiaoyong1014
 */
public class CustomProtocol implements Serializable {

    private static final long serialVersionUID = 290429819350651974L;
    private long comId;
    private SendType sendType;
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

    public SendType getSendType() {
        return sendType;
    }

    public void setSendType(SendType sendType) {
        this.sendType = sendType;
    }

    public CustomProtocol(long comId, SendType sendType, String content) {
        this.comId = comId;
        this.sendType = sendType;
        this.content = content;
    }

    public CustomProtocol(){

    }

    @Override
    public String toString() {
        return "CustomProtocol{" +
                "id=" + comId +
                ",sendType="+sendType.name()+"  content='" + content + '\'' +
                '}';
    }

    public enum SendType{
        HEART,TMS_LOGIN,OTHER_LOGIN
    }
}
