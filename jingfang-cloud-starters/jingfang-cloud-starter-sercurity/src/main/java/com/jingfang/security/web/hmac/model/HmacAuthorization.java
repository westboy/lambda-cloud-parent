package com.jingfang.security.web.hmac.model;

import lombok.Getter;
import lombok.Setter;

/**
 * HmacAuthorization
 * @author jpjoo
 */
@Setter
@Getter
public class HmacAuthorization {

	private String appid;
	private String timestamp;
	private String digest;

}
