package org.keycloak.sms.auth;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.requiredactions.VerifyEmail;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.validation.Validation;
import org.keycloak.utils.CredentialHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author yanfeiwuji
 * @description
 * @date 16:09 2020/2/21
 */
public class SmsUtil {

	private static final Logger logger = Logger.getLogger(VerifyEmail.class);

	public static final String SUCCESS_FLAG = "success";

//	public static Cache<String, String> sms_cache = get_cache();

	private static final String SMS_CATCH_NAME = "sms-verify";

	private static final String CODE_CATCH_SUFFIX = "-code";

//	private static Cache<String, String> get_cache(Map<String, String> config) {
//		try {
//			Cache<String, String> cache = getCacheManager(config).getCache(SMS_CATCH_NAME);
//			logger.info(cache);
//			return cache;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace(System.out);
//			throw e;
//		}
//	}
//
//	private static DefaultCacheManager getCacheManager(Map<String, String> config123) {
//		if (_cacheManager == null) {
//
////			String cacheContainer = config.get("authorization");
////			try {
////				_cacheManager = (DefaultCacheManager) new InitialContext().lookup(cacheContainer);
////			} catch (NamingException e) {
////				// TODO Auto-generated catch block
////				logger.error(e);
////				e.printStackTrace();
////			}
//
//			ConfigurationBuilder config = new ConfigurationBuilder();
//			_cacheManager = new DefaultCacheManager();
//			_cacheManager.defineConfiguration(SMS_CATCH_NAME, config.build());
//
//		}
//		return _cacheManager;
//	}

	// Store the code + expiration time in a UserCredential. Keycloak will persist
	// these in the DB.
	// When the code is validated on another node (in a clustered environment) the
	// other nodes have access to it's values too.
	@SuppressWarnings("deprecation")
	private static void storeSMSCode(AuthenticationFlowContext context, String code, Long expiringAt) {
		CredentialModel credentialsCode = new CredentialModel();

		credentialsCode.setType(SmsAuthenticatorContstants.USR_CRED_MDL_SMS_CODE);
		credentialsCode.setValue(code);

		UserCredentialManager userCredentialManager = context.getSession().userCredentialManager();

		if (userCredentialManager.isConfiguredFor(context.getRealm(), context.getUser(),
				SmsAuthenticatorContstants.USR_CRED_MDL_SMS_CODE)) {
			userCredentialManager.updateCredential(context.getRealm(), context.getUser(), credentialsCode);
		} else {
			userCredentialManager.createCredential(context.getRealm(), context.getUser(), credentialsCode);
		}

		CredentialModel credentialsCodeExp = new CredentialModel();
		credentialsCodeExp.setType(SmsAuthenticatorContstants.USR_CRED_MDL_SMS_EXP_TIME);
		credentialsCodeExp.setValue((expiringAt).toString());

		if (userCredentialManager.isConfiguredFor(context.getRealm(), context.getUser(),
				SmsAuthenticatorContstants.USR_CRED_MDL_SMS_EXP_TIME)) {
			userCredentialManager.updateCredential(context.getRealm(), context.getUser(), credentialsCodeExp);
		} else {
			userCredentialManager.createCredential(context.getRealm(), context.getUser(), credentialsCodeExp);
		}
	}

	private static enum CODE_STATUS {
		VALID, INVALID, EXPIRED
	}

	private static CODE_STATUS validateCode(AuthenticationFlowContext context, String enteredCode) {
		CODE_STATUS result = CODE_STATUS.INVALID;

		logger.debug("validateCode called ... ");

		String expectedCode = SMSAuthenticatorUtil.getCredentialValue(context,
				SmsAuthenticatorContstants.USR_CRED_MDL_SMS_CODE);
		String expTimeString = SMSAuthenticatorUtil.getCredentialValue(context,
				SmsAuthenticatorContstants.USR_CRED_MDL_SMS_EXP_TIME);

		logger.debug("Expected code = " + expectedCode + "    entered code = " + enteredCode);

		if (expectedCode != null) {
			result = enteredCode.equals(expectedCode) ? CODE_STATUS.VALID : CODE_STATUS.INVALID;
			long now = new Date().getTime();

			logger.debug("Valid code expires in " + (Long.parseLong(expTimeString) - now) + " ms");
			if (result == CODE_STATUS.VALID) {
				if (Long.parseLong(expTimeString) < now) {
					logger.debug("Code is expired !!");
					result = CODE_STATUS.EXPIRED;
				}
			}
		}
		logger.debug("result : " + result);
		return result;
	}

	private static final String[] NEED_KEYS = new String[] { SmsAuthenticatorContstants.CONF_APP_ID,
			SmsAuthenticatorContstants.CONF_APP_KEY, SmsAuthenticatorContstants.CONF_TWMPLATE_ID,
			SmsAuthenticatorContstants.CONF_SIGN, SmsAuthenticatorContstants.CONF_TEMPLATE_PARAMS,
			SmsAuthenticatorContstants.CONF_SMS_CODE_LENGTH, SmsAuthenticatorContstants.CONF_SMS_CODE_EXP };

	public static String sendSms(String phoneNumber, AuthenticationFlowContext context) {

		AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();

		Map<String, String> config = configModel.getConfig();

		// TODO:验证是否存在没有过期的验证码
//		if (validateCode(context) == CODE_STATUS.VALID) {
//			return SUCCESS_FLAG;
//		}
//		String phoneHasCode = validateCode(context); // get_cache(config).get(phoneNumber);
//		if (phoneHasCode != null) {
//			return SUCCESS_FLAG;
//		}

		if (configModel == null) {
			logger.infof("该流程中未定义短信相关参数");
			return "未配置";
		}

		// Map<String, String> config = configModel.getConfig();

		boolean allHas = Stream.of(NEED_KEYS).allMatch(k -> {
			String v = config.get(k);
			if (Validation.isBlank(v)) {
				logger.infof("流程 %s 中未配置参数 %s ", context.getFlowPath(), k);
				return false;
			} else {
				return true;
			}
		});

		if (!allHas) {
			return "未配置";
		}

		// 参数校验
		String appid = config.get(SmsAuthenticatorContstants.CONF_APP_ID);
		String appKey = config.get(SmsAuthenticatorContstants.CONF_APP_KEY);
		String templateId = config.get(SmsAuthenticatorContstants.CONF_TWMPLATE_ID);
		String sign = config.get(SmsAuthenticatorContstants.CONF_SIGN);
		String templateParams = config.get(SmsAuthenticatorContstants.CONF_TEMPLATE_PARAMS);
		String smsCodeLength = config.get(SmsAuthenticatorContstants.CONF_SMS_CODE_LENGTH);
		String smsCodeExp = config.get(SmsAuthenticatorContstants.CONF_SMS_CODE_EXP);

		Integer codeLength = Integer.valueOf(smsCodeLength);
		int codeExp = Integer.parseInt(smsCodeExp);
		String code = String.valueOf((int) ((Math.random() * 9 + 1) * Math.pow(10, codeLength - 1)));

		String[] params = templateParams.split("#");

		List<String> listParams = Stream.of(params).map(i -> {
			if (i.equals("{code}")) {
				return code;
			}
			if (i.equals("{exp}")) {
				return smsCodeExp;
			}
			return i;
		}).collect(Collectors.toList());

		logger.infof("key value");
		config.forEach((k, v) -> {
			logger.infof("%s %s", k, v);
		});

		SmsSingleSender smsSingleSender = new SmsSingleSender(Integer.valueOf(appid), appKey);

//		storeSMSCode(context, code, new Date().getTime() + codeExp * 60 * 1000 * 1000);

		try {

			// 发送短信
			SmsSingleSenderResult result = smsSingleSender.sendWithParam("86", phoneNumber, Integer.valueOf(templateId),
					new ArrayList<>(listParams), sign, "", "");
			logger.infof(result.toString());
			if (result.result == 0) {

				storeSMSCode(context, code, new Date().getTime() + codeExp * 60 * 1000 * 1000);

				logger.infof("发送验证码成功并缓存");
				return SUCCESS_FLAG;
			} else {
				return result.errMsg;
			}

		} catch (HTTPException | IOException e) {
			e.printStackTrace();
		}
		return SUCCESS_FLAG;
	}

	public static String checkCode(String phoneNumber, String inputCode, AuthenticationFlowContext context) {
		AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();

		Map<String, String> config = configModel.getConfig();

		CODE_STATUS reStatus = validateCode(context, inputCode);
		switch (reStatus) {
		case VALID:
			return SUCCESS_FLAG;
		case EXPIRED:
			return "验证码已过期";
		case INVALID:
			return "验证码输入错误";
		default:
			break;
		}
		return "";
	}
}
