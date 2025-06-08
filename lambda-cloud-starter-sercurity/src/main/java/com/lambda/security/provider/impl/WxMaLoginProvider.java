package com.lambda.security.provider.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.provider.AbstractThirdPartLoginProvider;
import com.lambda.security.service.ThirdPartyLoginService;
import me.chanjar.weixin.common.error.WxErrorException;

/**
 * WxMaLoginProvider
 *
 * @author Jin
 */
public class WxMaLoginProvider extends AbstractThirdPartLoginProvider {

    private final WxMaService wxMaService;

    public WxMaLoginProvider(ThirdPartyLoginService thirdPartService, WxMaService wxMaService) {
        super(thirdPartService);
        this.wxMaService = wxMaService;
    }

    @Override
    public String getThirdUserId(String code) {
        try {
            WxMaJscode2SessionResult wxMaJscode2SessionResult = wxMaService.jsCode2SessionInfo(code);
            return wxMaJscode2SessionResult.getOpenid();
        } catch (WxErrorException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public boolean support(String thirdId) {
        return "wxMa".equals(thirdId);
    }
}
