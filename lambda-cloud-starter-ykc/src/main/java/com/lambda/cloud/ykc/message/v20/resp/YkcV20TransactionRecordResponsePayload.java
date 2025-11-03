package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 交易记录响应消息
 * <p>
 * 帧类型码：0x3D
 * 传送间隔：按需发送
 * 功能：交易记录上传
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString(callSuper = true)
public class YkcV20TransactionRecordResponsePayload extends YkcV20BasePayload<YkcV20TransactionRecordResponseDetail> {

    /**
     * 默认构造函数
     */
    public YkcV20TransactionRecordResponsePayload() {
        super();
        setFrameType("3D");
    }

    /**
     * 带详细信息的构造函数
     *
     * @param detail 交易记录响应详细信息
     */
    public YkcV20TransactionRecordResponsePayload(YkcV20TransactionRecordResponseDetail detail) {
        super();
        setFrameType("3D");
        setDetail(detail);
    }
}
