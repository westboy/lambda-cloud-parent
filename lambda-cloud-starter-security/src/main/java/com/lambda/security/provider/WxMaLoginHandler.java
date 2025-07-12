package com.lambda.security.provider;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import me.chanjar.weixin.common.error.WxErrorException;

public interface WxMaLoginHandler {
    default Object handle(String loginParam, WxMaService wxMaService) throws WxErrorException {
        WxMaJscode2SessionResult wxMaJscode2SessionResult = wxMaService.jsCode2SessionInfo(loginParam);
        return wxMaJscode2SessionResult.getOpenid();
    }
}
