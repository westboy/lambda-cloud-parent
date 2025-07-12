package com.lambda.security.provider.wx;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.provider.AbstractThirdPartLoginProvider;
import com.lambda.security.provider.ThirdPartLoginResult;
import com.lambda.security.service.ThirdPartyLoginService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import me.chanjar.weixin.common.error.WxErrorException;

/**
 * WxMaLoginProvider
 *
 * @author Jin
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
public class WxMaLoginProvider<T extends WxMaLoginHandler> extends AbstractThirdPartLoginProvider {

    protected final WxMaService wxMaService;
    protected final T WxMaLoginHandler;

    public WxMaLoginProvider(ThirdPartyLoginService thirdPartService, WxMaService wxMaService, T wxMaLoginHandler) {
        super(thirdPartService);
        this.wxMaService = wxMaService;
        this.WxMaLoginHandler = wxMaLoginHandler;
    }

    @Override
    public ThirdPartLoginResult getThirdLoginParam(String loginParam) {
        try {
            Object result = WxMaLoginHandler.handle(loginParam, wxMaService);
            return new ThirdPartLoginResult(getThirdType(), result);
        } catch (WxErrorException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public boolean support(String thirdId) {
        return getThirdType().equals(thirdId);
    }

    @Override
    public String getThirdType() {
        return "wxMa";
    }
}
