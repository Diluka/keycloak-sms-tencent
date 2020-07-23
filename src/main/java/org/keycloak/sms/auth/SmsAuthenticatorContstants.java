package org.keycloak.sms.auth;

/**
 * @author yanfeiwuji
 * @description 常量
 * @date 15:16 2020/2/21
 */

public class SmsAuthenticatorContstants {

	/**
	 * ---- conf start -----------
	 **/
	public static final String CONF_APP_ID = "appid";
	public static final String CONF_APP_KEY = "appKey";

	// 签名
	public static final String CONF_SIGN = "sign";
	// 模板id
	public static final String CONF_TWMPLATE_ID = "templateId";
	// 模板参数

	public static final String CONF_TEMPLATE_PARAMS = "templateParams";
	// 验证码长度
	public static final String CONF_SMS_CODE_LENGTH = "smsCodeLength";

	// 验证码过期时间 分钟为单位
	public static final String CONF_SMS_CODE_EXP = "smsCodeExp";

	/**
	 * ---- conf start -----------
	 **/

//  public static final String ATTR_MOBILE = "mobileNumber";
	public static final String ANSW_SMS_CODE = "smsCode";

	// Configurable fields
	public static final String CONF_PRP_USR_ATTR_MOBILE = "sms-auth.attr.mobile";
	public static final String CONF_PRP_SMS_CODE_TTL = "sms-auth.code.ttl";
	public static final String CONF_PRP_SMS_CODE_LENGTH = "sms-auth.code.length";
	public static final String CONF_PRP_SMS_TEXT = "sms-auth.msg.text";

	public static final String CONF_PRP_SMS_URL = "sms-auth.sms.url";
	public static final String CONF_PRP_SMS_METHOD = "sms-auth.sms.method";
	public static final String CONF_PRP_SMS_USERNAME = "sms-auth.sms.username";
	public static final String CONF_PRP_SMS_PASSWORD = "sms-auth.sms.password";
	public static final String CONF_PRP_SMS_AUTHTYPE = "sms-auth.sms.authtype";
	public static final String CONF_PRP_CONTENT_TYPE = "sms-auth.content.type";

	public static final String CONF_PRP_PROXY_URL = "sms-auth.proxy.url";
	public static final String CONF_PRP_PROXY_USERNAME = "sms-auth.proxy.username";
	public static final String CONF_PRP_PROXY_PASSWORD = "sms-auth.proxy.password";

	// User credentials (used to persist the sent sms code + expiration time cluster
	// wide)
	public static final String USR_CRED_MDL_SMS_CODE = "sms-auth.code";
	public static final String USR_CRED_MDL_SMS_EXP_TIME = "sms-auth.exp-time";

	// Authentication methods
	public static final String AUTH_METHOD_BASIC = "Basic authentication";
	public static final String AUTH_METHOD_INMESSAGE = "In message authentication";
}
