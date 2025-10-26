package com.lambda.cloud.ykc.message.v17.resp;

import com.lambda.cloud.ykc.message.ResponseBuilder;
import com.lambda.cloud.ykc.message.v17.YkcV17BaseMessage;

/**
 * 云快充1.7协议交易记录确认消息体
 * <p>
 * 对应协议帧类型 0x40，运营平台确认收到充电桩上传的交易记录
 * </p>
 *
 * @author Jin
 */
public class YkcV17TransactionRecordResponseMessage extends YkcV17BaseMessage<YkcV17TransactionRecordResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV17TransactionRecordResponseMessage() {
        this.setFrameType("40");
    }

    /**
     * 带消息体的构造函数
     *
     * @param body 消息体
     */
    public YkcV17TransactionRecordResponseMessage(YkcV17TransactionRecordResponseDetail body) {
        this.setFrameType("40");
        this.setDetail(body);
    }
    
    static {
        ResponseBuilder.register("40", "1.7", protocolMessage -> {
            YkcV17TransactionRecordResponseMessage transactionRecordConfirmResponseMessage = new YkcV17TransactionRecordResponseMessage();
            transactionRecordConfirmResponseMessage.setStartFlag("68");
            transactionRecordConfirmResponseMessage.setSerialNumber(protocolMessage.getSerialNumber());
            return transactionRecordConfirmResponseMessage;
        });
    }
}