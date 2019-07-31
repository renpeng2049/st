package com.soyoung.st.netty;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

public class NettyMessage implements Serializable {

    private static final long serialVersionUID = 3201210212398130551L;

    /** 消息头字节长度 */
    public static final int HEAD_LEN = 17;

    /** 业务报文 */
    public static final int MESSAGE_TYPE_BIZ = 1;

    /** 心跳报文 */
    public static final int MESSAGE_TYPE_HB = 2;

    /** 魔幻数，约定一个特定数字，所以以此值开头的报文才是有效报文  */
    private int magicNumber = Constants.MAGIC_NUMBER;

    /** 消息体长度，即messageBody.length */
    private int length = 0;

    /** 消息类型：1：业务消息；2：心跳消息 */
    private int messageType = MESSAGE_TYPE_BIZ;

    /** 请求方随机生成logId，响应方将此值原路返回请求方，用以标识同一条消息 */
    private int logId = 0;

    /** 表示是请求0还是响应1 */
    private byte flag = 0;

    /** 消息头，16字节长度，依次由四个数字组成：magicNumber|length|messageType|logId，数字按大端存取 */
    private byte[] messageHead;

    /** 消息体，默认为UTF-8编码 */
    private byte[] messageBody;

    /** 默认心跳报文 */
    public static final NettyMessage HEATBEAT_MSG = buildHeartBeatMsg();

    public static NettyMessage buildHeartBeatMsg() {
        NettyMessage hb = new NettyMessage();
        hb.setMessageType(MESSAGE_TYPE_HB);
        hb.setLogId(10000000); // 心跳报文logId默认设置为10000000
        hb.setMessageBody("HB".getBytes()); // 默认编码即可,英文字符在所有编码结果都是一样的
        return hb;
    }

    public NettyMessage() {
    }

    public NettyMessage(int magicNumber, int length, int messageType, int logId) {
        super();
        this.magicNumber = magicNumber;
        this.length = length;
        this.messageType = messageType;
        this.logId = logId;
    }

    public NettyMessage(String msg) {
        if (msg == null || msg.length() == 0) {
            return;
        }
        this.messageBody = msg.getBytes(StandardCharsets.UTF_8);

        this.length = this.messageBody.length;
    }

    public NettyMessage(byte[] fullMsg) {
        if (fullMsg == null || fullMsg.length < HEAD_LEN) {
            return;
        }

        this.messageHead = new byte[HEAD_LEN];
        System.arraycopy(fullMsg, 0, messageHead, 0, HEAD_LEN);
        this.parseHead();
        if (fullMsg.length > HEAD_LEN) {
            this.messageBody = new byte[this.length];
            System.arraycopy(fullMsg, HEAD_LEN, this.messageBody, 0, this.length);
        }
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(byte[] messageBody) {
        this.messageBody = messageBody;
        if (this.messageBody != null) {
            this.length = this.messageBody.length;
        }
    }

    public void setMessageBody(String mb) {
        if (mb == null || mb.length() == 0) {
            return;
        }

        this.messageBody = mb.getBytes(StandardCharsets.UTF_8);

        this.length = this.messageBody.length;
    }

    public byte[] getMessageHead() {
        if (this.messageHead == null) {
            this.composeHead();
        }
        return messageHead;
    }

    public void setMessageHead(byte[] messageHead) {
        this.messageHead = messageHead;
        this.parseHead();
    }

    private void parseHead() {
        if (messageHead == null || messageHead.length != HEAD_LEN) {
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(messageHead.length);
        byteBuffer.put(messageHead,0,messageHead.length);

//        byte[] tmps = new byte[4];
//        System.arraycopy(messageHead, 0, tmps, 0, 4);
//        this.magicNumber = ByteTransUtil.byteArrayToInt(tmps, false);
        this.magicNumber = byteBuffer.getInt();

//        System.arraycopy(messageHead, 4, tmps, 0, 4);
//        this.length = ByteTransUtil.byteArrayToInt(tmps, false);
        this.length = byteBuffer.getInt();

//        System.arraycopy(messageHead, 8, tmps, 0, 4);
////        this.messageType = ByteTransUtil.byteArrayToInt(tmps, false);
        this.messageType = byteBuffer.getInt();

//        System.arraycopy(messageHead, 12, tmps, 0, 4);
//        this.logId = ByteTransUtil.byteArrayToInt(tmps, false);
        this.logId = byteBuffer.getInt();
    }

    private void composeHead() {
//        this.messageHead = new byte[HEAD_LEN];
//        System.arraycopy(ByteTransUtil.intToByteArray(this.magicNumber, false), 0, messageHead, 0, 4);
//        System.arraycopy(ByteTransUtil.intToByteArray(this.length, false), 0, messageHead, 4, 4);
//        System.arraycopy(ByteTransUtil.intToByteArray(this.messageType, false), 0, messageHead, 8, 4);
//        System.arraycopy(ByteTransUtil.intToByteArray(this.logId, false), 0, messageHead, 12, 4);
//        this.messageHead[HEAD_LEN - 1] = flag;

        ByteBuffer byteBuffer = ByteBuffer.allocate(HEAD_LEN);
        byteBuffer.putInt(magicNumber);
        byteBuffer.putInt(length);
        byteBuffer.putInt(messageType);
        byteBuffer.putInt(logId);
        byteBuffer.put(flag);

        messageHead = byteBuffer.array();
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Msg[magicNumber={0,number,###},length={1,number,###},messageType={2,number,###},logId={3,number,###},flag={4,number,###}][{5}]",
                new Object[] { magicNumber, length, messageType, logId, flag, bodyToString() });
    }

    public String bodyToString() {
        String body = null;
        if (this.messageBody != null && this.messageBody.length > 0) {

            body = new String(messageBody, StandardCharsets.UTF_8);
        }
        return body;
    }

    /**
     * 生成完整消息对应的字节数组，如果没有消息体，就只有头部
     *
     * @return
     */
    public byte[] composeFull() {
        if (this.messageBody != null) {
            this.length = this.messageBody.length;
        }

//        byte[] data = new byte[this.length + HEAD_LEN];
//        System.arraycopy(ByteTransUtil.intToByteArray(this.magicNumber, false), 0, data, 0, 4);
//        System.arraycopy(ByteTransUtil.intToByteArray(this.length, false), 0, data, 4, 4);
//        System.arraycopy(ByteTransUtil.intToByteArray(this.messageType, false), 0, data, 8, 4);
//        System.arraycopy(ByteTransUtil.intToByteArray(this.logId, false), 0, data, 12, 4);
//        data[HEAD_LEN - 1] = flag;
//        if (this.messageBody != null) {
//            System.arraycopy(this.messageBody, 0, data, HEAD_LEN, this.length);
//        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(this.length + HEAD_LEN);
        byteBuffer.putInt(magicNumber);
        byteBuffer.putInt(length);
        byteBuffer.putInt(messageType);
        byteBuffer.putInt(logId);
        byteBuffer.put(flag);

        if (this.messageBody != null) {
            byteBuffer.put(messageBody);
        }

        return byteBuffer.array();
    }
}
