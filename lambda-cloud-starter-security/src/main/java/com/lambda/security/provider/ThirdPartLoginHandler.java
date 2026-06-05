package com.lambda.security.provider;

import com.lambda.cloud.core.exception.NotSupportedException;

/**
 * 三方登录处理器接口
 *
 * <h3>自定义实现示例：</h3>
 * <pre>{@code
 * @Component
 * public class CustomWxMaLoginHandler implements WxMaLoginHandler {
 *
 *     @Override
 *     public Object handle(String loginParam, WxMaService wxMaService) throws WxErrorException {
 *         // 1. 获取session信息
 *         WxMaJscode2SessionResult sessionResult = wxMaService.jsCode2SessionInfo(loginParam);
 *
 *         // 2. 构建自定义返回对象
 *         Map<String, Object> result = new HashMap<>();
 *         result.put("openid", sessionResult.getOpenid());
 *         result.put("unionid", sessionResult.getUnionid());
 *         result.put("sessionKey", sessionResult.getSessionKey());
 *
 *         // 3. 可以在这里添加更多业务逻辑
 *         // 如：用户信息预处理、数据校验等
 *
 *         return result;
 *     }
 * }
 * }</pre>
 *
 *
 */
public interface ThirdPartLoginHandler {

    default Object handle(String loginParam) {
        throw new NotSupportedException("当前方法的未实现: " + loginParam);
    }
}
