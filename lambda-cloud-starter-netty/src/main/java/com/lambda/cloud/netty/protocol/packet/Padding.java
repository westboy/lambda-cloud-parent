package com.lambda.cloud.netty.protocol.packet;

import org.apache.commons.lang3.StringUtils;

/**
 * 补全方向
 *
 */
public enum Padding {
    /**
     * 向左补0x00
     */
    LEFT {
        @Override
        public String fill(String data, int length) {
            int maxLength = length * 2;
            return StringUtils.leftPad(data, maxLength, "0");
        }

        @Override
        public String getRegex(String regex) {
            return String.format("^(%s)*", regex);
        }
    },
    /**
     * 向右补0x00
     */
    RIGHT {
        @Override
        public String fill(String data, int length) {
            int maxLength = length * 2;
            return StringUtils.rightPad(data, maxLength, "0");
        }

        @Override
        public String getRegex(String regex) {
            return String.format("(%s)*$", regex);
        }
    };

    public String empty(String data, String regex) {
        return data.replaceAll(this.getRegex(regex), "");
    }

    /**
     * 将字符串按指定方向补全
     *
     * @param data   指定字符串
     * @param length 指定长度
     * @return String 补全结果
     */
    public abstract String fill(String data, int length);

    /**
     * 设置正则
     *
     * @param regex 正则
     * @return 设置结果
     */
    public abstract String getRegex(String regex);
}
